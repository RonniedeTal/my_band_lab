package com.my_band_lab.my_band_lab.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @Size(min = 3, max = 50, message = "Surname must be between 3 and 50 characters")
    private String surname;

    private String profileImageUrl;
}