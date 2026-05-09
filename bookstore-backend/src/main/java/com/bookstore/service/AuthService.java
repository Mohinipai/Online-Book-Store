package com.bookstore.service;

import com.bookstore.dto.LoginDto;
import com.bookstore.dto.RegisterDto;

public interface AuthService {
    String login(LoginDto loginDto);
    String register(RegisterDto registerDto);
}
