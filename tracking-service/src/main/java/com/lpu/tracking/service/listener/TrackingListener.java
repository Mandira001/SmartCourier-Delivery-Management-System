package com.lpu.tracking.service.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.lpu.tracking.service.entity.*;
import com.lpu.tracking.service.repository.TrackingRepository;

import java.time.LocalDateTime;

@Component
public class TrackingListener {

    @Autowired
    private TrackingRepository repo;

    @RabbitListener(queues = "tracking_queue")
    public void consume(String message) {

        try {
            String[] parts = message.split("\\|");

            String trackingNumber = parts[0];
            TrackingStatus status = TrackingStatus.valueOf(parts[1]);

            TrackingEvent event = new TrackingEvent();
            event.setTrackingNumber(trackingNumber);
            event.setStatus(status);
            event.setLocation("AUTO UPDATE");
            event.setRemarks("Updated via Delivery Service");
            event.setTimestamp(LocalDateTime.now());

            repo.save(event);

            System.out.println("Tracking updated via RabbitMQ");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
