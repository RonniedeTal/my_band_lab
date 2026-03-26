package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.LoginRequest;
import com.my_band_lab.my_band_lab.dto.LoginResponse;
import com.my_band_lab.my_band_lab.dto.RegisterRequest;
import com.my_band_lab.my_band_lab.dto.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request) throws Exception;
    LoginResponse login(LoginRequest request) throws Exception;
}