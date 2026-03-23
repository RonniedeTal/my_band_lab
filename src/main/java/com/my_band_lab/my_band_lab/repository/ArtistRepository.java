package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByUserId(Long userId);
    Optional<Artist> findByStageNameIgnoreCase(String stageName);
}