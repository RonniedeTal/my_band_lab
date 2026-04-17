package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.NotificationDTO;
import com.my_band_lab.my_band_lab.dto.NotificationPreferencesDTO;
import com.my_band_lab.my_band_lab.dto.NotificationSubscriptionDTO;
import com.my_band_lab.my_band_lab.entity.AppNotification;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.NotificationService;
import com.my_band_lab.my_band_lab.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @RequestBody NotificationSubscriptionDTO subscription,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        notificationService.subscribeUser(
                currentUser.getId(),
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth()
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @RequestParam String endpoint,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        notificationService.unsubscribeUser(currentUser.getId(), endpoint);
        return ResponseEntity.ok().build();
    }

//    @GetMapping
//    public ResponseEntity<Page<AppNotification>> getNotifications(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
//
//        User currentUser = userService.findUserByEmail(userDetails.getUsername());
//        Page<AppNotification> notifications = notificationService.getUserNotifications(
//                currentUser.getId(), PageRequest.of(page, size));
//
//        // Log para depuración
//        System.out.println("Notificaciones encontradas: " + notifications.getTotalElements());
//
//        return ResponseEntity.ok(notifications);
//    }

    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        Page<AppNotification> notifications = notificationService.getUserNotifications(
                currentUser.getId(), PageRequest.of(page, size));

        Page<NotificationDTO> dtoPage = notifications.map(notif -> NotificationDTO.builder()
                .id(notif.getId())
                .title(notif.getTitle())
                .body(notif.getBody())
                .isRead(notif.isRead())
                .notificationType(notif.getNotificationType())
                .referenceId(notif.getReferenceId())
                .createdAt(notif.getCreatedAt())
                .userId(notif.getUser().getId())
                .userName(notif.getUser().getName())
                .userSurname(notif.getUser().getSurname())
                .build());

        return ResponseEntity.ok(dtoPage);
    }
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        notificationService.deleteNotification(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferencesDTO> getPreferences(
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        var preferences = notificationService.getPreferences(currentUser.getId());

        return ResponseEntity.ok(NotificationPreferencesDTO.builder()
                .pushEnabled(preferences.isPushEnabled())
                .emailEnabled(preferences.isEmailEnabled())
                .notifyOnFollow(preferences.isNotifyOnFollow())
                .notifyOnComment(preferences.isNotifyOnComment())
                .notifyOnLike(preferences.isNotifyOnLike())
                .notifyOnSongUpload(preferences.isNotifyOnSongUpload())
                .build());
    }

    @PutMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(
            @RequestBody NotificationPreferencesDTO preferences,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        notificationService.updatePreferences(
                currentUser.getId(),
                preferences.isPushEnabled(),
                preferences.isEmailEnabled(),
                preferences.isNotifyOnFollow(),
                preferences.isNotifyOnComment(),
                preferences.isNotifyOnLike(),
                preferences.isNotifyOnSongUpload()
        );
        return ResponseEntity.ok().build();
    }
}