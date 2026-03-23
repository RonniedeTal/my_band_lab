package com.my_band_lab.my_band_lab.entity;


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
    private List<User> members = new ArrayList<>();


}
