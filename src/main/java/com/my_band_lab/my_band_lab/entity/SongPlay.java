package com.my_band_lab.my_band_lab.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "song_plays", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "song_id", "play_date"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongPlay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "play_date", nullable = false)
    private LocalDateTime playDate;

    @PrePersist
    protected void onCreate() {
        playDate = LocalDateTime.now();
    }
}