package com.my_band_lab.my_band_lab.dto;

import com.my_band_lab.my_band_lab.entity.MusicGenre;
import lombok.Data;

import java.util.List;

@Data
public class CreateArtistRequest {
    private Long userId;
    private String stageName;
    private String biography;
    private MusicGenre genre;
    private List<Long> instrumentIds;
    private Long mainInstrumentId;
}