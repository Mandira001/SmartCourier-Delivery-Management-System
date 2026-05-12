package com.lpu.tracking.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.lpu.tracking.service.repository.DeliveryProofRepository;
import com.lpu.tracking.service.repository.DocumentRepository;
import com.lpu.tracking.service.repository.TrackingRepository;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"spring.cloud.config.fail-fast=false",
		"eureka.client.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class TrackingServiceApplicationTests {

	@MockBean
	private TrackingRepository trackingRepository;

	@MockBean
	private DocumentRepository documentRepository;

	@MockBean
	private DeliveryProofRepository deliveryProofRepository;

	@Test
	void contextLoads() {
	}

}
