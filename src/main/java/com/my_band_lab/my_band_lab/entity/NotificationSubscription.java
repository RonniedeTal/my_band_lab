package com.my_band_lab.my_band_lab.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_subscriptions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "endpoint"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;

    @Column(name = "p256dh", nullable = false, length = 200)
    private String p256dh;

    @Column(name = "auth", nullable = false, length = 100)
    private String auth;

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