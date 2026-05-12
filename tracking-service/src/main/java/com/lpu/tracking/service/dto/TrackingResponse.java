package com.lpu.tracking.service.dto;

import java.time.LocalDateTime;

import com.lpu.tracking.service.entity.TrackingStatus;
// DTO for tracking response, containing status, location, remarks, and timestamp to provide tracking information to the client.
public class TrackingResponse {
	private TrackingStatus status;
    private String location;
    private String remarks;
    private LocalDateTime timestamp;
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
	public TrackingResponse(TrackingStatus status, String location, String remarks, LocalDateTime timestamp) {
		super();
		this.status = status;
		this.location = location;
		this.remarks = remarks;
		this.timestamp = timestamp;
	}
	public TrackingResponse() {
		super();
	}
}
