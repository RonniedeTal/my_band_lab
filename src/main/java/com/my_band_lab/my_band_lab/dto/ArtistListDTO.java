package com.my_band_lab.my_band_lab.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ArtistListDTO {
    private Long id;
    private String stageName;
    private String genre;
    private String city;
    private String country;
    private List<InstrumentDTO> instruments;
    private boolean lookingForBand;
    private String profileImageUrl;
    private boolean verified;

    @Data
    @Builder
    public static class InstrumentDTO {
        private Long id;
        private String name;
    }
}