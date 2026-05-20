package com.my_band_lab.my_band_lab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistByArtistResponse {
    private Long id;
    private String title;
    private String description;
    private String coverImageUrl;
    private boolean isPublic;
    private UserSimple user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSimple {
        private Long id;
        private String name;
        private String surname;
    }
}