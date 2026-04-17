package com.my_band_lab.my_band_lab.dto;

import lombok.Data;

@Data
public class NotificationSubscriptionDTO {
    private String endpoint;
    private String p256dh;
    private String auth;
}