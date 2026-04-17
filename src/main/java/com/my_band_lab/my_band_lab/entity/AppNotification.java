package com.my_band_lab.my_band_lab.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    private String icon;

    private String url;

    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "notification_type")
    private String notificationType; // FOLLOW, COMMENT, LIKE, SONG_UPLOAD, etc.

    @Column(name = "reference_id")
    private Long referenceId; // ID del artista, grupo, canción, etc.

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}