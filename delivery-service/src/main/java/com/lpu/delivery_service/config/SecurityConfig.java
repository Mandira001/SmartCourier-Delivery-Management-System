package com.lpu.delivery_service.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.lpu.delivery_service.security.JwtHeaderFilter;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
// this enables @PreAuthorize annotations on methods for role-based access control, 
// allowing us to secure specific endpoints based on user roles or permissions.
@EnableMethodSecurity
public class SecurityConfig {
	
	@Bean
        // corsConfigurationSource() defines a CORS configuration that allows requests from any origin, 
        // with any method and header. This is important for enabling cross-origin requests to the Delivery Service API, 
        // especially when the frontend and backend are hosted on different domains or ports.
	public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {

	    org.springframework.web.cors.CorsConfiguration config =
	            new org.springframework.web.cors.CorsConfiguration();

	    config.setAllowedOrigins(java.util.List.of("*"));   // allow all origins
	    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	    config.setAllowedHeaders(java.util.List.of("*"));
	    config.setAllowCredentials(false);

	    org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
	            new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

	    source.registerCorsConfiguration("/**", config);

	    return source;
	}

    @Bean
    // securityFilterChain configures the security filter chain for the application.
    // It disables CSRF protection (since we're likely using JWTs), sets up CORS with the defined configuration,
    // allows unauthenticated access to Swagger UI endpoints, and adds a custom JWT header filter
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                		//Allow swagger
                		.requestMatchers(
                				"/swagger-ui.html",
                				"/swagger-ui/**",
                				"/v3/api-docs/**"
                				).permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtHeaderFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    
    @Bean
    // customOpenAPI configures the OpenAPI documentation for the API, including security schemes and server information.
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
        // it adds a security requirement that specifies that the API uses bearer token authentication (JWT) for securing endpoints.
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                // it defines a security scheme named "bearerAuth" that uses HTTP bearer token authentication with JWT format, 
                                // which will be used in the API documentation to indicate that endpoints require JWT authentication.
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                        // it also defines a server with the URL "/gateway/deliveries", which indicates that the API is accessible through this base path
                .servers(List.of(new Server().url("/gateway/deliveries")));
    }
}
