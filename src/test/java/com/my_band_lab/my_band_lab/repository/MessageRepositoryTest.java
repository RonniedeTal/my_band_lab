package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Message;
import com.my_band_lab.my_band_lab.entity.Role;
import com.my_band_lab.my_band_lab.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("MessageRepository Tests")
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;
    private static long counter = 0;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        userRepository.deleteAll();
        
        counter++;
        user1 = userRepository.save(User.builder()
                .name("User1")
                .surname("Test")
                .email("user1_" + counter + "@test.com")
                .password("password")
                .role(Role.USER)
                .build());

        user2 = userRepository.save(User.builder()
                .name("User2")
                .surname("Test")
                .email("user2_" + counter + "@test.com")
                .password("password")
                .role(Role.ARTIST)
                .build());

        user3 = userRepository.save(User.builder()
                .name("User3")
                .surname("Test")
                .email("user3_" + counter + "@test.com")
                .password("password")
                .role(Role.USER)
                .build());
    }

    @Test
    @DisplayName("✅ Debe guardar un mensaje correctamente")
    void save_ShouldPersistMessage() {
        Message message = Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Hello, this is a test message")
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getContent()).isEqualTo("Hello, this is a test message");
        assertThat(saved.getSender().getId()).isEqualTo(user1.getId());
        assertThat(saved.getReceiver().getId()).isEqualTo(user2.getId());
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("✅ Debe encontrar conversación entre dos usuarios")
    void findConversation_ShouldReturnMessagesBetweenUsers() {
        for (int i = 0; i < 5; i++) {
            messageRepository.save(Message.builder()
                    .sender(user1)
                    .receiver(user2)
                    .content("Message " + i)
                    .isRead(false)
                    .build());
        }
        for (int i = 0; i < 3; i++) {
            messageRepository.save(Message.builder()
                    .sender(user1)
                    .receiver(user3)
                    .content("Message with user3 " + i)
                    .isRead(false)
                    .build());
        }

        Page<Message> conversation = messageRepository.findConversation(
                user1.getId(), user2.getId(), PageRequest.of(0, 10));

        assertThat(conversation.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("✅ Debe encontrar lista de conversación entre dos usuarios")
    void findConversationList_ShouldReturnAllMessages() {
        for (int i = 0; i < 10; i++) {
            messageRepository.save(Message.builder()
                    .sender(i % 2 == 0 ? user1 : user2)
                    .receiver(i % 2 == 0 ? user2 : user1)
                    .content("Conversation message " + i)
                    .isRead(false)
                    .build());
        }

        List<Message> conversation = messageRepository.findConversationList(user1.getId(), user2.getId());

        assertThat(conversation).hasSize(10);
    }

    @Test
    @DisplayName("✅ Debe encontrar mensajes no leídos para un usuario")
    void findUnreadMessages_ShouldReturnUnreadMessagesForReceiver() {
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Unread message 1")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Unread message 2")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Read message")
                .isRead(true)
                .build());

        List<Message> unread = messageRepository.findUnreadMessages(user2.getId());

        assertThat(unread).hasSize(2);
        assertThat(unread).allMatch(m -> !m.getIsRead());
    }

    @Test
    @DisplayName("✅ Debe contar mensajes no leídos correctamente")
    void countUnreadByReceiverId_ShouldReturnCorrectCount() {
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Unread 1")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Unread 2")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user3)
                .receiver(user2)
                .content("Unread from user3")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Read")
                .isRead(true)
                .build());

        Long count = messageRepository.countUnreadByReceiverId(user2.getId());

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("✅ Debe contar mensajes no leídos de un remitente específico")
    void countUnreadByReceiverAndSender_ShouldReturnCorrectCount() {
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Unread 1")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Unread 2")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user3)
                .receiver(user2)
                .content("From user3")
                .isRead(false)
                .build());

        Long count = messageRepository.countUnreadByReceiverAndSender(user2.getId(), user1.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("✅ Debe encontrar todos los mensajes de un usuario (enviados y recibidos)")
    void findAllByUserId_ShouldReturnAllUserMessages() {
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Sent to user2")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user3)
                .receiver(user1)
                .content("Received from user3")
                .isRead(false)
                .build());

        Page<Message> allMessages = messageRepository.findAllByUserId(user1.getId(), PageRequest.of(0, 10));

        assertThat(allMessages.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("✅ Debe encontrar IDs de partners de conversación")
    void findConversationPartnerIds_ShouldReturnPartnerIds() {
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Message to user2")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user3)
                .content("Message to user3")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user3)
                .receiver(user1)
                .content("Message from user3")
                .isRead(false)
                .build());

        List<Long> partners = messageRepository.findConversationPartnerIds(user1.getId());

        assertThat(partners).containsExactlyInAnyOrder(user2.getId(), user3.getId());
    }

    @Test
    @DisplayName("✅ Debe encontrar el último mensaje de una conversación")
    void findLatestMessageInConversation_ShouldReturnLastMessage() {
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("First message")
                .isRead(false)
                .build());
        Message lastMessage = messageRepository.save(Message.builder()
                .sender(user2)
                .receiver(user1)
                .content("Last message")
                .isRead(false)
                .build());

        Optional<Message> latest = messageRepository.findLatestMessageInConversation(
                user1.getId(), user2.getId());

        assertThat(latest).isPresent();
        assertThat(latest.get().getContent()).isEqualTo("Last message");
    }

    @Test
    @DisplayName("✅ Debe marcar mensajes como leídos")
    void markAsRead_ShouldMarkMessagesAsRead() {
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Unread message 1")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Unread message 2")
                .isRead(false)
                .build());

        int marked = messageRepository.markAsRead(user2.getId(), user1.getId());

        assertThat(marked).isEqualTo(2);
        
        List<Message> unread = messageRepository.findUnreadMessages(user2.getId());
        assertThat(unread).isEmpty();
    }

    @Test
    @DisplayName("✅ Debe marcar todos los mensajes como leídos para un usuario")
    void markAllAsReadByReceiver_ShouldMarkAllAsRead() {
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("From user1")
                .isRead(false)
                .build());
        messageRepository.save(Message.builder()
                .sender(user3)
                .receiver(user2)
                .content("From user3")
                .isRead(false)
                .build());

        int marked = messageRepository.markAllAsReadByReceiver(user2.getId());

        assertThat(marked).isEqualTo(2);
    }

    @Test
    @DisplayName("✅ Debe encontrar último mensaje entre dos usuarios")
    void findLatestMessageBetweenUsers_ShouldReturnLastMessage() {
        messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Message 1")
                .isRead(false)
                .build());
        Message last = messageRepository.save(Message.builder()
                .sender(user2)
                .receiver(user1)
                .content("Message 2")
                .isRead(false)
                .build());

        Optional<Message> latest = messageRepository.findLatestMessageBetweenUsers(
                user1.getId(), user2.getId());

        assertThat(latest).isPresent();
        assertThat(latest.get().getId()).isEqualTo(last.getId());
    }

    @Test
    @DisplayName("✅ Debe paginar mensajes enviados")
    void findBySenderIdOrderByCreatedAtDesc_ShouldReturnPaginatedSentMessages() {
        for (int i = 0; i < 15; i++) {
            messageRepository.save(Message.builder()
                    .sender(user1)
                    .receiver(user2)
                    .content("Sent message " + i)
                    .isRead(false)
                    .build());
        }

        Page<Message> page1 = messageRepository.findBySenderIdOrderByCreatedAtDesc(
                user1.getId(), PageRequest.of(0, 5));
        Page<Message> page2 = messageRepository.findBySenderIdOrderByCreatedAtDesc(
                user1.getId(), PageRequest.of(1, 5));

        assertThat(page1.getContent()).hasSize(5);
        assertThat(page2.getContent()).hasSize(5);
        assertThat(page1.getTotalElements()).isEqualTo(15);
    }

    @Test
    @DisplayName("✅ Debe encontrar mensajes recibidos paginados")
    void findByReceiverIdOrderByCreatedAtDesc_ShouldReturnPaginatedReceivedMessages() {
        for (int i = 0; i < 10; i++) {
            messageRepository.save(Message.builder()
                    .sender(user1)
                    .receiver(user2)
                    .content("Received message " + i)
                    .isRead(false)
                    .build());
        }

        Page<Message> received = messageRepository.findByReceiverIdOrderByCreatedAtDesc(
                user2.getId(), PageRequest.of(0, 10));

        assertThat(received.getContent()).hasSize(10);
    }

    @Test
    @DisplayName("❌ No debe encontrar conversación cuando no existe")
    void findConversation_ShouldReturnEmpty_WhenNoMessages() {
        Page<Message> conversation = messageRepository.findConversation(
                user1.getId(), user2.getId(), PageRequest.of(0, 10));

        assertThat(conversation.getContent()).isEmpty();
    }

    @Test
    @DisplayName("❌ No debe encontrar último mensaje cuando no hay mensajes")
    void findLatestMessageBetweenUsers_ShouldReturnEmpty_WhenNoMessages() {
        Optional<Message> latest = messageRepository.findLatestMessageBetweenUsers(
                user1.getId(), user2.getId());

        assertThat(latest).isEmpty();
    }

    @Test
    @DisplayName("❌ No debe contar mensajes no leídos cuando no hay")
    void countUnreadByReceiverId_ShouldReturnZero_WhenNoUnreadMessages() {
        Long count = messageRepository.countUnreadByReceiverId(user1.getId());

        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("✅ Debe eliminar un mensaje")
    void delete_ShouldRemoveMessage() {
        Message message = messageRepository.save(Message.builder()
                .sender(user1)
                .receiver(user2)
                .content("Message to delete")
                .isRead(false)
                .build());

        messageRepository.delete(message);

        Optional<Message> found = messageRepository.findById(message.getId());
        assertThat(found).isEmpty();
    }
}