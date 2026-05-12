package com.lpu.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
// This controller handles fallback responses for the API Gateway. 
// When a downstream service is unavailable or an error occurs while routing a request, 
// the gateway can redirect to this fallback endpoint to provide a consistent error response to the client. 
// The fallback method constructs a response with a 503 Service Unavailable status and a message indicating that 
// the requested service is currently unavailable, which helps improve the user experience during service outages.
public class FallbackController {

    @RequestMapping("/fallback")
    public Mono<ResponseEntity<Map<String, Object>>> fallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "The requested service is currently unavailable. Please try again later.");
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}
