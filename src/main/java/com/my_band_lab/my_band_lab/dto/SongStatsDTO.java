package com.my_band_lab.my_band_lab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongStatsDTO {
    private Long songId;
    private String title;
    private Integer playCount;
    private Long uniqueListeners;
}