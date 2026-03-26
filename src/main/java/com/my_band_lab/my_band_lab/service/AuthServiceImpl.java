package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.LoginRequest;
import com.my_band_lab.my_band_lab.dto.LoginResponse;
import com.my_band_lab.my_band_lab.dto.RegisterRequest;
import com.my_band_lab.my_band_lab.dto.RegisterResponse;
import com.my_band_lab.my_band_lab.entity.Role;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import com.my_band_lab.my_band_lab.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public RegisterResponse register(RegisterRequest request) throws Exception {
        // Verificar si el email ya existe
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new Exception("Email already in use: " + request.getEmail());
        }

        // Crear nuevo usuario
        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .surname(savedUser.getSurname())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) throws Exception {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 1. Buscar usuario por email
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        // 2. Validar password con BCrypt
//        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
//        }

        // 3. Devolver datos del usuario (sin token por ahora)
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}