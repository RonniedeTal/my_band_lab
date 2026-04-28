package com.my_band_lab.my_band_lab.dto;

import lombok.Data;
import java.util.List;

@Data
public class LookingForGenresRequest {
    private List<String> genres;
}