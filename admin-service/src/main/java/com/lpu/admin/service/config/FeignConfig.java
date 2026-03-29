package com.lpu.admin.service.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    private final Tracer tracer;

    public FeignConfig(Tracer tracer) {
        this.tracer = tracer;
    }

    @Bean
    public RequestInterceptor feignAuthInterceptor() {
        return template -> {

            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();

                // ✅ Forward Authorization
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    template.header("Authorization", authHeader);
                }

                // ✅ ADD THIS (MOST IMPORTANT FIX)
                String email = request.getHeader("X-User-Email");
                String role = request.getHeader("X-User-Role");

                if (email != null) {
                    template.header("X-User-Email", email);
                }

                if (role != null) {
                    template.header("X-User-Role", role);
                }
            }

            // ✅ Tracing (Zipkin)
            if (tracer.currentSpan() != null) {
                var context = tracer.currentSpan().context();

                template.header("X-B3-TraceId", context.traceId());
                template.header("X-B3-SpanId", context.spanId());

                if (context.parentId() != null) {
                    template.header("X-B3-ParentSpanId", context.parentId());
                }

                template.header("X-B3-Sampled", "1");
            }
        };
    }
}