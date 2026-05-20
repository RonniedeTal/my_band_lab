package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.ConversationResponse;
import com.my_band_lab.my_band_lab.dto.MessageRequest;
import com.my_band_lab.my_band_lab.dto.MessageResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.MessageService;
import com.my_band_lab.my_band_lab.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User sender = userService.findUserByEmail(userDetails.getUsername());
            MessageResponse response = messageService.sendMessage(sender.getId(), request);

            log.info("📨 Message sent from user {} to user {}", sender.getId(), request.getReceiverId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

    @GetMapping("/conversation/{partnerId}")
    public ResponseEntity<?> getConversation(
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.findUserByEmail(userDetails.getUsername());
            Pageable pageable = PageRequest.of(page, size);
            PageResponse<MessageResponse> conversation = messageService.getConversation(
                    currentUser.getId(), partnerId, pageable);

            messageService.markAsRead(currentUser.getId(), partnerId);

            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            log.error("Error getting conversation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.findUserByEmail(userDetails.getUsername());
            List<ConversationResponse> conversations = messageService.getUserConversations(currentUser.getId());

            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error getting conversations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.findUserByEmail(userDetails.getUsername());
            Long count = messageService.getUnreadCount(currentUser.getId());

            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            log.error("Error getting unread count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

    @GetMapping("/unread/from/{senderId}")
    public ResponseEntity<?> getUnreadCountFromUser(
            @PathVariable Long senderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.findUserByEmail(userDetails.getUsername());
            Long count = messageService.getUnreadCountFromUser(currentUser.getId(), senderId);

            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            log.error("Error getting unread count from user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

    @PostMapping("/mark-read/{senderId}")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long senderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.findUserByEmail(userDetails.getUsername());
            int marked = messageService.markAsRead(currentUser.getId(), senderId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "markedCount", marked
            ));
        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.findUserByEmail(userDetails.getUsername());
            int marked = messageService.markAllAsRead(currentUser.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "markedCount", marked
            ));
        } catch (Exception e) {
            log.error("Error marking all messages as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<?> getMessageById(
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            MessageResponse message = messageService.getMessageById(messageId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Error getting message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }
}