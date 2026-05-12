package com.lpu.delivery_service;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.lpu.delivery_service.repository.DeliveryRepository;

import io.micrometer.tracing.Tracer;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"spring.cloud.config.fail-fast=false",
		"eureka.client.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
})
class DeliveryServiceApplicationTests {

	@MockBean
	private DeliveryRepository deliveryRepository;

	@MockBean
	private RabbitTemplate rabbitTemplate;

	@MockBean
	private Tracer tracer;

	@Test
	void contextLoads() {
	}

}
