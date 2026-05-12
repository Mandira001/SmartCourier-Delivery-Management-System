package com.lpu.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"spring.cloud.config.fail-fast=false",
		"eureka.client.enabled=false",
		"spring.cloud.gateway.default-filters="
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
