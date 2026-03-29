package com.lpu.delivery_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import com.lpu.delivery_service.dto.CreateDeliveryRequest;
import com.lpu.delivery_service.dto.DeliveryResponse;
import com.lpu.delivery_service.entity.Delivery;
import com.lpu.delivery_service.entity.DeliveryStatus;
import com.lpu.delivery_service.repository.DeliveryRepository;

import io.micrometer.tracing.Tracer;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepo;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private Tracer tracer;

    // Create Delivery
    public DeliveryResponse createDelivery(CreateDeliveryRequest request, String email) {

        Delivery delivery = new Delivery();

        delivery.setSenderAddress(request.getSenderAddress());
        delivery.setReceiverAddress(request.getReceiverAddress());

        delivery.setCustomerEmail(email);
        delivery.setStatus(DeliveryStatus.BOOKED);
        delivery.setTrackingNumber(UUID.randomUUID().toString());
        delivery.setCreatedAt(LocalDateTime.now());

        Delivery saved = deliveryRepo.save(delivery);

        return mapToResponse(saved);
    }

    // Get My Deliveries
    public List<Delivery> getMyDeliveries(String email) {
        return deliveryRepo.findByCustomerEmail(email);
    }

    // Get by ID
    public Delivery getById(Long id) {
        return deliveryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
    }
    
    public List<Delivery> getAllDeliveries() {
        return deliveryRepo.findAll();
    }

    // Update Status with validation
    public Delivery updateStatus(Long id, DeliveryStatus newStatus) {

        Delivery delivery = getById(id);
        DeliveryStatus current = delivery.getStatus();

        if (!isValidTransition(current, newStatus)) {
            throw new RuntimeException("Invalid transition from " + current + " to " + newStatus);
        }

        delivery.setStatus(newStatus);
        Delivery saved = deliveryRepo.save(delivery);

        // 🔥 SEND EVENT TO RABBITMQ WITH TRACE
        String payload = saved.getTrackingNumber() + "|" + newStatus.name();

        Message msg = MessageBuilder
                .withBody(payload.getBytes())
                .setHeader("traceId", tracer.currentSpan().context().traceId())
                .setHeader("spanId", tracer.currentSpan().context().spanId())
                .build();

        rabbitTemplate.send("tracking_queue", msg);

        return saved;
    }

    // Lifecycle Logic
    private boolean isValidTransition(DeliveryStatus current, DeliveryStatus next) {

        return switch (current) {

            case DRAFT -> next == DeliveryStatus.BOOKED;

            case BOOKED -> next == DeliveryStatus.PICKED_UP;

            case PICKED_UP -> next == DeliveryStatus.IN_TRANSIT;

            case IN_TRANSIT -> next == DeliveryStatus.OUT_FOR_DELIVERY;

            case OUT_FOR_DELIVERY -> next == DeliveryStatus.DELIVERED;

            default -> false;
        };
    }

    // Mapping
    private DeliveryResponse mapToResponse(Delivery delivery) {

        DeliveryResponse res = new DeliveryResponse();

        res.setId(delivery.getId());
        res.setStatus(delivery.getStatus());
        res.setTrackingNumber(delivery.getTrackingNumber());
        res.setCreatedAt(delivery.getCreatedAt());
        res.setSenderAddress(delivery.getSenderAddress());
        res.setReceiverAddress(delivery.getReceiverAddress());

        return res;
    }
}