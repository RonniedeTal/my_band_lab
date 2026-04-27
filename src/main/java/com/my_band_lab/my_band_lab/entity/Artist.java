package com.my_band_lab.my_band_lab.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    private User user;

    private String stageName;

    private String biography;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    private MusicGenre genre;

    private boolean verified;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(name = "is_looking_for_band")
    @Builder.Default
    private boolean isLookingForBand = false;

    @Column(name = "looking_for_instrument_ids")
    @Builder.Default
    private List<Long> lookingForInstrumentIds = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "artist_instrument",
            joinColumns = @JoinColumn(name = "artist_id"),
            inverseJoinColumns = @JoinColumn(name = "instrument_id")
    )
    @Builder.Default
    @JsonIgnoreProperties({"artists"})
    private List<Instrument> instruments = new ArrayList<>();

    // ========== RELACIÓN CON CANCIONES ==========
    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Song> songs = new ArrayList<>();
    // ===========================================

    @Column(name = "main_instrument_id")
    private Long mainInstrumentId; // Instrumento principal (opcional)

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

    //================album============
    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Album> albums = new ArrayList<>();
}
