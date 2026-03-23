package com.lpu.auth_service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

//    private final PasswordEncoder passwordEncoder;
	@Autowired
	private JwtFilter jwtFilter;

//    SecurityConfig(PasswordEncoder passwordEncoder) {
//        this.passwordEncoder = passwordEncoder;
//    }
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		    .csrf(csrf -> csrf.disable()) // disable CSRF for now
		    .cors(cors -> {})   //enable default CORS
		    .authorizeHttpRequests(auth -> auth
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
}
