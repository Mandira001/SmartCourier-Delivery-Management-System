package com.lpu.auth_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.lpu.auth_service.dto.LoginRequest;
import com.lpu.auth_service.dto.SignupRequest;
import com.lpu.auth_service.entity.User;
import com.lpu.auth_service.repository.UserRepository;
import com.lpu.auth_service.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Signup Success
    @Test
    void testSignup_Success() {

        SignupRequest request = new SignupRequest();
        request.setName("Test");
        request.setEmail("test@mail.com");
        request.setPassword("123");
        request.setRole("USER");

        when(userRepo.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("123"))
                .thenReturn("encoded");

        String result = authService.signup(request);

        assertEquals("User registered successfully", result);
        verify(userRepo, times(1)).save(any(User.class));
    }

    // Signup Existing User
    @Test
    void testSignup_UserExists() {

        SignupRequest request = new SignupRequest();
        request.setEmail("test@mail.com");

        when(userRepo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(new User()));

        String result = authService.signup(request);

        assertEquals("User already exists", result);
    }

    // Signup Admin Invalid Key
    @Test
    void testSignup_AdminInvalidKey() {

        SignupRequest request = new SignupRequest();
        request.setEmail("admin@mail.com");
        request.setPassword("123");
        request.setRole("ADMIN");
        request.setAdminKey("WRONG");

        when(userRepo.findByEmail("admin@mail.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            authService.signup(request);
        });

        assertEquals("Invalid admin key", ex.getMessage());
    }

    // Login Success
    @Test
    void testLogin_Success() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("123");

        User user = new User();
        user.setEmail("test@mail.com");
        user.setPassword("encoded");
        user.setRole("USER");

        when(userRepo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123", "encoded"))
                .thenReturn(true);

        when(jwtUtil.generateToken("test@mail.com", "USER"))
                .thenReturn("token123");

        String result = authService.login(request);

        assertEquals("token123", result);
    }

    // Login Wrong Password
    @Test
    void testLogin_InvalidPassword() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("wrong");

        User user = new User();
        user.setPassword("encoded");

        when(userRepo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid password", ex.getMessage());
    }
}
