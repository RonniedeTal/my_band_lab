package com.my_band_lab.my_band_lab.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "music_groups")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class MusicGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Please introduce a group name")
    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Column(name = "formed_date")
    private LocalDateTime formedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    private MusicGenre genre;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    @JsonIgnoreProperties({"musicGroups", "foundedGroup"})

    private List<User> members = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "founder_id")  // Cambiado de leader_id a founder_id
    @JsonIgnoreProperties({"musicGroups", "foundedGroup", "artist"})
    private User founder;  // Cambiado de leader a founder

    private boolean verified;

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
