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
// This service class contains the business logic for handling user authentication, including signup and login.
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
	// This method handles the user signup process. 
	// It checks if the user already exists, creates a new user with the provided details, and saves it to the database.
	public String signup(SignupRequest request) {
		
		//checks if user exists
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
	// The login method authenticates the user by checking the email and password. 
	// If authentication is successful, it generates a JWT token and stores it in Redis with an expiration time matching the JWT's expiry.
	public String login(LoginRequest request) {
		User user = userRepo.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));
		// CHECK PASSWORD
		if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new RuntimeException("Invalid password");
		}
		// Generate JWT token
		String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // STORE TOKEN IN REDIS
        redisTemplate.opsForValue().set(
        		user.getEmail(), 
        		token, 
        		java.time.Duration.ofHours(1));     //match JWT expiry
		
		return token;
	}
}
