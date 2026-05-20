package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.ConversationResponse;
import com.my_band_lab.my_band_lab.dto.MessageRequest;
import com.my_band_lab.my_band_lab.dto.MessageResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Message;
import com.my_band_lab.my_band_lab.entity.Role;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.MessageRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Tests")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User sender;
    private User receiver;
    private Message message;
    private MessageRequest messageRequest;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .name("Sender")
                .surname("User")
                .email("sender@test.com")
                .password("password")
                .role(Role.USER)
                .profileImageUrl("https://example.com/sender.jpg")
                .build();

        receiver = User.builder()
                .id(2L)
                .name("Receiver")
                .surname("User")
                .email("receiver@test.com")
                .password("password")
                .role(Role.ARTIST)
                .profileImageUrl("https://example.com/receiver.jpg")
                .build();

        message = Message.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .content("Test message content")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        messageRequest = new MessageRequest();
        messageRequest.setReceiverId(2L);
        messageRequest.setContent("Test message content");
    }

    @Nested
    @DisplayName("sendMessage Tests")
    class SendMessageTests {

        @Test
        @DisplayName("✅ Debe enviar mensaje exitosamente")
        void sendMessage_ShouldSendMessage_WhenValid() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
            when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
            when(messageRepository.save(any(Message.class))).thenReturn(message);

            MessageResponse response = messageService.sendMessage(1L, messageRequest);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEqualTo("Test message content");
            assertThat(response.getSenderId()).isEqualTo(1L);
            assertThat(response.getReceiverId()).isEqualTo(2L);
            verify(messageRepository, times(1)).save(any(Message.class));
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción al enviarse mensaje a sí mismo")
        void sendMessage_ShouldThrowException_WhenSendingToSelf() {
            messageRequest.setReceiverId(1L);

            assertThatThrownBy(() -> messageService.sendMessage(1L, messageRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot send message to yourself");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando remitente no existe")
        void sendMessage_ShouldThrowException_WhenSenderNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService.sendMessage(1L, messageRequest))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Sender not found");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando receptor no existe")
        void sendMessage_ShouldThrowException_WhenReceiverNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
            when(userRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService.sendMessage(1L, messageRequest))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Receiver not found");
        }
    }

    @Nested
    @DisplayName("getMessageById Tests")
    class GetMessageByIdTests {

        @Test
        @DisplayName("✅ Debe obtener mensaje por ID")
        void getMessageById_ShouldReturnMessage_WhenExists() throws Exception {
            when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

            MessageResponse response = messageService.getMessageById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getContent()).isEqualTo("Test message content");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando mensaje no existe")
        void getMessageById_ShouldThrowException_WhenNotFound() {
            when(messageRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService.getMessageById(999L))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Message not found");
        }
    }

    @Nested
    @DisplayName("getConversation Tests")
    class GetConversationTests {

        @Test
        @DisplayName("✅ Debe obtener conversación paginada")
        void getConversation_ShouldReturnPaginatedConversation() throws Exception {
            List<Message> messages = List.of(message);
            Page<Message> page = new PageImpl<>(messages, PageRequest.of(0, 50), 1);

            when(userRepository.existsById(1L)).thenReturn(true);
            when(userRepository.existsById(2L)).thenReturn(true);
            when(messageRepository.findConversation(eq(1L), eq(2L), any(Pageable.class))).thenReturn(page);

            PageResponse<MessageResponse> response = messageService.getConversation(1L, 2L, PageRequest.of(0, 50));

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario no existe")
        void getConversation_ShouldThrowException_WhenUserNotFound() {
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThatThrownBy(() -> messageService.getConversation(1L, 2L, PageRequest.of(0, 50)))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getUserConversations Tests")
    class GetUserConversationsTests {

        @Test
        @DisplayName("✅ Debe obtener lista de conversaciones del usuario")
        void getUserConversations_ShouldReturnConversations() throws Exception {
            when(userRepository.existsById(1L)).thenReturn(true);
            when(messageRepository.findConversationPartnerIds(1L)).thenReturn(List.of(2L));
            when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
            when(messageRepository.findLatestMessageBetweenUsers(1L, 2L)).thenReturn(Optional.of(message));
            when(messageRepository.countUnreadByReceiverAndSender(1L, 2L)).thenReturn(0L);

            List<ConversationResponse> conversations = messageService.getUserConversations(1L);

            assertThat(conversations).hasSize(1);
            assertThat(conversations.get(0).getPartnerId()).isEqualTo(2L);
            assertThat(conversations.get(0).getPartnerName()).isEqualTo("Receiver User");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario no existe")
        void getUserConversations_ShouldThrowException_WhenUserNotFound() {
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThatThrownBy(() -> messageService.getUserConversations(1L))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getUnreadCount Tests")
    class GetUnreadCountTests {

        @Test
        @DisplayName("✅ Debe obtener conteo de mensajes no leídos")
        void getUnreadCount_ShouldReturnUnreadCount() throws Exception {
            when(userRepository.existsById(1L)).thenReturn(true);
            when(messageRepository.countUnreadByReceiverId(1L)).thenReturn(5L);

            Long count = messageService.getUnreadCount(1L);

            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario no existe")
        void getUnreadCount_ShouldThrowException_WhenUserNotFound() {
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThatThrownBy(() -> messageService.getUnreadCount(1L))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("markAsRead Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("✅ Debe marcar mensajes como leídos")
        void markAsRead_ShouldMarkMessagesAsRead() throws Exception {
            when(userRepository.existsById(1L)).thenReturn(true);
            when(userRepository.existsById(2L)).thenReturn(true);
            when(messageRepository.markAsRead(1L, 2L)).thenReturn(3);

            int marked = messageService.markAsRead(1L, 2L);

            assertThat(marked).isEqualTo(3);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando receptor no existe")
        void markAsRead_ShouldThrowException_WhenReceiverNotFound() {
            when(userRepository.existsById(1L)).thenReturn(false);

            assertThatThrownBy(() -> messageService.markAsRead(1L, 2L))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Receiver not found");
        }
    }

    @Nested
    @DisplayName("markAllAsRead Tests")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("✅ Debe marcar todos los mensajes como leídos")
        void markAllAsRead_ShouldMarkAllAsRead() throws Exception {
            when(userRepository.existsById(1L)).thenReturn(true);
            when(messageRepository.markAllAsReadByReceiver(1L)).thenReturn(10);

            int marked = messageService.markAllAsRead(1L);

            assertThat(marked).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("toResponse Tests")
    class ToResponseTests {

        @Test
        @DisplayName("✅ Debe convertir Message a MessageResponse")
        void toResponse_ShouldConvertMessage() {
            MessageResponse response = messageService.toResponse(message);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getSenderId()).isEqualTo(1L);
            assertThat(response.getSenderName()).isEqualTo("Sender User");
            assertThat(response.getSenderEmail()).isEqualTo("sender@test.com");
            assertThat(response.getReceiverId()).isEqualTo(2L);
            assertThat(response.getReceiverName()).isEqualTo("Receiver User");
            assertThat(response.getContent()).isEqualTo("Test message content");
            assertThat(response.getIsRead()).isFalse();
        }
    }

    @Nested
    @DisplayName("toConversationResponse Tests")
    class ToConversationResponseTests {

        @Test
        @DisplayName("✅ Debe convertir a ConversationResponse")
        void toConversationResponse_ShouldReturnConversationResponse() throws Exception {
            when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
            when(messageRepository.findLatestMessageBetweenUsers(1L, 2L)).thenReturn(Optional.of(message));
            when(messageRepository.countUnreadByReceiverAndSender(1L, 2L)).thenReturn(2L);

            List<Message> messages = List.of(message);
            Page<Message> page = new PageImpl<>(messages, PageRequest.of(0, 50), 1);
            when(messageRepository.findConversation(eq(1L), eq(2L), any(Pageable.class))).thenReturn(page);

            ConversationResponse response = messageService.toConversationResponse(1L, 2L);

            assertThat(response.getPartnerId()).isEqualTo(2L);
            assertThat(response.getPartnerName()).isEqualTo("Receiver User");
            assertThat(response.getPartnerEmail()).isEqualTo("receiver@test.com");
            assertThat(response.getLastMessageContent()).isEqualTo("Test message content");
            assertThat(response.getUnreadCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando partner no existe")
        void toConversationResponse_ShouldThrowException_WhenPartnerNotFound() {
            when(userRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> messageService.toConversationResponse(1L, 2L))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Partner not found");
        }
    }
}