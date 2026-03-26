package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.RegisterRequest;
import com.my_band_lab.my_band_lab.dto.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request) throws Exception;
}