package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.Role;
import com.my_band_lab.my_band_lab.entity.User;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MusicGroupService Tests")
class MusicGroupServiceTest {

    @Mock
    private MusicGroupRepository musicGroupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private MusicGroupServiceImpl musicGroupService;

    private User founderUser;
    private User normalUser;
    private User artistUser;
    private MusicGroup testGroup;
    private MusicGroup verifiedGroup;
    private MusicGroup unverifiedGroup;

    @BeforeEach
    void setUp() {
        // Usuario fundador
        founderUser = User.builder()
                .id(1L)
                .name("Founder")
                .surname("User")
                .email("founder@example.com")
                .password("encoded")
                .role(Role.USER)
                .musicGroups(new ArrayList<>())
                .build();

        // Usuario normal para ser miembro
        normalUser = User.builder()
                .id(2L)
                .name("Normal")
                .surname("User")
                .email("normal@example.com")
                .password("encoded")
                .role(Role.USER)
                .musicGroups(new ArrayList<>())
                .build();

        // Usuario artista
        artistUser = User.builder()
                .id(3L)
                .name("Artist")
                .surname("User")
                .email("artist@example.com")
                .password("encoded")
                .role(Role.ARTIST)
                .musicGroups(new ArrayList<>())
                .build();

        // Grupo de prueba
        testGroup = MusicGroup.builder()
                .id(1L)
                .name("Test Group")
                .description("Test description")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .founder(founderUser)
                .members(new ArrayList<>())
                .build();
        testGroup.getMembers().add(founderUser);
        founderUser.getMusicGroups().add(testGroup);

        // Grupo verificado
        verifiedGroup = MusicGroup.builder()
                .id(2L)
                .name("Verified Group")
                .description("Verified description")
                .genre(MusicGenre.POP)
                .verified(true)
                .founder(artistUser)
                .members(new ArrayList<>())
                .build();
        verifiedGroup.getMembers().add(artistUser);
        artistUser.getMusicGroups().add(verifiedGroup);

        // Grupo no verificado
        unverifiedGroup = MusicGroup.builder()
                .id(3L)
                .name("Unverified Group")
                .description("Unverified description")
                .genre(MusicGenre.JAZZ)
                .verified(false)
                .founder(normalUser)
                .members(new ArrayList<>())
                .build();
        unverifiedGroup.getMembers().add(normalUser);
        normalUser.getMusicGroups().add(unverifiedGroup);
    }

    // ==================== TESTS: createGroup ====================

    @Nested
    @DisplayName("createGroup Tests")
    class CreateGroupTests {

        @Test
        @DisplayName("✅ Debe crear grupo exitosamente con founderId")
        void createGroup_ShouldCreateGroup_WithFounderId() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(founderUser));
            when(musicGroupRepository.findByNameIgnoreCase("New Group")).thenReturn(Optional.empty());
            when(musicGroupRepository.save(any(MusicGroup.class))).thenAnswer(invocation -> {
                MusicGroup group = invocation.getArgument(0);
                group.setId(4L);
                return group;
            });

            MusicGroup result = musicGroupService.createGroup("New Group", "New description", MusicGenre.ROCK, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Group");
            assertThat(result.getGenre()).isEqualTo(MusicGenre.ROCK);
            assertThat(result.isVerified()).isFalse();
            assertThat(result.getFounder().getId()).isEqualTo(1L);
            verify(musicGroupRepository, times(1)).save(any(MusicGroup.class));
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando el nombre del grupo ya existe")
        void createGroup_ShouldThrowException_WhenNameExists() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(founderUser));
            when(musicGroupRepository.findByNameIgnoreCase("Test Group")).thenReturn(Optional.of(testGroup));

            assertThatThrownBy(() -> musicGroupService.createGroup("Test Group", "Description", MusicGenre.ROCK, 1L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Group name already exists");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando fundador no existe")
        void createGroup_ShouldThrowException_WhenFounderNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> musicGroupService.createGroup("New Group", "Description", MusicGenre.ROCK, 999L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Founder user not found");
        }
    }

    // ==================== TESTS: addMember ====================

    @Nested
    @DisplayName("addMember Tests")
    class AddMemberTests {

