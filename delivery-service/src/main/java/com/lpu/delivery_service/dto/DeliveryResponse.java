package com.lpu.delivery_service.dto;

import java.time.LocalDateTime;
import com.lpu.delivery_service.entity.Address;
import com.lpu.delivery_service.entity.DeliveryStatus;
// This class represents the response body for delivery-related operations. It includes delivery details, addresses, package information, and pricing for client consumption.
// DeliveryResponse is used to send clean delivery details back to the client after booking.
public class DeliveryResponse {

    private Long id;
    private DeliveryStatus status;
    private String trackingNumber;
    private LocalDateTime createdAt;
    private Address senderAddress;
    private Address receiverAddress;

    // Package & pricing fields
    private double weightKg;
    private double distanceKm;
    private String deliveryType;
    private String serviceType;
    private double price;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Address getSenderAddress() { return senderAddress; }
    public void setSenderAddress(Address senderAddress) { this.senderAddress = senderAddress; }

    public Address getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(Address receiverAddress) { this.receiverAddress = receiverAddress; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
