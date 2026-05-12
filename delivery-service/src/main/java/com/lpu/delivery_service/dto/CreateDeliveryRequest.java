package com.lpu.delivery_service.dto;

import com.lpu.delivery_service.entity.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
// This class represents the request body for creating a new delivery. It includes sender and receiver addresses, as well as package details for pricing calculations.
// CreateDeliveryRequest is used to receive delivery booking data from the frontend.
public class CreateDeliveryRequest {

    @Valid
    @NotNull(message = "Sender address is required")
    private Address senderAddress;

    @Valid
    @NotNull(message = "Receiver address is required")
    private Address receiverAddress;

    // Package details for pricing
    private double weightKg    = 1.0;
    private double distanceKm  = 100.0;
    private String deliveryType = "NATIONAL";  // NATIONAL or INTERNATIONAL
    private String serviceType  = "STANDARD";  // STANDARD or EXPRESS

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
}
