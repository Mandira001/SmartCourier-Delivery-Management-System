package com.lpu.tracking.service.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.core.Message;

import com.lpu.tracking.service.entity.*;
import com.lpu.tracking.service.repository.TrackingRepository;

import java.time.LocalDateTime;

@Component
public class TrackingListener {

    @Autowired
    private TrackingRepository repo;
    // It listens to RabbitMQ queue named "tracking_queue". 
    // When a message arrives, it processes the message to extract 
    // tracking information and saves a new tracking event to the database.
    @RabbitListener(queues = "tracking_queue")
    public void consume(Message message) {

        try {
            // GET PAYLOAD
            // The message body is expected to be a string in the format "trackingNumber|status".
            String payload = new String(message.getBody());

            // GET TRACE HEADERS
            // It also retrieves traceId and spanId from message headers for logging purposes, 
            // which can be useful for distributed tracing in microservices.
            String traceId = (String) message.getMessageProperties().getHeaders().get("traceId");
            String spanId = (String) message.getMessageProperties().getHeaders().get("spanId");

            System.out.println("TRACE RECEIVED: " + traceId);
            System.out.println("SPAN RECEIVED: " + spanId);

            // PARSE MESSAGE
            // The payload is split to extract the tracking number and status, 
            // which are then used to create a new tracking event.
            String[] parts = payload.split("\\|");

            // Extract tracking number and status from the message payload
            String trackingNumber = parts[0];
            TrackingStatus status = TrackingStatus.valueOf(parts[1]);

            // SAVE EVENT
            // A new TrackingEvent entity is created with the extracted information 
            // and saved to the database.
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
