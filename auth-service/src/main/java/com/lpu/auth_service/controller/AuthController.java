package com.lpu.auth_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lpu.auth_service.dto.LoginRequest;
import com.lpu.auth_service.dto.SignupRequest;
import com.lpu.auth_service.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	private AuthService authService;
	
	@GetMapping("/test")
	public String test() {
	    return "SECURED API WORKING";
	}
	
	@PostMapping("/signup")
	public String signup(@RequestBody SignupRequest request) {
		return authService.signup(request);
	}
	
	@PostMapping("/login")
	public String login(@RequestBody LoginRequest request) {
		System.out.println("LOGIN API HIT");
		return authService.login(request);
	}
}
