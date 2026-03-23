package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MusicGroupRepository extends JpaRepository<MusicGroup, Long> {
    Optional<MusicGroup> findByNameIgnoreCase(String name);
    List<MusicGroup> findByGenre(MusicGenre genre);
}