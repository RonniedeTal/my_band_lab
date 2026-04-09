package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.Favorite;
import com.my_band_lab.my_band_lab.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndArtist(User user, Artist artist);

    boolean existsByUserAndArtist(User user, Artist artist);

    void deleteByUserAndArtist(User user, Artist artist);

    Page<Favorite> findByUser(User user, Pageable pageable);

    @Query("SELECT f.artist FROM Favorite f WHERE f.user = :user")
    Page<Artist> findFavoriteArtistsByUser(@Param("user") User user, Pageable pageable);
}