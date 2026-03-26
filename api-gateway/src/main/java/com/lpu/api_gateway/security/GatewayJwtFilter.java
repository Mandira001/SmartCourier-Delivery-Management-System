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

    private static final Logger log = LoggerFactory.getLogger(GatewayJwtFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
            return chain.filter(exchange);
        }

        // Public APIs
        if (path.startsWith("/gateway/auth")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            log.info("User: {} | Role: {}", email, role);

            // PASS DATA TO SERVICES
            exchange = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.add("X-User-Email", email);
                        headers.add("X-User-Role", role);
                    }))
                    .build();

        } catch (Exception e) {
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
