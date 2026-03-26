package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.RegisterRequest;
import com.my_band_lab.my_band_lab.dto.RegisterResponse;
import com.my_band_lab.my_band_lab.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) throws Exception {
        return authService.register(request);
    }
}