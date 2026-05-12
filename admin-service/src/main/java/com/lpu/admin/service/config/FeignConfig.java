package com.lpu.admin.service.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
// FeignConfig is a configuration class that defines a Feign RequestInterceptor bean.
// The interceptor is responsible for forwarding the Authorization header and user information 
// (email and role) from the incoming HTTP request to the outgoing Feign requests made by the 
// Admin Service to the Delivery Service. This ensures that the Delivery Service receives the 
// necessary authentication and user context for processing the requests. Additionally, the 
// interceptor also adds tracing headers for distributed tracing with Zipkin.
@Configuration
public class FeignConfig {

    private final Tracer tracer;

    public FeignConfig(Tracer tracer) {
        this.tracer = tracer;
    }

    @Bean
    public RequestInterceptor feignAuthInterceptor() {
        return template -> {

            // Get the current HTTP request attributes to access headers from the incoming request
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                    // If the current request attributes are available, extract the Authorization header and user info (email & role)
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();

                // Forward Authorization
                // Check if the incoming HTTP request has an Authorization header, 
                // and if it does, add that header to the outgoing Feign request template 
                // so that the Delivery Service can authenticate the request.
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    template.header("Authorization", authHeader);
                }

                // Forward user info (email & role)
                String email = request.getHeader("X-User-Email");
                String role = request.getHeader("X-User-Role");

                if (email != null) {
                    template.header("X-User-Email", email);
                }

                if (role != null) {
                    template.header("X-User-Role", role);
                }
            }

            // Tracing (Zipkin)
            // Add B3 tracing headers to the outgoing request if a current span exists in the tracer.
            if (tracer.currentSpan() != null) {
                var context = tracer.currentSpan().context();

                // Add B3 tracing headers to the outgoing request
                template.header("X-B3-TraceId", context.traceId());
                template.header("X-B3-SpanId", context.spanId());

                // If there's a parent span, include the ParentSpanId header
                if (context.parentId() != null) {
                    // Note: parentId can be null for root spans, so we check before adding the header
                    template.header("X-B3-ParentSpanId", context.parentId());
                }

                // Optionally, you can also add the Sampled header if your tracing setup uses it
                template.header("X-B3-Sampled", "1");
            }
        };
    }
}