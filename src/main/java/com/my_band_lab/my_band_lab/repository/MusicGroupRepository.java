package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MusicGroupRepository extends JpaRepository<MusicGroup, Long> {
    Optional<MusicGroup> findByNameIgnoreCase(String name);
    List<MusicGroup> findByGenre(MusicGenre genre);

    List<MusicGroup> findByVerifiedFalse();
    Page<MusicGroup> findByVerifiedFalse(Pageable pageable);

    @Query("SELECT mg FROM MusicGroup mg WHERE LOWER(mg.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(mg.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<MusicGroup> searchByNameOrDescription(@Param("query") String query, Pageable pageable);
}