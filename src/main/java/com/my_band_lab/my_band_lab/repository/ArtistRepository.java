package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<Artist> findByVerifiedFalse();
    Page<Artist> findByVerifiedFalse(Pageable pageable);

    @Query("SELECT a FROM Artist a JOIN a.instruments i WHERE i.id = :instrumentId")
    List<Artist> findByInstrumentId(@Param("instrumentId") Long instrumentId);

    @Query("SELECT a FROM Artist a WHERE LOWER(a.stageName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.user.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Artist> searchByNameOrStageName(@Param("query") String query, Pageable pageable);

    @Query("SELECT a FROM Artist a WHERE " +
            "(:query IS NULL OR :query = '' OR " +
            "LOWER(a.stageName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.user.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.user.surname) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:country IS NULL OR :country = '' OR LOWER(a.country) = LOWER(:country)) AND " +
            "(:city IS NULL OR :city = '' OR LOWER(a.city) = LOWER(:city)) AND " +
            "(:genre IS NULL OR a.genre = :genre)")
    Page<Artist> searchWithFilters(@Param("query") String query,
                                   @Param("country") String country,
                                   @Param("city") String city,
                                   @Param("genre") MusicGenre genre,
                                   Pageable pageable);

    List<Artist> findByIsLookingForBandTrue();

    // ✅ Consulta nativa para filtrar por géneros buscados
    @Query(value = "SELECT * FROM artists a WHERE a.is_looking_for_band = true AND :genre = ANY(a.looking_for_genres)", nativeQuery = true)
    List<Artist> findByLookingForBandAndGenre(@Param("genre") String genre);
}