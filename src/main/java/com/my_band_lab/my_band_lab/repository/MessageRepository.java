package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Message;
import com.my_band_lab.my_band_lab.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            Pageable pageable);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
            "ORDER BY m.createdAt DESC")
    List<Message> findConversationList(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessages(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadByReceiverId(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.isRead = false")
    Long countUnreadByReceiverAndSender(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId ORDER BY m.createdAt DESC")
    Page<Message> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT DISTINCT CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END " +
            "FROM messages m WHERE m.sender_id = :userId OR m.receiver_id = :userId",
            nativeQuery = true)
    List<Long> findConversationPartnerIds(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.id = " +
            "(SELECT MAX(m2.id) FROM Message m2 WHERE " +
            "((m2.sender.id = :userId AND m2.receiver.id = :partnerId) OR " +
            "(m2.sender.id = :partnerId AND m2.receiver.id = :userId)))")
    Optional<Message> findLatestMessageInConversation(
            @Param("userId") Long userId,
            @Param("partnerId") Long partnerId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.receiver.id = :userId AND m.sender.id = :senderId AND m.isRead = false")
    int markAsRead(@Param("userId") Long userId, @Param("senderId") Long senderId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.receiver.id = :userId AND m.isRead = false")
    int markAllAsReadByReceiver(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
            "ORDER BY m.createdAt DESC")
    Optional<Message> findLatestMessageBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    Page<Message> findBySenderIdOrderByCreatedAtDesc(Long senderId, Pageable pageable);

    Page<Message> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender.id = :senderId OR m.receiver.id = :receiverId")
    Long countTotalMessagesForUser(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}