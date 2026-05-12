package com.lpu.api_gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;

@Component
public class GatewayJwtFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // This logger is used to log user information for debugging and monitoring purposes. 
    // It logs the email and role of the user extracted from the JWT, 
    // which can help in tracing requests and understanding user behavior across services.
    private static final Logger log = LoggerFactory.getLogger(GatewayJwtFilter.class);

    @Override
    // This is the core method of the filter. It intercepts every request passing through the gateway, 
    // checks for a valid JWT, and extracts user information if the token is valid. 
    // If the token is missing or invalid, it responds with an unauthorized status.
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
            return chain.filter(exchange);
        }

        // Public APIs
        // This makes auth endpoints public, because users must be able to login/signup before they have a token.
        if (path.startsWith("/gateway/auth")) {
            return chain.filter(exchange);
        }

        // This reads the JWT from request headers.
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        // This checks if the Authorization header is present and starts with "Bearer ". 
        // If not, it means the request is unauthorized, 
        // and the filter responds with a 401 status code without forwarding the request to downstream services.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // This extracts the actual JWT token by removing the "Bearer " prefix from the Authorization header.
        String token = authHeader.substring(7);

        try {
            // This extracts identity from the token.
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            // This logs the user's email and role for debugging and monitoring purposes.
            log.info("User: {} | Role: {}", email, role);

            // PASS DATA TO SERVICES
            exchange = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        // The gateway does not just validate the token. It also passes user information to other services. So Delivery, Tracking, and Admin services can know who is calling without decoding the JWT again.
                        headers.add("X-User-Email", email);
                        headers.add("X-User-Role", role);
                    }))
                    .build();

        } catch (Exception e) {
            // If any exception occurs during token validation (e.g., token is invalid, expired, or signature does not match), the filter catches the exception, logs the stack trace for debugging, and responds with a 401 Unauthorized status code.
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
