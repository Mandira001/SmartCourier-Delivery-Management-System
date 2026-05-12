package com.lpu.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    // This configuration class defines a bean for WebClient.Builder, 
    // which is a reactive HTTP client used to make non-blocking HTTP requests to downstream services.
	@Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
