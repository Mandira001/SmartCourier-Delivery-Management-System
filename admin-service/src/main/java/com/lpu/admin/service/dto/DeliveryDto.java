package com.lpu.admin.service.dto;
// DeliveryDto is a Data Transfer Object (DTO) that represents 
// the structure of delivery data as received from the Delivery Service.
// Why needed? Because Admin Service should not directly depend on 
// Delivery Service entity class. It uses its own DTO.

public class DeliveryDto {

    private Long id;
    private String status;
    private String trackingNumber;
    private String customerEmail;

    // Package & pricing fields (mirrored from delivery-service)
    private double weightKg;
    private double distanceKm;
    private String deliveryType;
    private String serviceType;
    private double price;

    // createdAt as String (delivery-service serialises LocalDateTime as ISO string)
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
