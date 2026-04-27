package com.my_band_lab.my_band_lab.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LookingForInstrumentsResponse {
    private List<InstrumentDTO> instruments;
    private int count;

    @Data
    @Builder
    public static class InstrumentDTO {
        private Long id;
        private String name;
        private String category;
    }
}