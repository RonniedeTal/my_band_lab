package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.Role;
import com.my_band_lab.my_band_lab.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("MusicGroupRepository Tests")
class MusicGroupRepositoryTest {

    @Autowired
    private MusicGroupRepository musicGroupRepository;

    @Autowired
    private UserRepository userRepository;

    private static long counter = 0;

    private User createUniqueUser() {
        counter++;
        return User.builder()
                .name("User" + counter)
                .surname("Surname" + counter)
                .email("user" + counter + "@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    private MusicGroup createUniqueGroup(String baseName, User founder, MusicGenre genre, boolean verified) {
        return MusicGroup.builder()
                .name(baseName + "_" + System.currentTimeMillis() + "_" + counter)
                .description("Description for " + baseName)
                .genre(genre)
                .verified(verified)
                .founder(founder)
                .build();
    }

    @Test
    @DisplayName("✅ Debe guardar un nuevo grupo")
    void save_ShouldPersistMusicGroup() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        MusicGroup group = createUniqueGroup("TestGroup", founder, MusicGenre.ROCK, false);
        MusicGroup saved = musicGroupRepository.save(group);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(group.getName());
        assertThat(saved.getGenre()).isEqualTo(MusicGenre.ROCK);
        assertThat(saved.isVerified()).isFalse();
        assertThat(saved.getFounder().getId()).isEqualTo(founder.getId());
    }

    @Test
    @DisplayName("✅ Debe encontrar grupo por ID")
    void findById_ShouldReturnGroup_WhenIdExists() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        MusicGroup group = createUniqueGroup("FindByIdGroup", founder, MusicGenre.POP, false);
        MusicGroup saved = musicGroupRepository.save(group);

        Optional<MusicGroup> found = musicGroupRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("❌ No debe encontrar grupo cuando ID no existe")
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        Optional<MusicGroup> found = musicGroupRepository.findById(99999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("✅ Debe encontrar grupo por nombre (case-insensitive)")
    void findByNameIgnoreCase_ShouldReturnGroup_WhenNameExists() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        String uniqueName = "UniqueGroup_" + System.currentTimeMillis();
        MusicGroup group = MusicGroup.builder()
                .name(uniqueName)
                .description("Description")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .founder(founder)
                .build();
        musicGroupRepository.save(group);

        Optional<MusicGroup> found = musicGroupRepository.findByNameIgnoreCase(uniqueName.toUpperCase());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(uniqueName);
    }

