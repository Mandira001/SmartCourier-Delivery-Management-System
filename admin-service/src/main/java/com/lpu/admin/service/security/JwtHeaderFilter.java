package com.lpu.admin.service.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
// JwtHeaderFilter is a custom filter that extends OncePerRequestFilter to ensure it runs once per request.
public class JwtHeaderFilter extends OncePerRequestFilter {

    @Override
    // doFilterInternal is the method that processes each incoming HTTP request. 
    // It checks for the presence of custom headers (X-User-Email and X-User-Role) 
    // that contain user information. If these headers are present, it creates an 
    // authentication token with the user's email and role, and sets this authentication 
    // in the SecurityContext, allowing downstream code to access the authenticated user's details.
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");

        if (email != null && role != null) {

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
