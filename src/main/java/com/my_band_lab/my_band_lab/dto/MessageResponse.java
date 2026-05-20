package com.my_band_lab.my_band_lab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private String senderImageUrl;
    private Long receiverId;
    private String receiverName;
    private String receiverEmail;
    private String receiverImageUrl;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}