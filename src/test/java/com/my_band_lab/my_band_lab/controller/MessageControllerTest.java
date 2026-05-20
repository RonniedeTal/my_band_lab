package com.my_band_lab.my_band_lab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my_band_lab.my_band_lab.dto.ConversationResponse;
import com.my_band_lab.my_band_lab.dto.MessageRequest;
import com.my_band_lab.my_band_lab.dto.MessageResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Role;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.MessageService;
import com.my_band_lab.my_band_lab.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@DisplayName("MessageController Tests")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private UserService userService;

    private User testUser;
    private User receiver;
    private MessageResponse messageResponse;
    private MessageRequest messageRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test")
                .surname("User")
                .email("test@test.com")
                .password("password")
                .role(Role.USER)
                .build();

        receiver = User.builder()
                .id(2L)
                .name("Receiver")
                .surname("User")
                .email("receiver@test.com")
                .password("password")
                .role(Role.ARTIST)
                .build();

        messageResponse = MessageResponse.builder()
                .id(1L)
                .senderId(1L)
                .senderName("Test User")
                .senderEmail("test@test.com")
                .receiverId(2L)
                .receiverName("Receiver User")
                .receiverEmail("receiver@test.com")
                .content("Test message")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        messageRequest = new MessageRequest();
        messageRequest.setReceiverId(2L);
        messageRequest.setContent("Test message");
    }

    @Nested
    @DisplayName("POST /api/messages/send Tests")
    class SendMessageTests {

        @Test
        @WithMockUser
        @DisplayName("✅ Debe enviar mensaje exitosamente")
        void sendMessage_ShouldReturnCreated_WhenValid() throws Exception {
            when(userService.findUserByEmail(anyString())).thenReturn(testUser);
            when(messageService.sendMessage(eq(1L), any(MessageRequest.class))).thenReturn(messageResponse);

            mockMvc.perform(post("/api/messages/send")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(messageRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.content").value("Test message"))
                    .andExpect(jsonPath("$.senderId").value(1))
                    .andExpect(jsonPath("$.receiverId").value(2));

            verify(messageService, times(1)).sendMessage(eq(1L), any(MessageRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("❌ Debe retornar bad request al enviarse mensaje a sí mismo")
        void sendMessage_ShouldReturnBadRequest_WhenSendingToSelf() throws Exception {
            when(userService.findUserByEmail(anyString())).thenReturn(testUser);
            when(messageService.sendMessage(eq(1L), any(MessageRequest.class)))
                    .thenThrow(new IllegalArgumentException("Cannot send message to yourself"));

            messageRequest.setReceiverId(1L);

            mockMvc.perform(post("/api/messages/send")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(messageRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Cannot send message to yourself"));
        }
    }

    @Nested
    @DisplayName("GET /api/messages/conversation/{partnerId} Tests")
    class GetConversationTests {

        @Test
        @WithMockUser
        @DisplayName("✅ Debe obtener conversación exitosamente")
        void getConversation_ShouldReturnConversation() throws Exception {
            when(userService.findUserByEmail(anyString())).thenReturn(testUser);
            
            PageResponse<MessageResponse> pageResponse = PageResponse.<MessageResponse>builder()
                    .content(List.of(messageResponse))
                    .totalElements(1)
                    .totalPages(1)
                    .currentPage(0)
                    .size(50)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();

            when(messageService.getConversation(eq(1L), eq(2L), any())).thenReturn(pageResponse);

            mockMvc.perform(get("/api/messages/conversation/2")
                            .param("page", "0")
                            .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(messageService).markAsRead(1L, 2L);
        }
    }

    @Nested
    @DisplayName("GET /api/messages/conversations Tests")
    class GetUserConversationsTests {

        @Test
        @WithMockUser
        @DisplayName("✅ Debe obtener lista de conversaciones")
        void getUserConversations_ShouldReturnConversations() throws Exception {
            when(userService.findUserByEmail(anyString())).thenReturn(testUser);

            ConversationResponse conversation = ConversationResponse.builder()
                    .partnerId(2L)
                    .partnerName("Receiver User")
                    .partnerEmail("receiver@test.com")
                    .lastMessageContent("Test message")
                    .lastMessageTime(LocalDateTime.now())
                    .unreadCount(0L)
                    .totalMessages(1L)
                    .build();

            when(messageService.getUserConversations(1L)).thenReturn(List.of(conversation));

            mockMvc.perform(get("/api/messages/conversations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].partnerId").value(2))
                    .andExpect(jsonPath("$[0].partnerName").value("Receiver User"));
        }
    }

    @Nested
    @DisplayName("GET /api/messages/unread/count Tests")
    class GetUnreadCountTests {

        @Test
        @WithMockUser
        @DisplayName("✅ Debe obtener conteo de mensajes no leídos")
        void getUnreadCount_ShouldReturnCount() throws Exception {
            when(userService.findUserByEmail(anyString())).thenReturn(testUser);
            when(messageService.getUnreadCount(1L)).thenReturn(5L);

            mockMvc.perform(get("/api/messages/unread/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount").value(5));
        }
    }

    @Nested
    @DisplayName("POST /api/messages/mark-read/{senderId} Tests")
    class MarkAsReadTests {

        @Test
        @WithMockUser
        @DisplayName("✅ Debe marcar mensajes como leídos")
        void markAsRead_ShouldReturnMarkedCount() throws Exception {
            when(userService.findUserByEmail(anyString())).thenReturn(testUser);
            when(messageService.markAsRead(1L, 2L)).thenReturn(3);

            mockMvc.perform(post("/api/messages/mark-read/2")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.markedCount").value(3));
        }
    }

    @Nested
    @DisplayName("POST /api/messages/mark-all-read Tests")
    class MarkAllAsReadTests {

        @Test
        @WithMockUser
        @DisplayName("✅ Debe marcar todos los mensajes como leídos")
        void markAllAsRead_ShouldReturnMarkedCount() throws Exception {
            when(userService.findUserByEmail(anyString())).thenReturn(testUser);
            when(messageService.markAllAsRead(1L)).thenReturn(10);

            mockMvc.perform(post("/api/messages/mark-all-read")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.markedCount").value(10));
        }
    }

    @Nested
    @DisplayName("GET /api/messages/{messageId} Tests")
    class GetMessageByIdTests {

        @Test
        @WithMockUser
        @DisplayName("✅ Debe obtener mensaje por ID")
        void getMessageById_ShouldReturnMessage() throws Exception {
            when(messageService.getMessageById(1L)).thenReturn(messageResponse);

            mockMvc.perform(get("/api/messages/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.content").value("Test message"));
        }

        @Test
        @WithMockUser
        @DisplayName("❌ Debe retornar error cuando mensaje no existe")
        void getMessageById_ShouldReturnError_WhenNotFound() throws Exception {
            when(messageService.getMessageById(999L)).thenThrow(new Exception("Message not found"));

            mockMvc.perform(get("/api/messages/999"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @WithMockUser
        @DisplayName("❌ Debe validar que receiverId es requerido")
        void sendMessage_ShouldValidateReceiverId() throws Exception {
            messageRequest.setReceiverId(null);

            mockMvc.perform(post("/api/messages/send")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(messageRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("❌ Debe validar que content es requerido")
        void sendMessage_ShouldValidateContent() throws Exception {
            messageRequest.setContent(null);

            mockMvc.perform(post("/api/messages/send")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(messageRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}