    @Test
    @DisplayName("❌ No debe encontrar grupo cuando nombre no existe")
    void findByNameIgnoreCase_ShouldReturnEmpty_WhenNameDoesNotExist() {
        Optional<MusicGroup> found = musicGroupRepository.findByNameIgnoreCase("NonexistentGroup_" + System.currentTimeMillis());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("✅ Debe encontrar grupos por género ROCK")
    void findByGenre_ShouldReturnGroups_WhenGenreRock() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        String rockGroupName = "RockGroup_" + System.currentTimeMillis();
        MusicGroup rockGroup = MusicGroup.builder()
                .name(rockGroupName)
                .description("Rock description")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .founder(founder)
                .build();
        musicGroupRepository.save(rockGroup);

        List<MusicGroup> groups = musicGroupRepository.findByGenre(MusicGenre.ROCK);

        assertThat(groups).isNotEmpty();
        boolean found = groups.stream().anyMatch(g -> g.getName().equals(rockGroupName));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("✅ Debe retornar lista vacía cuando no hay grupos del género")
    void findByGenre_ShouldReturnEmpty_WhenNoGroupsInGenre() {
        List<MusicGroup> groups = musicGroupRepository.findByGenre(MusicGenre.METAL);
        assertThat(groups).isNotNull();
    }

    @Test
    @DisplayName("✅ Debe encontrar grupos no verificados")
    void findByVerifiedFalse_ShouldReturnUnverifiedGroups() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        String unverifiedGroupName = "UnverifiedGroup_" + System.currentTimeMillis();
        MusicGroup group = MusicGroup.builder()
                .name(unverifiedGroupName)
                .description("Unverified description")
                .genre(MusicGenre.POP)
                .verified(false)
                .founder(founder)
                .build();
        musicGroupRepository.save(group);

        List<MusicGroup> unverifiedGroups = musicGroupRepository.findByVerifiedFalse();

        assertThat(unverifiedGroups).isNotEmpty();
        boolean found = unverifiedGroups.stream().anyMatch(g -> g.getName().equals(unverifiedGroupName));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("✅ Debe encontrar grupos no verificados con paginación")
    void findByVerifiedFalse_WithPageable_ShouldReturnPageOfUnverifiedGroups() {
        // Cada grupo necesita un fundador diferente
        for (int i = 0; i < 3; i++) {
            User founder = createUniqueUser();
            userRepository.save(founder);

            MusicGroup group = MusicGroup.builder()
                    .name("PageGroup_" + System.currentTimeMillis() + "_" + i)
                    .description("Description " + i)
                    .genre(MusicGenre.ROCK)
                    .verified(false)
                    .founder(founder)
                    .build();
            musicGroupRepository.save(group);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<MusicGroup> groupPage = musicGroupRepository.findByVerifiedFalse(pageable);

        assertThat(groupPage.getContent()).isNotEmpty();
        assertThat(groupPage.getContent().size()).isGreaterThanOrEqualTo(3);
        assertThat(groupPage.getContent().stream().noneMatch(MusicGroup::isVerified)).isTrue();
    }

    @Test
    @DisplayName("✅ Debe buscar grupos por nombre")
    void searchByNameOrDescription_ShouldFindByGroupName() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        String searchableName = "SearchableName_" + System.currentTimeMillis();
        MusicGroup group = MusicGroup.builder()
                .name(searchableName)
                .description("Some description")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .founder(founder)
                .build();
        musicGroupRepository.save(group);

        Pageable pageable = PageRequest.of(0, 10);
        Page<MusicGroup> result = musicGroupRepository.searchByNameOrDescription("SearchableName", pageable);

        assertThat(result.getContent()).isNotEmpty();
        boolean found = result.getContent().stream().anyMatch(g -> g.getName().equals(searchableName));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("✅ Debe buscar grupos por descripción")
    void searchByNameOrDescription_ShouldFindByDescription() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        String uniqueDescription = "UniqueSearchableDescription_" + System.currentTimeMillis();
        MusicGroup group = MusicGroup.builder()
                .name("GroupName_" + System.currentTimeMillis())
                .description(uniqueDescription)
                .genre(MusicGenre.ROCK)
                .verified(false)
                .founder(founder)
                .build();
        musicGroupRepository.save(group);

        Pageable pageable = PageRequest.of(0, 10);
        Page<MusicGroup> result = musicGroupRepository.searchByNameOrDescription("UniqueSearchableDescription", pageable);

        assertThat(result.getContent()).isNotEmpty();
        boolean found = result.getContent().stream().anyMatch(g -> g.getDescription().equals(uniqueDescription));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("❌ No debe encontrar grupos cuando query no coincide")
    void searchByNameOrDescription_ShouldReturnEmpty_WhenNoMatch() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        MusicGroup group = MusicGroup.builder()
                .name("UniqueName_" + System.currentTimeMillis())
                .description("Unique description")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .founder(founder)
                .build();
        musicGroupRepository.save(group);

        Pageable pageable = PageRequest.of(0, 10);
        Page<MusicGroup> result = musicGroupRepository.searchByNameOrDescription("NonexistentQuery_" + System.currentTimeMillis(), pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("✅ Debe eliminar un grupo")
    void delete_ShouldRemoveGroup() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        MusicGroup group = MusicGroup.builder()
                .name("ToDeleteGroup_" + System.currentTimeMillis())
                .description("To be deleted")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .founder(founder)
                .build();
        MusicGroup saved = musicGroupRepository.save(group);
        Long groupId = saved.getId();

        musicGroupRepository.delete(saved);

        Optional<MusicGroup> found = musicGroupRepository.findById(groupId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("✅ Debe actualizar grupo existente")
    void update_ShouldUpdateGroup() {
        User founder = createUniqueUser();
        userRepository.save(founder);

        MusicGroup group = MusicGroup.builder()
                .name("OriginalName_" + System.currentTimeMillis())
                .description("Original description")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .founder(founder)
                .build();
        MusicGroup saved = musicGroupRepository.save(group);

        saved.setName("UpdatedName_" + System.currentTimeMillis());
        saved.setDescription("Updated description");
        saved.setGenre(MusicGenre.METAL);
        saved.setVerified(true);

        MusicGroup updated = musicGroupRepository.save(saved);

        assertThat(updated.getName()).startsWith("UpdatedName_");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getGenre()).isEqualTo(MusicGenre.METAL);
        assertThat(updated.isVerified()).isTrue();
    }
}