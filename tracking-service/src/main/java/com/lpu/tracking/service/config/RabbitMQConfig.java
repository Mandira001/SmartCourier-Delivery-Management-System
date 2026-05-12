package com.lpu.tracking.service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Configuration class for RabbitMQ, defining the queue used for tracking events in the application.
@Configuration
public class RabbitMQConfig {
	public static final String QUEUE = "tracking_queue";
// Defines a bean for the RabbitMQ queue, which will be used to send and receive tracking event messages in the application.
    @Bean
    public Queue queue() {
        return new Queue(QUEUE);
    }
}
