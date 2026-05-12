package com.lpu.delivery_service.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// JwtHeaderFilter is a custom filter that extracts user information from HTTP headers 
// and sets the authentication context for the request, allowing the application 
// to identify the user and their role based on the provided headers.
public class JwtHeaderFilter extends OncePerRequestFilter {

    @Override
    // doFilterInternal checks for the presence of "X-User-Email" and "X-User-Role" headers in the incoming HTTP request.
    // If both headers are present, it creates an authentication token with the user's email and role,
    // and sets it in the SecurityContext, allowing the application to recognize the user for authorization purposes.
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
