package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.GroupFavorite;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupFavoriteRepository extends JpaRepository<GroupFavorite, Long> {

    // Verificar si un grupo está en favoritos
    boolean existsByUserAndGroup(User user, MusicGroup group);

    // Eliminar de favoritos
    void deleteByUserAndGroup(User user, MusicGroup group);

    // Obtener grupos favoritos de un usuario (paginado)
    @Query("SELECT gf.group FROM GroupFavorite gf WHERE gf.user = :user")
    Page<MusicGroup> findFavoriteGroupsByUser(@Param("user") User user, Pageable pageable);
}