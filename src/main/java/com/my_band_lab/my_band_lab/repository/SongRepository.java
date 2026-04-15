package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByArtistIdOrderByCreatedAtDesc(Long artistId);
    List<Song> findByMusicGroupIdOrderByCreatedAtDesc(Long groupId);
}