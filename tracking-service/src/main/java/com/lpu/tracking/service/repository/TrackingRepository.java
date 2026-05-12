package com.lpu.tracking.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lpu.tracking.service.entity.TrackingEvent;
// Repository interface for tracking events, providing methods to retrieve tracking information based on tracking number and timestamp.
public interface TrackingRepository extends JpaRepository<TrackingEvent, Long>{
	List<TrackingEvent> findByTrackingNumberOrderByTimestampAsc(String trackingNumber);
}
