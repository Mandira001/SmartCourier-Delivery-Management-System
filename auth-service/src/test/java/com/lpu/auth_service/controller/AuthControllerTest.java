package com.lpu.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpu.auth_service.dto.LoginRequest;
import com.lpu.auth_service.dto.SignupRequest;
import com.lpu.auth_service.service.AuthService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import com.lpu.auth_service.security.SecurityConfig;
import com.lpu.auth_service.security.JwtUtil;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.config.fail-fast=false",
                "eureka.client.enabled=false"
        }
)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // Signup Test
    @Test
    void testSignup() throws Exception {

        SignupRequest request = new SignupRequest();
        request.setName("Test");
        request.setEmail("test@mail.com");
        request.setPassword("123");
        request.setRole("USER");

        Mockito.when(authService.signup(Mockito.any()))
                .thenReturn("User registered successfully");

        mockMvc.perform(post("/auth/signup")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    // Login Test
    @Test
    void testLogin() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("123");

        Mockito.when(authService.login(Mockito.any()))
                .thenReturn("token123");

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("token123"));
    }
}
