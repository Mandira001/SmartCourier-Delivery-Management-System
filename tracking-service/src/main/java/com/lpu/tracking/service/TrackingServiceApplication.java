package com.lpu.tracking.service;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableRabbit
@SpringBootApplication
@EnableDiscoveryClient
public class TrackingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackingServiceApplication.class, args);
	}

}
