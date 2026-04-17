package com.my_band_lab.my_band_lab.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPreferencesDTO {
    private boolean pushEnabled;
    private boolean emailEnabled;
    private boolean notifyOnFollow;
    private boolean notifyOnComment;
    private boolean notifyOnLike;
    private boolean notifyOnSongUpload;
}