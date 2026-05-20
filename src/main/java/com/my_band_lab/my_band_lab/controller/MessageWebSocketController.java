package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.MessageRequest;
import com.my_band_lab.my_band_lab.dto.MessageResponse;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.MessageService;
import com.my_band_lab.my_band_lab.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageRequest request, Principal principal) {
        try {
            User sender = userService.findUserByEmail(principal.getName());
            MessageResponse response = messageService.sendMessage(sender.getId(), request);

            String dest1 = "/topic/messages/" + request.getReceiverId();
            String dest2 = "/topic/messages/" + sender.getId();
            
            messagingTemplate.convertAndSend(dest1, (Object) response);
            messagingTemplate.convertAndSend(dest2, (Object) response);

            log.info("📨 WebSocket message sent from {} to {}", sender.getId(), request.getReceiverId());
        } catch (Exception e) {
            log.error("Error sending WebSocket message: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.sendToUser/{userId}")
    public void sendMessageToUser(@DestinationVariable Long userId, @Payload MessageRequest request, Principal principal) {
        try {
            User sender = userService.findUserByEmail(principal.getName());
            MessageResponse response = messageService.sendMessage(sender.getId(), request);
            String dest = "/queue/messages/" + userId;
            messagingTemplate.convertAndSend(dest, (Object) response);
            log.info("📨 Direct WebSocket message from {} to {}", sender.getId(), userId);
        } catch (Exception e) {
            log.error("Error sending direct WebSocket message: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.markRead/{senderId}")
    public void markMessagesAsRead(@DestinationVariable Long senderId, Principal principal) {
        try {
            User receiver = userService.findUserByEmail(principal.getName());
            messageService.markAsRead(receiver.getId(), senderId);
            
            String dest = "/queue/read/" + senderId;
            var payload = new java.util.HashMap<String, Object>();
            payload.put("readerId", receiver.getId());
            payload.put("marked", true);
            messagingTemplate.convertAndSend(dest, (Object) payload);
        } catch (Exception e) {
            log.error("Error marking messages as read via WebSocket: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.getConversations")
    public void getConversations(Principal principal) {
        try {
            User user = userService.findUserByEmail(principal.getName());
            var conversations = messageService.getUserConversations(user.getId());
            String dest = "/queue/conversations/" + user.getId();
            messagingTemplate.convertAndSend(dest, (Object) conversations);
        } catch (Exception e) {
            log.error("Error getting conversations via WebSocket: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.getUnreadCount")
    public void getUnreadCount(Principal principal) {
        try {
            User user = userService.findUserByEmail(principal.getName());
            Long count = messageService.getUnreadCount(user.getId());
            
            String dest = "/queue/unread/" + user.getId();
            var payload = new java.util.HashMap<String, Object>();
            payload.put("unreadCount", count);
            messagingTemplate.convertAndSend(dest, (Object) payload);
        } catch (Exception e) {
            log.error("Error getting unread count via WebSocket: {}", e.getMessage());
        }
    }
}