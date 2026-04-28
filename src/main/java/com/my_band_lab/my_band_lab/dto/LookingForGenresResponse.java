package com.my_band_lab.my_band_lab.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LookingForGenresResponse {
    private List<String> genres;
    private int count;
}