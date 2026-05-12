package com.lpu.delivery_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
// Delivery is the main entity that stores courier booking details, customer email, status, tracking number, address, package, and pricing information.
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sender_address_id")
    private Address senderAddress;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "receiver_address_id")
    private Address receiverAddress;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private String customerEmail;
    private String trackingNumber;
    private LocalDateTime createdAt;

    // Package & pricing fields (nullable wrappers so Hibernate adds them as nullable columns)
    private Double weightKg;
    private Double distanceKm;
    private String deliveryType;  // NATIONAL or INTERNATIONAL
    private String serviceType;   // STANDARD or EXPRESS
    private Double price;         // calculated at booking time

    public Delivery() {}

    // --- Getters & Setters ---

    public Long getId() { return id; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public Address getSenderAddress() { return senderAddress; }
    public void setSenderAddress(Address senderAddress) { this.senderAddress = senderAddress; }

    public Address getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(Address receiverAddress) { this.receiverAddress = receiverAddress; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Double getWeightKg() { return weightKg != null ? weightKg : 0.0; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Double getDistanceKm() { return distanceKm != null ? distanceKm : 0.0; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public Double getPrice() { return price != null ? price : 0.0; }
    public void setPrice(Double price) { this.price = price; }
}
