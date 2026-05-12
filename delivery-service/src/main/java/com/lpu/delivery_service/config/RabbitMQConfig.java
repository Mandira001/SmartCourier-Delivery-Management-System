package com.lpu.delivery_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Creates a RabbitMQ queue named "tracking_queue" that will be used for sending tracking updates from the Delivery Service to the Tracking Service.
	public static final String QUEUE = "tracking_queue";

    // queue() defines a bean for the RabbitMQ queue, allowing Spring to manage it and enabling the Delivery Service 
    // to send messages to this queue whenever there are updates to delivery status.
    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }
}
