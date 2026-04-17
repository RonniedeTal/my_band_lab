package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.NotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, Long> {
    List<NotificationSubscription> findByUserId(Long userId);
    Optional<NotificationSubscription> findByUserIdAndEndpoint(Long userId, String endpoint);
    void deleteByUserIdAndEndpoint(Long userId, String endpoint);
}