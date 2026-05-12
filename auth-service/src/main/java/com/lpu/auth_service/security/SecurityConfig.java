package com.lpu.auth_service.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SecurityConfig {

//    private final PasswordEncoder passwordEncoder;
	@Autowired
	private JwtFilter jwtFilter;

//    SecurityConfig(PasswordEncoder passwordEncoder) {
//        this.passwordEncoder = passwordEncoder;
//    }
	
	@Bean
	// This method configures CORS to allow requests from any origin, which is necessary for the frontend application 
	// to communicate with the auth service without being blocked by the browser's same-origin policy.
	public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {

	    org.springframework.web.cors.CorsConfiguration config =
	            new org.springframework.web.cors.CorsConfiguration();

		// Allow all origins, methods, and headers for CORS
	    config.setAllowedOrigins(java.util.List.of("*"));   // allow all origins
	    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	    config.setAllowedHeaders(java.util.List.of("*"));
	    config.setAllowCredentials(false);

		// Register the CORS configuration for all paths
	    org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
	            new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
		// This line registers the CORS configuration for all paths (/**) using the config object defined above. 
	    source.registerCorsConfiguration("/**", config);

	    return source;
	}
	
	@Bean
	// This method configures the security filter chain for the application. 
	// It disables CSRF protection (since we're using JWTs), enables CORS with the configuration defined above, 
	// and sets up authorization rules to allow unauthenticated access to the /auth/** endpoints 
	// while securing all other endpoints. 
	// It also adds the JwtFilter to validate JWT tokens on incoming requests.
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		    .csrf(csrf -> csrf.disable()) // disable CSRF for now
		    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
		    .authorizeHttpRequests(auth -> auth
		    		//Allow swagger
		    		.requestMatchers(
		                    "/swagger-ui.html",
		                    "/swagger-ui/**",
		                    "/v3/api-docs/**"
		                ).permitAll()
		    		.requestMatchers("/auth/**").permitAll() //allow signup and login
		    		.anyRequest().authenticated()  //secure other APIs
		    		)
		    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); //JWT filter
//		    .httpBasic(httpBasic -> httpBasic.disable()) // disable basic auth
//            .formLogin(form -> form.disable()); // disable login page
		return http.build();
	}
	
	// This bean defines the password encoder to be used for hashing user passwords. 
	// BCrypt is a strong hashing algorithm that is widely used for password hashing in Java applications.
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// This bean configures the OpenAPI documentation for the application. 
	// It sets the server URL to /gateway/auth, which is the endpoint 
	// through which the frontend will access the auth service via the API Gateway.
	@Bean
	public OpenAPI customOpenAPI() {
	    return new OpenAPI()
	            .servers(List.of(
	                    new Server().url("/gateway/auth")
	            ));
	}
	
}
