package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.*;
import com.my_band_lab.my_band_lab.repository.*;
import com.my_band_lab.my_band_lab.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {


    private final NotificationSubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void subscribeUser(Long userId, String endpoint, String p256dh, String auth) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        subscriptionRepository.findByUserIdAndEndpoint(userId, endpoint)
                .ifPresent(sub -> subscriptionRepository.delete(sub));

        NotificationSubscription subscription = NotificationSubscription.builder()
                .user(user)
                .endpoint(endpoint)
                .p256dh(p256dh)
                .auth(auth)
                .build();

        subscriptionRepository.save(subscription);
        log.info("Usuario {} suscrito a notificaciones push", user.getEmail());
    }

    @Override
    @Transactional
    public void unsubscribeUser(Long userId, String endpoint) {
        subscriptionRepository.deleteByUserIdAndEndpoint(userId, endpoint);
        log.info("Usuario {} desuscrito de notificaciones push", userId);
    }

    @Override
    @Transactional
    public AppNotification createNotification(Long userId, String title, String body, String notificationType, Long referenceId) {
        System.out.println("=== CREATE NOTIFICATION ===");
        System.out.println("User ID: " + userId);
        System.out.println("Title: " + title);
        System.out.println("Body: " + body);
        System.out.println("Type: " + notificationType);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        AppNotification notification = AppNotification.builder()
                .user(user)
                .title(title)
                .body(body)
                .notificationType(notificationType)
                .referenceId(referenceId)
                .isRead(false)
                .build();

        AppNotification saved = notificationRepository.save(notification);
        System.out.println("Notificación guardada con ID: " + saved.getId());
        return saved;
    }

    @Override
    public Page<AppNotification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        AppNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para modificar esta notificacion");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<AppNotification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        AppNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta notificacion");
        }

        notificationRepository.delete(notification);
    }

    @Override
    public void sendPushNotification(Long userId, String title, String body, String url, String icon) {
        // TODO: Implementar envío real de notificaciones push con web-push
        // Por ahora solo guardamos la notificación en BD
        createNotification(userId, title, body, "PUSH", null);
        log.info("Notificacion guardada en BD para usuario {}: {} - {}", userId, title, body);
    }

    @Override
    @Transactional
    public void updatePreferences(Long userId, boolean pushEnabled, boolean emailEnabled,
                                  boolean notifyOnFollow, boolean notifyOnComment,
                                  boolean notifyOnLike, boolean notifyOnSongUpload) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserNotificationPreference preferences = preferenceRepository.findByUserId(userId)
                .orElse(UserNotificationPreference.builder().user(user).build());

        preferences.setPushEnabled(pushEnabled);
        preferences.setEmailEnabled(emailEnabled);
        preferences.setNotifyOnFollow(notifyOnFollow);
        preferences.setNotifyOnComment(notifyOnComment);
        preferences.setNotifyOnLike(notifyOnLike);
        preferences.setNotifyOnSongUpload(notifyOnSongUpload);

        preferenceRepository.save(preferences);
        log.info("Preferencias de notificacion actualizadas para usuario {}", userId);
    }

    @Override
    public UserNotificationPreference getPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElse(UserNotificationPreference.builder()
                        .user(userRepository.findById(userId).orElse(null))
                        .pushEnabled(true)
                        .emailEnabled(false)
                        .notifyOnFollow(true)
                        .notifyOnComment(true)
                        .notifyOnLike(true)
                        .notifyOnSongUpload(true)
                        .build());
    }
}