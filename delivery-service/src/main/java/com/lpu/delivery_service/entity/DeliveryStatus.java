package com.lpu.delivery_service.entity;

// DeliveryStatus defines the lifecycle stages of a courier delivery.
public enum DeliveryStatus {

    DRAFT,
    BOOKED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION       // Package lost, damaged, or undeliverable
}