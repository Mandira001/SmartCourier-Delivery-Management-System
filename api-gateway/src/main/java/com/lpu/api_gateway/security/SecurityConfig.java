package com.lpu.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
	// This configuration class defines the security settings for the API Gateway. 
	// It disables CSRF protection, HTTP Basic authentication, and form login, 
	// which are not needed for a stateless API gateway that relies on JWT for authentication. 
	// The authorizeExchange configuration allows all requests to pass through without authentication at this level, 
	// as the actual authentication is handled by the GatewayJwtFilter. 
	// This setup ensures that the gateway can validate JWT tokens and extract user information 
	// without being blocked by Spring Security's default authentication mechanisms.
	 @Bean
	 public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		 return http
	             .csrf(csrf -> csrf.disable())
	             .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) 
	             .formLogin(ServerHttpSecurity.FormLoginSpec::disable) 
	             .authorizeExchange(exchange -> exchange
	                    .anyExchange().permitAll()
	             )
	             .build();
		 }
}
