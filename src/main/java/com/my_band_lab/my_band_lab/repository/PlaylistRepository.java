package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Playlist> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true ORDER BY p.createdAt DESC")
    Page<Playlist> findPublicPlaylists(Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.isPublic = true AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Playlist> searchPublicPlaylists(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Playlist p WHERE p.user.id = :userId AND (p.isPublic = true OR p.user.id = :currentUserId)")
    List<Playlist> findAccessiblePlaylists(@Param("userId") Long userId, @Param("currentUserId") Long currentUserId);
}