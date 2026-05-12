 package com.lpu.delivery_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lpu.delivery_service.dto.CreateDeliveryRequest;
import com.lpu.delivery_service.dto.DeliveryResponse;
import com.lpu.delivery_service.entity.Delivery;
import com.lpu.delivery_service.entity.DeliveryStatus;
import com.lpu.delivery_service.repository.DeliveryRepository;

import io.micrometer.tracing.Tracer;

@Service
// DeliveryService contains the core business logic for managing deliveries, including price calculation, lifecycle management, and integration with RabbitMQ for tracking updates.
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Tracer tracer;

    // ----------------------------------------------------------------
    // Price Calculation
    // ----------------------------------------------------------------
    /**
     * Pricing model:
     *   NATIONAL  + STANDARD : base=100,  ₹50/kg,  ₹2/km
     *   NATIONAL  + EXPRESS  : base=100,  ₹80/kg,  ₹3/km
     *   INTERNATIONAL + STANDARD : base=1500, ₹200/kg, ₹1/km
     *   INTERNATIONAL + EXPRESS  : base=1500, ₹300/kg, ₹1.5/km
     */
    // calculatePrice computes the delivery price based on weight, distance, delivery type, and service type, applying different rates for national vs international and standard vs express services.
    private double calculatePrice(double weightKg, double distanceKm,
                                   String deliveryType, String serviceType) {
        boolean isIntl    = "INTERNATIONAL".equalsIgnoreCase(deliveryType);
        boolean isExpress = "EXPRESS".equalsIgnoreCase(serviceType);

        double baseFee, ratePerKg, ratePerKm;

        if (isIntl) {
            baseFee   = 1500.0;
            ratePerKg = isExpress ? 300.0 : 200.0;
            ratePerKm = isExpress ? 1.5   : 1.0;
        } else {
            baseFee   = 100.0;
            ratePerKg = isExpress ? 80.0  : 50.0;
            ratePerKm = isExpress ? 3.0   : 2.0;
        }

        double raw = baseFee + (weightKg * ratePerKg) + (distanceKm * ratePerKm);
        return Math.round(raw * 100.0) / 100.0;
    }

    // ----------------------------------------------------------------
    // Create Delivery
    // ----------------------------------------------------------------
    // createDelivery handles the creation of a new delivery, including price calculation, saving to the database, and publishing an initial tracking event to RabbitMQ.
    public DeliveryResponse createDelivery(CreateDeliveryRequest request, String email) {

        // Safe defaults for package fields
        // If weight or distance are not provided or invalid, we assign reasonable defaults to ensure price calculation can proceed without errors.
        double weightKg    = request.getWeightKg()    > 0 ? request.getWeightKg()    : 1.0;
        double distanceKm  = request.getDistanceKm()  > 0 ? request.getDistanceKm()  : 100.0;
        String deliveryType = request.getDeliveryType() != null ? request.getDeliveryType() : "NATIONAL";
        String serviceType  = request.getServiceType()  != null ? request.getServiceType()  : "STANDARD";

        double price = calculatePrice(weightKg, distanceKm, deliveryType, serviceType);

        Delivery delivery = new Delivery();
        delivery.setSenderAddress(request.getSenderAddress());
        delivery.setReceiverAddress(request.getReceiverAddress());
        // Associate the delivery with the customer's email for later retrieval and access control.
        delivery.setCustomerEmail(email);   // Email comes from gateway header.
        delivery.setStatus(DeliveryStatus.BOOKED);  // Initial status is BOOKED.
        delivery.setTrackingNumber(UUID.randomUUID().toString());   // Tracking number is generated using UUID.
        delivery.setCreatedAt(LocalDateTime.now());   // Current time is stored.

        // Package & pricing
        delivery.setWeightKg((double) weightKg);
        delivery.setDistanceKm((double) distanceKm);
        delivery.setDeliveryType(deliveryType);
        delivery.setServiceType(serviceType);
        delivery.setPrice((double) price);

        // Save to DB
        Delivery saved = deliveryRepo.save(delivery);

        // Then it publishes event to RabbitMQ
        // After creating a delivery, Delivery Service sends a message to RabbitMQ 
        // so Tracking Service can create the first tracking event asynchronously.
        String payload = saved.getTrackingNumber() + "|" + DeliveryStatus.BOOKED.name();
        Message msg = MessageBuilder
                .withBody(payload.getBytes())
                .setHeader("traceId", tracer.currentSpan().context().traceId())
                .setHeader("spanId",  tracer.currentSpan().context().spanId())
                .build();
        rabbitTemplate.send("tracking_queue", msg);

        return mapToResponse(saved);
    }

    // ----------------------------------------------------------------
    // Read
    // ----------------------------------------------------------------
    // getMyDeliveries retrieves all deliveries associated with a specific customer's email, 
    // allowing users to view their delivery history and current deliveries.
    public List<Delivery> getMyDeliveries(String email) {
        return deliveryRepo.findByCustomerEmail(email);
    }

    public Delivery getById(Long id) {
        return deliveryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryRepo.findAll();
    }

    // ----------------------------------------------------------------
    // Update Status (with lifecycle validation)
    // ----------------------------------------------------------------
    // updateStatus is responsible for updating the status of a delivery while ensuring that 
    // the transition follows the defined lifecycle rules. 
    // It also publishes a message to RabbitMQ whenever a status change occurs, 
    // allowing other services to react to the update.
    public Delivery updateStatus(Long id, DeliveryStatus newStatus) {

        // it gets current delivery.
        Delivery delivery = getById(id);
        DeliveryStatus current = delivery.getStatus();

        // it validates the status transition based on the defined lifecycle rules.
        if (!isValidTransition(current, newStatus)) {
            throw new RuntimeException("Invalid transition from " + current + " to " + newStatus);
        }

        // it updates the status and saves the delivery.
        delivery.setStatus(newStatus);
        Delivery saved = deliveryRepo.save(delivery);

        // it publishes a message to RabbitMQ with the tracking number and new status, 
        // allowing other services (like Tracking Service) to react to the status change asynchronously.
        String payload = saved.getTrackingNumber() + "|" + newStatus.name();
        Message msg = MessageBuilder
                .withBody(payload.getBytes())
                .setHeader("traceId", tracer.currentSpan().context().traceId())
                .setHeader("spanId",  tracer.currentSpan().context().spanId())
                .build();
        rabbitTemplate.send("tracking_queue", msg);

        return saved;
    }

    // ----------------------------------------------------------------
    // Lifecycle transitions
    // ----------------------------------------------------------------
    // isValidTransition checks if the transition from the current status to the next status is valid according to the defined lifecycle rules.
    private boolean isValidTransition(DeliveryStatus current, DeliveryStatus next) {
        // EXCEPTION can be raised from any active (non-terminal) state
        if (next == DeliveryStatus.EXCEPTION) {
            return current == DeliveryStatus.BOOKED
                || current == DeliveryStatus.PICKED_UP
                || current == DeliveryStatus.IN_TRANSIT
                || current == DeliveryStatus.OUT_FOR_DELIVERY;
        }
        return switch (current) {
            case DRAFT            -> next == DeliveryStatus.BOOKED;
            case BOOKED           -> next == DeliveryStatus.PICKED_UP;
            case PICKED_UP        -> next == DeliveryStatus.IN_TRANSIT;
            case IN_TRANSIT       -> next == DeliveryStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == DeliveryStatus.DELIVERED;
            default               -> false; // DELIVERED and EXCEPTION are terminal
        };
    }

    // ----------------------------------------------------------------
    // Mapper
    // ----------------------------------------------------------------
    private DeliveryResponse mapToResponse(Delivery delivery) {
        DeliveryResponse res = new DeliveryResponse();
        res.setId(delivery.getId());
        res.setStatus(delivery.getStatus());
        res.setTrackingNumber(delivery.getTrackingNumber());
        res.setCreatedAt(delivery.getCreatedAt());
        res.setSenderAddress(delivery.getSenderAddress());
        res.setReceiverAddress(delivery.getReceiverAddress());
        res.setWeightKg(delivery.getWeightKg());
        res.setDistanceKm(delivery.getDistanceKm());
        res.setDeliveryType(delivery.getDeliveryType());
        res.setServiceType(delivery.getServiceType());
        res.setPrice(delivery.getPrice());
        return res;
    }
}