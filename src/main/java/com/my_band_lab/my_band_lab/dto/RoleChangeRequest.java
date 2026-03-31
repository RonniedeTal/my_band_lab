package com.my_band_lab.my_band_lab.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleChangeRequest {

    @NotBlank(message = "Role is required")
    private String role;
}