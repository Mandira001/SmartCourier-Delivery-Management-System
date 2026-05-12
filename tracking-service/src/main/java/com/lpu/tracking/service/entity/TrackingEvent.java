package com.lpu.tracking.service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
// This is a JPA entity class that represents a tracking event in the system. 
// It has fields for tracking number, status, location, remarks, and timestamp.
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private TrackingStatus status;

    private String location;
    private String remarks;

    private LocalDateTime timestamp;

    @PrePersist
    public void setTimestamp() {
        this.timestamp = LocalDateTime.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public TrackingStatus getStatus() {
		return status;
	}

	public void setStatus(TrackingStatus status) {
		this.status = status;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public TrackingEvent() {
		super();
	}
}