        @Test
        @DisplayName("✅ Debe añadir miembro exitosamente")
        void addMember_ShouldAddMember_WhenValid() throws Exception {
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
            setupSecurityContext("founder@example.com", founderUser);
            when(userRepository.findByEmailIgnoreCase("founder@example.com")).thenReturn(Optional.of(founderUser));
            when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
            when(musicGroupRepository.save(any(MusicGroup.class))).thenReturn(testGroup);

            MusicGroup result = musicGroupService.addMember(1L, 2L);

            assertThat(result).isNotNull();
            assertThat(result.getMembers()).contains(normalUser);
            verify(musicGroupRepository, times(1)).save(any(MusicGroup.class));
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario no es fundador")
        void addMember_ShouldThrowException_WhenUserIsNotFounder() throws Exception {
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
            setupSecurityContext("normal@example.com", normalUser);
            when(userRepository.findByEmailIgnoreCase("normal@example.com")).thenReturn(Optional.of(normalUser));

            assertThatThrownBy(() -> musicGroupService.addMember(1L, 2L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Only the group founder can add members");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando el usuario ya es miembro")
        void addMember_ShouldThrowException_WhenUserAlreadyMember() throws Exception {
            testGroup.getMembers().add(normalUser);
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
            setupSecurityContext("founder@example.com", founderUser);
            when(userRepository.findByEmailIgnoreCase("founder@example.com")).thenReturn(Optional.of(founderUser));
            when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

            assertThatThrownBy(() -> musicGroupService.addMember(1L, 2L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("User is already a member of this group");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando grupo no existe")
        void addMember_ShouldThrowException_WhenGroupNotFound() {
            when(musicGroupRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> musicGroupService.addMember(999L, 2L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Group not found");
        }
    }

    // ==================== TESTS: removeMember ====================

    @Nested
    @DisplayName("removeMember Tests")
    class RemoveMemberTests {

        @Test
        @DisplayName("✅ Debe remover miembro exitosamente")
        void removeMember_ShouldRemoveMember_WhenValid() throws Exception {
            testGroup.getMembers().add(normalUser);
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
            setupSecurityContext("founder@example.com", founderUser);
            when(userRepository.findByEmailIgnoreCase("founder@example.com")).thenReturn(Optional.of(founderUser));
            when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
            when(musicGroupRepository.save(any(MusicGroup.class))).thenReturn(testGroup);

            MusicGroup result = musicGroupService.removeMember(1L, 2L);

            assertThat(result).isNotNull();
            assertThat(result.getMembers()).doesNotContain(normalUser);
            verify(musicGroupRepository, times(1)).save(any(MusicGroup.class));
        }

        @Test
        @DisplayName("❌ No debe permitir remover al fundador")
        void removeMember_ShouldThrowException_WhenRemovingFounder() throws Exception {
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
            setupSecurityContext("founder@example.com", founderUser);
            when(userRepository.findByEmailIgnoreCase("founder@example.com")).thenReturn(Optional.of(founderUser));
            when(userRepository.findById(1L)).thenReturn(Optional.of(founderUser));

            assertThatThrownBy(() -> musicGroupService.removeMember(1L, 1L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Cannot remove the group founder");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario no es fundador")
        void removeMember_ShouldThrowException_WhenUserIsNotFounder() throws Exception {
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
            setupSecurityContext("normal@example.com", normalUser);
            when(userRepository.findByEmailIgnoreCase("normal@example.com")).thenReturn(Optional.of(normalUser));

            assertThatThrownBy(() -> musicGroupService.removeMember(1L, 2L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Only the group founder can remove members");
        }
    }

    // ==================== TESTS: getGroupMembers ====================

    @Nested
    @DisplayName("getGroupMembers Tests")
    class GetGroupMembersTests {

        @Test
        @DisplayName("✅ Debe retornar miembros del grupo")
        void getGroupMembers_ShouldReturnMembers() throws Exception {
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));

            List<User> members = musicGroupService.getGroupMembers(1L);

            assertThat(members).isNotEmpty();
            assertThat(members).contains(founderUser);
            verify(musicGroupRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando grupo no existe")
        void getGroupMembers_ShouldThrowException_WhenGroupNotFound() {
            when(musicGroupRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> musicGroupService.getGroupMembers(999L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Group not found");
        }
    }

    // ==================== TESTS: updateGroupGenre ====================

    @Nested
    @DisplayName("updateGroupGenre Tests")
    class UpdateGroupGenreTests {

        @Test
        @DisplayName("✅ Debe actualizar género del grupo")
        void updateGroupGenre_ShouldUpdateGenre() throws Exception {
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
            when(musicGroupRepository.save(any(MusicGroup.class))).thenReturn(testGroup);

            MusicGroup result = musicGroupService.updateGroupGenre(1L, MusicGenre.METAL);

            assertThat(result.getGenre()).isEqualTo(MusicGenre.METAL);
            verify(musicGroupRepository, times(1)).save(testGroup);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando grupo no existe")
        void updateGroupGenre_ShouldThrowException_WhenGroupNotFound() {
            when(musicGroupRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> musicGroupService.updateGroupGenre(999L, MusicGenre.ROCK))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Group not found");
        }
    }

    // ==================== TESTS: getGroupsByGenre ====================

    @Nested
    @DisplayName("getGroupsByGenre Tests")
    class GetGroupsByGenreTests {

        @Test
        @DisplayName("✅ Debe retornar grupos por género")
        void getGroupsByGenre_ShouldReturnGroups() throws Exception {
            List<MusicGroup> groups = List.of(testGroup);
            when(musicGroupRepository.findAll()).thenReturn(groups);

            List<MusicGroup> result = musicGroupService.getGroupsByGenre(MusicGenre.ROCK);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getGenre()).isEqualTo(MusicGenre.ROCK);
        }
    }

    // ==================== TESTS: getAllGroups ====================

    @Nested
    @DisplayName("getAllGroups Tests")
    class GetAllGroupsTests {

        @Test
        @DisplayName("✅ Debe retornar todos los grupos")
        void getAllGroups_ShouldReturnAllGroups() throws Exception {
            List<MusicGroup> groups = List.of(testGroup, verifiedGroup);
            when(musicGroupRepository.findAll()).thenReturn(groups);

            List<MusicGroup> result = musicGroupService.getAllGroups();

            assertThat(result).hasSize(2);
            verify(musicGroupRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando no hay grupos")
        void getAllGroups_ShouldThrowException_WhenNoGroups() throws Exception {
            when(musicGroupRepository.findAll()).thenReturn(List.of());

            assertThatThrownBy(() -> musicGroupService.getAllGroups())
                    .isInstanceOf(Exception.class)
                    .hasMessage("No music groups found");
        }
    }

    // ==================== TESTS: getGroupById ====================

    @Nested
    @DisplayName("getGroupById Tests")
    class GetGroupByIdTests {

        @Test
        @DisplayName("✅ Debe retornar grupo por ID")
        void getGroupById_ShouldReturnGroup() throws Exception {
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));

            MusicGroup result = musicGroupService.getGroupById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Group");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando grupo no existe")
        void getGroupById_ShouldThrowException_WhenNotFound() {
            when(musicGroupRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> musicGroupService.getGroupById(999L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Music group not found with id: 999");
        }
    }

    // ==================== TESTS: deleteGroup ====================

    @Nested
    @DisplayName("deleteGroup Tests")
    class DeleteGroupTests {

        @Test
        @DisplayName("✅ Debe eliminar grupo exitosamente")
        void deleteGroup_ShouldDeleteGroup() throws Exception {
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
            doNothing().when(musicGroupRepository).delete(testGroup);

            musicGroupService.deleteGroup(1L);

            verify(musicGroupRepository, times(1)).delete(testGroup);
        }
    }

    // ==================== TESTS: getAllGroupsPaginated ====================

    @Nested
    @DisplayName("getAllGroupsPaginated Tests")
    class GetAllGroupsPaginatedTests {

        @Test
        @DisplayName("✅ Debe retornar página de grupos")
        void getAllGroupsPaginated_ShouldReturnPage() throws Exception {
            List<MusicGroup> groups = List.of(testGroup, verifiedGroup);
            Page<MusicGroup> groupPage = new PageImpl<>(groups, PageRequest.of(0, 10), groups.size());
            when(musicGroupRepository.findAll(any(Pageable.class))).thenReturn(groupPage);

            PageResponse<MusicGroup> result = musicGroupService.getAllGroupsPaginated(0, 10);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getCurrentPage()).isEqualTo(0);
        }
    }

    // ==================== TESTS: searchGroups ====================

    @Nested
    @DisplayName("searchGroups Tests")
    class SearchGroupsTests {

        @Test
        @DisplayName("✅ Debe buscar grupos por nombre o descripción")
        void searchGroups_ShouldReturnGroups() throws Exception {
            List<MusicGroup> groups = List.of(testGroup);
            Page<MusicGroup> groupPage = new PageImpl<>(groups, PageRequest.of(0, 10), groups.size());
            when(musicGroupRepository.searchByNameOrDescription(eq("Test"), any(Pageable.class))).thenReturn(groupPage);

            PageResponse<MusicGroup> result = musicGroupService.searchGroups("Test", 0, 10);

            assertThat(result.getContent()).hasSize(1);
            verify(musicGroupRepository, times(1)).searchByNameOrDescription(eq("Test"), any(Pageable.class));
        }
    }

    // ==================== TESTS: getUnverifiedGroups ====================

    @Nested
    @DisplayName("getUnverifiedGroups Tests")
    class GetUnverifiedGroupsTests {

        @Test
        @DisplayName("✅ Debe retornar grupos no verificados")
        void getUnverifiedGroups_ShouldReturnUnverifiedGroups() throws Exception {
            List<MusicGroup> unverifiedGroups = List.of(testGroup, unverifiedGroup);
            when(musicGroupRepository.findByVerifiedFalse()).thenReturn(unverifiedGroups);

            List<MusicGroup> result = musicGroupService.getUnverifiedGroups();

            assertThat(result).hasSize(2);
            assertThat(result.stream().noneMatch(MusicGroup::isVerified)).isTrue();
        }

        @Test
        @DisplayName("✅ Debe retornar lista vacía cuando no hay grupos no verificados")
        void getUnverifiedGroups_ShouldReturnEmptyList_WhenNone() throws Exception {
            when(musicGroupRepository.findByVerifiedFalse()).thenReturn(new ArrayList<>());

            List<MusicGroup> result = musicGroupService.getUnverifiedGroups();

            assertThat(result).isEmpty();
        }
    }

    // ==================== TESTS: verifyGroup ====================

    @Nested
    @DisplayName("verifyGroup Tests")
    class VerifyGroupTests {

        @Test
        @DisplayName("✅ Debe verificar grupo exitosamente")
        void verifyGroup_ShouldVerifyGroup() throws Exception {
            when(musicGroupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
            when(musicGroupRepository.save(any(MusicGroup.class))).thenAnswer(invocation -> {
                MusicGroup group = invocation.getArgument(0);
                group.setVerified(true);
                return group;
            });

            MusicGroup result = musicGroupService.verifyGroup(1L);

            assertThat(result.isVerified()).isTrue();
            verify(musicGroupRepository, times(1)).save(testGroup);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando grupo ya está verificado")
        void verifyGroup_ShouldThrowException_WhenAlreadyVerified() throws Exception {
            when(musicGroupRepository.findById(2L)).thenReturn(Optional.of(verifiedGroup));

            assertThatThrownBy(() -> musicGroupService.verifyGroup(2L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Group is already verified");
        }
    }

    // ==================== TESTS: getUnverifiedGroupsPaginated ====================

    @Nested
    @DisplayName("getUnverifiedGroupsPaginated Tests")
    class GetUnverifiedGroupsPaginatedTests {

        @Test
        @DisplayName("✅ Debe retornar página de grupos no verificados")
        void getUnverifiedGroupsPaginated_ShouldReturnPage() throws Exception {
            List<MusicGroup> unverifiedGroups = List.of(testGroup);
            Page<MusicGroup> groupPage = new PageImpl<>(unverifiedGroups, PageRequest.of(0, 10), unverifiedGroups.size());
            when(musicGroupRepository.findByVerifiedFalse(any(Pageable.class))).thenReturn(groupPage);

            PageResponse<MusicGroup> result = musicGroupService.getUnverifiedGroupsPaginated(0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).isVerified()).isFalse();
            verify(musicGroupRepository, times(1)).findByVerifiedFalse(any(Pageable.class));
        }
    }

    // Helper method to setup security context
    private void setupSecurityContext(String email, User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }
}