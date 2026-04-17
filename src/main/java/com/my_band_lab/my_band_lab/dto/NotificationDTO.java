package com.my_band_lab.my_band_lab.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private String body;
    private boolean isRead;
    private String notificationType;
    private Long referenceId;
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
    private String userSurname;
}