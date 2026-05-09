package com.bookstore.controller;

import com.bookstore.dto.AuthResponseDto;
import com.bookstore.dto.LoginDto;
import com.bookstore.dto.RegisterDto;
import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;
    private UserRepository userRepository;

    // Build Login REST API
    @PostMapping(value = {"/login", "/signin"})
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto loginDto){
        String token = authService.login(loginDto);

        User user = userRepository.findByEmail(loginDto.getEmail()).get();

        AuthResponseDto authResponseDto = new AuthResponseDto();
        authResponseDto.setAccessToken(token);
        authResponseDto.setName(user.getName());
        authResponseDto.setEmail(user.getEmail());
        authResponseDto.setRole(user.getRole().name());

        return ResponseEntity.ok(authResponseDto);
    }

    // Build Register REST API
    @PostMapping(value = {"/register", "/signup"})
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto){
        String response = authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}
