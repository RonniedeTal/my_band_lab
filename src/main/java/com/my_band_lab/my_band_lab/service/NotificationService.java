package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.AppNotification;
import com.my_band_lab.my_band_lab.entity.NotificationSubscription;
import com.my_band_lab.my_band_lab.entity.UserNotificationPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void subscribeUser(Long userId, String endpoint, String p256dh, String auth);
    void unsubscribeUser(Long userId, String endpoint);

    AppNotification createNotification(Long userId, String title, String body, String notificationType, Long referenceId);
    Page<AppNotification> getUserNotifications(Long userId, Pageable pageable);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    void deleteNotification(Long notificationId, Long userId);

    void sendPushNotification(Long userId, String title, String body, String url, String icon);

    void updatePreferences(Long userId, boolean pushEnabled, boolean emailEnabled,
                           boolean notifyOnFollow, boolean notifyOnComment,
                           boolean notifyOnLike, boolean notifyOnSongUpload);
    UserNotificationPreference getPreferences(Long userId);
}