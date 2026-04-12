package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.GroupFollow;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupFollowRepository extends JpaRepository<GroupFollow, Long> {

    // Verificar si un usuario sigue a un grupo
    boolean existsByUserAndGroup(User user, MusicGroup group);

    // Eliminar un follow
    void deleteByUserAndGroup(User user, MusicGroup group);

    // Obtener todos los grupos que sigue un usuario (paginado)
    @Query("SELECT gf.group FROM GroupFollow gf WHERE gf.user = :user")
    Page<MusicGroup> findFollowedGroupsByUser(@Param("user") User user, Pageable pageable);

    // Contar cuántos seguidores tiene un grupo
    long countByGroup(MusicGroup group);
}