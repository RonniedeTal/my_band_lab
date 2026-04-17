package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.AppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    Page<AppNotification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserIdAndIsReadFalse(Long userId);
    List<AppNotification> findByUserIdAndIsReadFalse(Long userId);
}