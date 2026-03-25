package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByUserId(Long userId);
    Optional<Artist> findByStageNameIgnoreCase(String stageName);
    List<Artist> findByGenre(MusicGenre genre);

    @Query("SELECT a FROM Artist a JOIN a.instruments i WHERE i.id = :instrumentId")
    List<Artist> findByInstrumentId(@Param("instrumentId") Long instrumentId);
}