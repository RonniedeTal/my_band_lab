package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByArtistIdOrderByReleaseDateDesc(Long artistId);
    List<Album> findByMusicGroupIdOrderByReleaseDateDesc(Long groupId);
}