package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.ArtistSummary;
import com.my_band_lab.my_band_lab.dto.GroupSummary;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.dto.UserAdminResponse;
import com.my_band_lab.my_band_lab.entity.*;
import com.my_band_lab.my_band_lab.repository.ArtistRepository;
import com.my_band_lab.my_band_lab.repository.MusicGroupRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MusicGroupRepository musicGroupRepository;

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // Datos de prueba
    private User adminUser;
    private User normalUser;
    private User artistUser;
    private User otroUser;
    private Artist artist;
    private MusicGroup group;

    @BeforeEach
    void setUp() {
        // ========== Usuario ADMIN ==========
        adminUser = User.builder()
                .id(1L)
                .name("Laura")
                .surname("Fernandez")
                .email("laura.fernandez@example.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .profileImageUrl(null)
                .musicGroups(new ArrayList<>())
                .build();

        // ========== Usuario normal (USER) - SIN GRUPOS ==========
        normalUser = User.builder()
                .id(2L)
                .name("Test")
                .surname("User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .profileImageUrl(null)
                .musicGroups(new ArrayList<>())  // ← Sin grupos inicialmente
                .build();

        // ========== Usuario ARTIST con artista ==========
        artistUser = User.builder()
                .id(3L)
                .name("Miguel")
                .surname("Rodriguez")
                .email("miguel.rodriguez@example.com")
                .password("encodedPassword")
                .role(Role.ARTIST)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .profileImageUrl(null)
                .musicGroups(new ArrayList<>())
                .build();

        artist = Artist.builder()
                .id(1L)
                .stageName("Miguel Rockero")
                .verified(true)
                .user(artistUser)
                .build();
        artistUser.setArtist(artist);

        // ========== Grupo con miembros ==========
        group = MusicGroup.builder()
                .id(1L)
                .name("Los Rockeros")
                .genre(MusicGenre.ROCK)
                .verified(true)
                .founder(artistUser)
                .members(new ArrayList<>())
                .build();
        group.getMembers().add(artistUser);
        // NOTA: NO añadimos normalUser al grupo para que el test pase

        artistUser.getMusicGroups().add(group);

        // ========== Otro usuario ==========
        otroUser = User.builder()
                .id(4L)
                .name("Ana")
                .surname("Martinez")
                .email("ana.martinez@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .profileImageUrl(null)
                .musicGroups(new ArrayList<>())
                .build();
    }

    // ==================== TESTS DE findUserById ====================

    @Nested
    @DisplayName("findUserById Tests")
    class FindUserByIdTests {

        @Test
        @DisplayName("✅ Debe retornar usuario cuando existe")
        void findUserById_ShouldReturnUser_WhenUserExists() throws Exception {
            when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

            User result = userService.findUserById(2L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getName()).isEqualTo("Test");
            verify(userRepository, times(1)).findById(2L);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario no existe")
        void findUserById_ShouldThrowException_WhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findUserById(999L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("User not found");
            verify(userRepository, times(1)).findById(999L);
        }
    }

    // ==================== TESTS DE getUserByIdForAdmin ====================

    @Nested
    @DisplayName("getUserByIdForAdmin Tests")
    class GetUserByIdForAdminTests {

        @Test
        @DisplayName("✅ Debe retornar UserAdminResponse para usuario normal")
        void getUserByIdForAdmin_ShouldReturnUserAdminResponse_ForNormalUser() throws Exception {
            when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

            UserAdminResponse response = userService.getUserByIdForAdmin(2L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(2L);
            assertThat(response.getName()).isEqualTo("Test");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getRole()).isEqualTo("USER");
            assertThat(response.getArtist()).isNull();
            assertThat(response.getGroups()).isEmpty();  // ← Ahora está vacío
            verify(userRepository, times(1)).findById(2L);
        }

        @Test
        @DisplayName("✅ Debe retornar UserAdminResponse para artista con grupos")
        void getUserByIdForAdmin_ShouldReturnUserAdminResponse_ForArtistWithGroups() throws Exception {
            when(userRepository.findById(3L)).thenReturn(Optional.of(artistUser));

            UserAdminResponse response = userService.getUserByIdForAdmin(3L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(3L);
            assertThat(response.getRole()).isEqualTo("ARTIST");

            assertThat(response.getArtist()).isNotNull();
            assertThat(response.getArtist().getId()).isEqualTo(1L);
            assertThat(response.getArtist().getStageName()).isEqualTo("Miguel Rockero");

            assertThat(response.getGroups()).hasSize(1);
            assertThat(response.getGroups().get(0).getId()).isEqualTo(1L);
            assertThat(response.getGroups().get(0).getRole()).isEqualTo("FOUNDER");

            verify(userRepository, times(1)).findById(3L);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario no existe")
        void getUserByIdForAdmin_ShouldThrowException_WhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByIdForAdmin(999L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("User not found");
            verify(userRepository, times(1)).findById(999L);
        }
    }

    // ==================== TESTS DE changeUserRole ====================

    @Nested
    @DisplayName("changeUserRole Tests")
    class ChangeUserRoleTests {

        @Test
        @DisplayName("✅ Debe cambiar rol de USER a ARTIST exitosamente")
        void changeUserRole_ShouldChangeUserToArtist_WhenValid() throws Exception {
            when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setRole(Role.ARTIST);
                return savedUser;
            });

            UserAdminResponse response = userService.changeUserRole(2L, "ARTIST", 1L);

            assertThat(response.getRole()).isEqualTo("ARTIST");
            verify(userRepository, times(1)).findById(2L);
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("✅ Debe cambiar rol de ARTIST a USER exitosamente")
        void changeUserRole_ShouldChangeArtistToUser_WhenValid() throws Exception {
            when(userRepository.findById(3L)).thenReturn(Optional.of(artistUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setRole(Role.USER);
                return savedUser;
            });

            UserAdminResponse response = userService.changeUserRole(3L, "USER", 1L);

            assertThat(response.getRole()).isEqualTo("USER");
            verify(userRepository, times(1)).findById(3L);
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("❌ No debe permitir cambiar el propio rol")
        void changeUserRole_ShouldThrowException_WhenChangingOwnRole() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            assertThatThrownBy(() -> userService.changeUserRole(1L, "USER", 1L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("You cannot change your own role");

            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando el rol es inválido")
        void changeUserRole_ShouldThrowException_WhenInvalidRole() {
            assertThatThrownBy(() -> userService.changeUserRole(2L, "INVALID", 1L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Invalid role. Valid roles: USER, ARTIST, ADMIN");

            verify(userRepository, never()).findById(any());
        }
    }

    // ==================== TESTS DE deleteUserByAdmin ====================

    @Nested
    @DisplayName("deleteUserByAdmin Tests")
    class DeleteUserByAdminTests {

        @Test
        @DisplayName("✅ Debe eliminar usuario normal sin grupos exitosamente")
        void deleteUserByAdmin_ShouldDeleteNormalUser_WhenNoGroups() throws Exception {
            when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
            doNothing().when(userRepository).delete(normalUser);

            userService.deleteUserByAdmin(2L, 1L);

            verify(userRepository, times(1)).findById(2L);
            verify(userRepository, times(1)).delete(normalUser);
        }

        @Test
        @DisplayName("❌ No debe permitir eliminar el propio usuario")
        void deleteUserByAdmin_ShouldThrowException_WhenDeletingOwnAccount() {
            // NO mockeamos findById porque la validación ocurre ANTES de llamarlo
            // Cuando userId == currentAdminId, se lanza excepción inmediatamente

            assertThatThrownBy(() -> userService.deleteUserByAdmin(1L, 1L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("You cannot delete your own account");

            // Verificamos que NUNCA se llamó a findById porque la validación falló primero
            verify(userRepository, never()).findById(any());
            verify(userRepository, never()).delete(any());
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando el usuario no existe")
        void deleteUserByAdmin_ShouldThrowException_WhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUserByAdmin(999L, 1L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("User not found");

            verify(userRepository, times(1)).findById(999L);
            verify(userRepository, never()).delete(any());
        }

        @Test
        @DisplayName("✅ Debe eliminar usuario que es miembro de un grupo (no fundador)")
        void deleteUserByAdmin_ShouldRemoveUserFromGroup_WhenUserIsMember() throws Exception {
            // Crear un usuario que es miembro de un grupo (pero no fundador)
            User memberUser = User.builder()
                    .id(5L)
                    .name("Member")
                    .surname("User")
                    .email("member@example.com")
                    .role(Role.USER)
                    .musicGroups(new ArrayList<>())
                    .build();

            MusicGroup testGroup = MusicGroup.builder()
                    .id(2L)
                    .name("Test Group")
                    .genre(MusicGenre.ROCK)
                    .founder(adminUser)
                    .members(new ArrayList<>())
                    .build();
            testGroup.getMembers().add(memberUser);
            memberUser.getMusicGroups().add(testGroup);

            when(userRepository.findById(5L)).thenReturn(Optional.of(memberUser));
            when(musicGroupRepository.save(any(MusicGroup.class))).thenReturn(testGroup);
            doNothing().when(userRepository).delete(memberUser);

            userService.deleteUserByAdmin(5L, 1L);

            assertThat(testGroup.getMembers()).doesNotContain(memberUser);
            verify(musicGroupRepository, times(1)).save(testGroup);
            verify(userRepository, times(1)).delete(memberUser);
        }

        @Test
        @DisplayName("✅ Debe transferir fundador cuando el fundador es eliminado y hay otros miembros")
        void deleteUserByAdmin_ShouldTransferFounder_WhenUserIsFounderWithMembers() throws Exception {
            // Crear un grupo con fundador y otro miembro
            User otherMember = User.builder()
                    .id(6L)
                    .name("Other")
                    .surname("Member")
                    .email("other@example.com")
                    .role(Role.USER)
                    .musicGroups(new ArrayList<>())
                    .build();

            MusicGroup testGroup = MusicGroup.builder()
                    .id(3L)
                    .name("Founder Group")
                    .genre(MusicGenre.ROCK)
                    .founder(artistUser)
                    .members(new ArrayList<>())
                    .build();
            testGroup.getMembers().add(artistUser);
            testGroup.getMembers().add(otherMember);
            artistUser.getMusicGroups().add(testGroup);
            otherMember.getMusicGroups().add(testGroup);

            when(userRepository.findById(3L)).thenReturn(Optional.of(artistUser));
            when(musicGroupRepository.save(any(MusicGroup.class))).thenReturn(testGroup);
            doNothing().when(userRepository).delete(artistUser);

            userService.deleteUserByAdmin(3L, 1L);

            assertThat(testGroup.getFounder()).isEqualTo(otherMember);
            assertThat(testGroup.getMembers()).doesNotContain(artistUser);
            verify(musicGroupRepository, times(1)).save(testGroup);
            verify(userRepository, times(1)).delete(artistUser);
        }

        @Test
        @DisplayName("✅ Debe eliminar grupo cuando el fundador es eliminado y no hay otros miembros")
        void deleteUserByAdmin_ShouldDeleteGroup_WhenUserIsFounderWithoutMembers() throws Exception {
            // Crear un grupo solo con el fundador
            MusicGroup emptyGroup = MusicGroup.builder()
                    .id(4L)
                    .name("Empty Group")
                    .genre(MusicGenre.POP)
                    .founder(otroUser)
                    .members(new ArrayList<>())
                    .build();
            emptyGroup.getMembers().add(otroUser);
            otroUser.getMusicGroups().add(emptyGroup);

            when(userRepository.findById(4L)).thenReturn(Optional.of(otroUser));
            doNothing().when(musicGroupRepository).delete(emptyGroup);
            doNothing().when(userRepository).delete(otroUser);

            userService.deleteUserByAdmin(4L, 1L);

            verify(musicGroupRepository, times(1)).delete(emptyGroup);
            verify(userRepository, times(1)).delete(otroUser);
        }

        @Test
        @DisplayName("✅ Debe eliminar artista cuando el usuario tiene perfil de artista")
        void deleteUserByAdmin_ShouldDeleteArtist_WhenUserHasArtistProfile() throws Exception {
            when(userRepository.findById(3L)).thenReturn(Optional.of(artistUser));
            doNothing().when(artistRepository).delete(artist);
            doNothing().when(userRepository).delete(artistUser);

            userService.deleteUserByAdmin(3L, 1L);

            verify(artistRepository, times(1)).delete(artist);
            verify(userRepository, times(1)).delete(artistUser);
        }
    }
}