package com.my_band_lab.my_band_lab.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "push_enabled")
    @Builder.Default
    private boolean pushEnabled = true;

    @Column(name = "email_enabled")
    @Builder.Default
    private boolean emailEnabled = false;

    @Column(name = "notify_on_follow")
    @Builder.Default
    private boolean notifyOnFollow = true;

    @Column(name = "notify_on_comment")
    @Builder.Default
    private boolean notifyOnComment = true;

    @Column(name = "notify_on_like")
    @Builder.Default
    private boolean notifyOnLike = true;

    @Column(name = "notify_on_song_upload")
    @Builder.Default
    private boolean notifyOnSongUpload = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}