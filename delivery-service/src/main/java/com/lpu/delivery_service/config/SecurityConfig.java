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
@EnableMethodSecurity
public class SecurityConfig {
	
	@Bean
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
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .servers(List.of(new Server().url("/gateway/deliveries")));
    }
}
