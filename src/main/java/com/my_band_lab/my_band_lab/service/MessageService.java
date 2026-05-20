package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.ConversationResponse;
import com.my_band_lab.my_band_lab.dto.MessageRequest;
import com.my_band_lab.my_band_lab.dto.MessageResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {

    MessageResponse sendMessage(Long senderId, MessageRequest request) throws Exception;

    MessageResponse getMessageById(Long messageId) throws Exception;

    PageResponse<MessageResponse> getConversation(Long userId, Long partnerId, Pageable pageable) throws Exception;

    List<ConversationResponse> getUserConversations(Long userId) throws Exception;

    Long getUnreadCount(Long userId) throws Exception;

    Long getUnreadCountFromUser(Long receiverId, Long senderId) throws Exception;

    int markAsRead(Long receiverId, Long senderId) throws Exception;

    int markAllAsRead(Long userId) throws Exception;

    MessageResponse toResponse(com.my_band_lab.my_band_lab.entity.Message message);

    ConversationResponse toConversationResponse(Long userId, Long partnerId) throws Exception;
}