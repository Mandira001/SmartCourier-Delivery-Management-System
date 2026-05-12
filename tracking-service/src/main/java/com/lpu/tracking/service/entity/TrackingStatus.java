package com.lpu.tracking.service.entity;
// This is an enum that defines the various statuses that a tracking event can have in the system.
public enum TrackingStatus {
	BOOKED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION
}
