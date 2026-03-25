package com.lpu.tracking.service.dto;

import com.lpu.tracking.service.entity.TrackingStatus;

public class TrackingEventRequest {
	private String trackingNumber;
    private TrackingStatus status;
    private String location;
    private String remarks;
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
	public TrackingEventRequest(String trackingNumber, TrackingStatus status, String location, String remarks) {
		super();
		this.trackingNumber = trackingNumber;
		this.status = status;
		this.location = location;
		this.remarks = remarks;
	}
	public TrackingEventRequest() {
		super();
	}
}
