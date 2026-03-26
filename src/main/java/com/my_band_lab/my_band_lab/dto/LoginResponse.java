package com.my_band_lab.my_band_lab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String role;
}