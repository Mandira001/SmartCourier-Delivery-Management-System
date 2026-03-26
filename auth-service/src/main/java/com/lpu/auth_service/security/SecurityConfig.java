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
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public OpenAPI customOpenAPI() {
	    return new OpenAPI()
	            .servers(List.of(
	                    new Server().url("/gateway/auth")
	            ));
	}
	
}
