package com.lpu.delivery_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
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
    private DeliveryStatus status;  // Draft, Booked, In Transit, Delivered

    private String customerEmail;

    private String trackingNumber;

    private LocalDateTime createdAt;

    // Constructors
    public Delivery() {}

    // Getters & Setters
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
}
