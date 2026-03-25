package com.my_band_lab.my_band_lab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Please introduce your email")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Please introduce a password")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Please introduce your name")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Please introduce your surname")
    @Size(min = 3, max = 50, message = "Surname must be between 3 and 50 characters")
    @Column(nullable = false)
    private String surname;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private Role role;

    @Column (name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column (name = "profile_image_url")
    private String profileImageUrl;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Artist artist;

    @ManyToMany(mappedBy = "members")
    @JsonIgnore
    @Builder.Default
    private List<MusicGroup> musicGroups = new ArrayList<>();

    @OneToOne(mappedBy = "founder")  // Cambiado de leader a founder
    @JsonIgnore
    private MusicGroup foundedGroup;  // Cambiado de ledGroup a foundedGroup

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<Playlist> playlists = new ArrayList<>();
//
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
//    private Artist artist;
//
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
