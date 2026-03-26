package com.my_band_lab.my_band_lab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}