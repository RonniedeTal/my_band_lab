// src/main/java/com/my_band_lab/my_band_lab/dto/LookingForBandStatusRequest.java

package com.my_band_lab.my_band_lab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LookingForBandStatusRequest {
    private boolean isLookingForBand;  // ← este nombre debe coincidir con el JSON
}