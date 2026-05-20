package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.ConversationResponse;
import com.my_band_lab.my_band_lab.dto.MessageRequest;
import com.my_band_lab.my_band_lab.dto.MessageResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Message;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.MessageRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public MessageResponse sendMessage(Long senderId, MessageRequest request) throws Exception {
        if (senderId.equals(request.getReceiverId())) {
            throw new IllegalArgumentException("Cannot send message to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new Exception("Sender not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new Exception("Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);
        return toResponse(saved);
    }

    @Override
    public MessageResponse getMessageById(Long messageId) throws Exception {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new Exception("Message not found"));
        return toResponse(message);
    }

    @Override
    public PageResponse<MessageResponse> getConversation(Long userId, Long partnerId, Pageable pageable) throws Exception {
        if (!userRepository.existsById(userId)) {
            throw new Exception("User not found");
        }
        if (!userRepository.existsById(partnerId)) {
            throw new Exception("Partner not found");
        }

        Page<Message> messages = messageRepository.findConversation(userId, partnerId, pageable);

        List<MessageResponse> content = messages.map(this::toResponse).getContent();

        return PageResponse.<MessageResponse>builder()
                .content(content)
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .currentPage(messages.getNumber())
                .size(messages.getSize())
                .hasNext(messages.hasNext())
                .hasPrevious(messages.hasPrevious())
                .build();
    }

    @Override
    public List<ConversationResponse> getUserConversations(Long userId) throws Exception {
        if (!userRepository.existsById(userId)) {
            throw new Exception("User not found");
        }

        List<Long> partnerIds = messageRepository.findConversationPartnerIds(userId);
        List<ConversationResponse> conversations = new ArrayList<>();

        for (Long partnerId : partnerIds) {
            conversations.add(toConversationResponse(userId, partnerId));
        }

        conversations.sort((a, b) -> {
            if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return conversations;
    }

    @Override
    public Long getUnreadCount(Long userId) throws Exception {
        if (!userRepository.existsById(userId)) {
            throw new Exception("User not found");
        }
        return messageRepository.countUnreadByReceiverId(userId);
    }

    @Override
    public Long getUnreadCountFromUser(Long receiverId, Long senderId) throws Exception {
        if (!userRepository.existsById(receiverId)) {
            throw new Exception("Receiver not found");
        }
        if (!userRepository.existsById(senderId)) {
            throw new Exception("Sender not found");
        }
        return messageRepository.countUnreadByReceiverAndSender(receiverId, senderId);
    }

    @Override
    public int markAsRead(Long receiverId, Long senderId) throws Exception {
        if (!userRepository.existsById(receiverId)) {
            throw new Exception("Receiver not found");
        }
        if (!userRepository.existsById(senderId)) {
            throw new Exception("Sender not found");
        }
        return messageRepository.markAsRead(receiverId, senderId);
    }

    @Override
    public int markAllAsRead(Long userId) throws Exception {
        if (!userRepository.existsById(userId)) {
            throw new Exception("User not found");
        }
        return messageRepository.markAllAsReadByReceiver(userId);
    }

    @Override
    public MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName() + " " + message.getSender().getSurname())
                .senderEmail(message.getSender().getEmail())
                .senderImageUrl(message.getSender().getProfileImageUrl())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getName() + " " + message.getReceiver().getSurname())
                .receiverEmail(message.getReceiver().getEmail())
                .receiverImageUrl(message.getReceiver().getProfileImageUrl())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    @Override
    public ConversationResponse toConversationResponse(Long userId, Long partnerId) throws Exception {
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new Exception("Partner not found"));

        Optional<Message> lastMessage = messageRepository.findLatestMessageBetweenUsers(userId, partnerId);

        Long unreadCount = messageRepository.countUnreadByReceiverAndSender(userId, partnerId);

        Long totalMessages = 0L;
        if (lastMessage.isPresent()) {
            Page<Message> messages = messageRepository.findConversation(userId, partnerId, Pageable.unpaged());
            totalMessages = messages.getTotalElements();
        }

        return ConversationResponse.builder()
                .partnerId(partner.getId())
                .partnerName(partner.getName() + " " + partner.getSurname())
                .partnerEmail(partner.getEmail())
                .partnerImageUrl(partner.getProfileImageUrl())
                .lastMessageId(lastMessage.map(Message::getId).orElse(null))
                .lastMessageContent(lastMessage.map(Message::getContent).orElse(null))
                .lastMessageTime(lastMessage.map(Message::getCreatedAt).orElse(null))
                .unreadCount(unreadCount)
                .totalMessages(totalMessages)
                .build();
    }
}