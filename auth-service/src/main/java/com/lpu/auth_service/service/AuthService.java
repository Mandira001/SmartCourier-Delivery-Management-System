package com.lpu.auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
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
		user.setRole("USER");
		
		userRepo.save(user);
		
		return "User registered successfully";
	}
	
	public String login(LoginRequest request) {
		User user = userRepo.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new RuntimeException("Invalid password");
		}
		
		return jwtUtil.generateToken(user.getEmail());
	}
}
