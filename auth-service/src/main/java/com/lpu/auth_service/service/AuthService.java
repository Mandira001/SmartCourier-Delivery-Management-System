package com.lpu.auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lpu.auth_service.dto.LoginRequest;
import com.lpu.auth_service.dto.SignupRequest;
import com.lpu.auth_service.entity.User;
import com.lpu.auth_service.repository.UserRepository;
import com.lpu.auth_service.security.JwtUtil;

@Service
public class AuthService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	public String signup(SignupRequest request) {
		
		//checks is user exists
		if(userRepo.findByEmail(request.getEmail()).isPresent()) {
			return "User already exists";
		}
		
		//create user
		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		// SAFE ROLE ASSIGNMENT
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {

            // Check secret key
            if ("SECRET123".equals(request.getAdminKey())) {
                user.setRole("ADMIN");
            } else {
                throw new RuntimeException("Invalid admin key");
            }

        } else {
            user.setRole("USER");
        }
		
		userRepo.save(user);
		
		return "User registered successfully";
	}
	
	public String login(LoginRequest request) {
		User user = userRepo.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new RuntimeException("Invalid password");
		}
		
		String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // STORE TOKEN IN REDIS
        redisTemplate.opsForValue().set(
        		user.getEmail(), 
        		token, 
        		java.time.Duration.ofHours(1));     //match JWT expiry
		
		return token;
	}
}
