package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.SongPlay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SongPlayRepository extends JpaRepository<SongPlay, Long> {

    // Verificar si el usuario reprodujo la canción en las últimas 24 horas
    Optional<SongPlay> findByUserIdAndSongIdAndPlayDateAfter(Long userId, Long songId, LocalDateTime dateTime);

    // Contar reproducciones totales de una canción
    long countBySongId(Long songId);

    // Contar reproducciones únicas por usuario (sin duplicados en 24h)
    @Query("SELECT COUNT(DISTINCT sp.user.id) FROM SongPlay sp WHERE sp.song.id = :songId")
    long countUniquePlaysBySong(@Param("songId") Long songId);

    // Top canciones de un artista
    @Query("SELECT sp.song, COUNT(sp) as playCount FROM SongPlay sp " +
            "WHERE sp.song.artist.id = :artistId " +
            "GROUP BY sp.song " +
            "ORDER BY playCount DESC")
    List<Object[]> findTopSongsByArtist(@Param("artistId") Long artistId);

    // Top canciones de un grupo
    @Query("SELECT sp.song, COUNT(sp) as playCount FROM SongPlay sp " +
            "WHERE sp.song.musicGroup.id = :groupId " +
            "GROUP BY sp.song " +
            "ORDER BY playCount DESC")
    List<Object[]> findTopSongsByGroup(@Param("groupId") Long groupId);
}