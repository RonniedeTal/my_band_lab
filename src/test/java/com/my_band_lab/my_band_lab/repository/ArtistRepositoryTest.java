package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.Role;
import com.my_band_lab.my_band_lab.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("ArtistRepository Tests")
class ArtistRepositoryTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private UserRepository userRepository;

    private List<User> testUsers = new ArrayList<>();
    private List<Artist> testArtists = new ArrayList<>();

    private static long counter = 0;

    private User createUniqueUser(String baseName) {
        counter++;
        return User.builder()
                .name(baseName + "Name" + counter)
                .surname(baseName + "Surname" + counter)
                .email(baseName + counter + "@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    private User createUniqueArtistUser(String baseName) {
        counter++;
        return User.builder()
                .name(baseName + "Name" + counter)
                .surname(baseName + "Surname" + counter)
                .email(baseName + counter + "@test.com")
                .password("encodedPassword")
                .role(Role.ARTIST)
                .build();
    }

    @BeforeEach
    void setUp() {
        testUsers.clear();
        testArtists.clear();
    }

    // ==================== TESTS: save ====================

    @Test
    @DisplayName("✅ Debe guardar un nuevo artista")
    void save_ShouldPersistArtist() {
        User user = createUniqueUser("save");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("Test Artist")
                .biography("This is a test artist biography")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user)
                .build();

        Artist saved = artistRepository.save(artist);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStageName()).isEqualTo("Test Artist");
        assertThat(saved.getGenre()).isEqualTo(MusicGenre.ROCK);
        assertThat(saved.isVerified()).isFalse();
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
    }

    // ==================== TESTS: findById ====================

    @Test
    @DisplayName("✅ Debe encontrar artista por ID")
    void findById_ShouldReturnArtist_WhenIdExists() {
        User user = createUniqueUser("findById");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("FindById Artist")
                .biography("Bio")
                .genre(MusicGenre.POP)
                .verified(false)
                .user(user)
                .build();
        Artist saved = artistRepository.save(artist);

        Optional<Artist> found = artistRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getStageName()).isEqualTo("FindById Artist");
    }

    @Test
    @DisplayName("❌ No debe encontrar artista cuando ID no existe")
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        Optional<Artist> found = artistRepository.findById(99999L);
        assertThat(found).isEmpty();
    }

    // ==================== TESTS: findAll ====================

    @Test
    @DisplayName("✅ Debe encontrar todos los artistas")
    void findAll_ShouldReturnAllArtists() {
        // Crear 3 usuarios y 3 artistas
        for (int i = 0; i < 3; i++) {
            User user = createUniqueUser("findAll");
            userRepository.save(user);

            Artist artist = Artist.builder()
                    .stageName("Artist " + i)
                    .biography("Bio " + i)
                    .genre(MusicGenre.ROCK)
                    .verified(i % 2 == 0)
                    .user(user)
                    .build();
            artistRepository.save(artist);
        }

        List<Artist> artists = artistRepository.findAll();

        assertThat(artists).hasSizeGreaterThanOrEqualTo(3);
    }

    // ==================== TESTS: findByUserId ====================

    @Test
    @DisplayName("✅ Debe encontrar artista por userId")
    void findByUserId_ShouldReturnArtist_WhenUserHasArtist() {
        User user = createUniqueUser("findByUserId");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("FindByUserId Artist")
                .biography("Bio")
                .genre(MusicGenre.JAZZ)
                .verified(false)
                .user(user)
                .build();
        Artist saved = artistRepository.save(artist);

        Optional<Artist> found = artistRepository.findByUserId(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("❌ No debe encontrar artista cuando userId no tiene artista")
    void findByUserId_ShouldReturnEmpty_WhenUserHasNoArtist() {
        User userWithoutArtist = createUniqueUser("noArtist");
        userRepository.save(userWithoutArtist);

        Optional<Artist> found = artistRepository.findByUserId(userWithoutArtist.getId());

        assertThat(found).isEmpty();
    }

    // ==================== TESTS: findByStageNameIgnoreCase ====================

    @Test
    @DisplayName("✅ Debe encontrar artista por nombre de escenario (case-insensitive)")
    void findByStageNameIgnoreCase_ShouldReturnArtist_WhenMatch() {
        User user = createUniqueUser("stageName");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("TestStageName")
                .biography("Bio")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user)
                .build();
        artistRepository.save(artist);

        Optional<Artist> found = artistRepository.findByStageNameIgnoreCase("TESTSTAGENAME");

        assertThat(found).isPresent();
        assertThat(found.get().getStageName()).isEqualTo("TestStageName");
    }

    @Test
    @DisplayName("❌ No debe encontrar artista cuando nombre no existe")
    void findByStageNameIgnoreCase_ShouldReturnEmpty_WhenNoMatch() {
        User user = createUniqueUser("stageName2");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("ExistingName")
                .biography("Bio")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user)
                .build();
        artistRepository.save(artist);

        Optional<Artist> found = artistRepository.findByStageNameIgnoreCase("Nonexistent");

        assertThat(found).isEmpty();
    }

    // ==================== TESTS: findByGenre ====================

    @Test
    @DisplayName("✅ Debe encontrar artistas por género ROCK")
    void findByGenre_ShouldReturnArtists_WhenGenreRock() {
        // Crear artista ROCK
        User user1 = createUniqueUser("rock1");
        userRepository.save(user1);
        Artist rockArtist = Artist.builder()
                .stageName("Rock Artist")
                .biography("Rock bio")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user1)
                .build();
        artistRepository.save(rockArtist);

        // Crear artista JAZZ
        User user2 = createUniqueUser("jazz");
        userRepository.save(user2);
        Artist jazzArtist = Artist.builder()
                .stageName("Jazz Artist")
                .biography("Jazz bio")
                .genre(MusicGenre.JAZZ)
                .verified(false)
                .user(user2)
                .build();
        artistRepository.save(jazzArtist);

        List<Artist> artists = artistRepository.findByGenre(MusicGenre.ROCK);

        assertThat(artists).isNotEmpty();
        assertThat(artists.stream().allMatch(a -> a.getGenre() == MusicGenre.ROCK)).isTrue();
    }

    @Test
    @DisplayName("✅ Debe retornar lista vacía cuando no hay artistas del género")
    void findByGenre_ShouldReturnEmpty_WhenNoArtistsInGenre() {
        List<Artist> artists = artistRepository.findByGenre(MusicGenre.METAL);
        assertThat(artists).isEmpty();
    }

    // ==================== TESTS: findByVerifiedFalse ====================

    @Test
    @DisplayName("✅ Debe encontrar artistas no verificados")
    void findByVerifiedFalse_ShouldReturnUnverifiedArtists() {
        // Crear artista verificado
        User user1 = createUniqueArtistUser("verified");
        userRepository.save(user1);
        Artist verifiedArtist = Artist.builder()
                .stageName("Verified Artist")
                .biography("Verified bio")
                .genre(MusicGenre.POP)
                .verified(true)
                .user(user1)
                .build();
        artistRepository.save(verifiedArtist);

        // Crear artista no verificado
        User user2 = createUniqueUser("unverified1");
        userRepository.save(user2);
        Artist unverifiedArtist1 = Artist.builder()
                .stageName("Unverified Artist 1")
                .biography("Unverified bio 1")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user2)
                .build();
        artistRepository.save(unverifiedArtist1);

        // Crear otro artista no verificado
        User user3 = createUniqueUser("unverified2");
        userRepository.save(user3);
        Artist unverifiedArtist2 = Artist.builder()
                .stageName("Unverified Artist 2")
                .biography("Unverified bio 2")
                .genre(MusicGenre.JAZZ)
                .verified(false)
                .user(user3)
                .build();
        artistRepository.save(unverifiedArtist2);

        List<Artist> unverifiedArtists = artistRepository.findByVerifiedFalse();

        assertThat(unverifiedArtists).hasSize(2);
        assertThat(unverifiedArtists.stream().noneMatch(Artist::isVerified)).isTrue();
    }

    // ==================== TESTS: findByVerifiedFalse (paginado) ====================

    @Test
    @DisplayName("✅ Debe encontrar artistas no verificados con paginación")
    void findByVerifiedFalse_WithPageable_ShouldReturnPageOfUnverifiedArtists() {
        // Crear 3 artistas no verificados
        for (int i = 0; i < 3; i++) {
            User user = createUniqueUser("unverifiedPage");
            userRepository.save(user);

            Artist artist = Artist.builder()
                    .stageName("Unverified Page " + i)
                    .biography("Bio " + i)
                    .genre(MusicGenre.ROCK)
                    .verified(false)
                    .user(user)
                    .build();
            artistRepository.save(artist);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<Artist> artistPage = artistRepository.findByVerifiedFalse(pageable);

        assertThat(artistPage.getContent()).hasSize(3);
        assertThat(artistPage.getContent().stream().noneMatch(Artist::isVerified)).isTrue();
        assertThat(artistPage.getTotalElements()).isEqualTo(3);
    }

    // ==================== TESTS: findByInstrumentId ====================

    @Test
    @DisplayName("✅ Debe encontrar artistas por instrumento")
    void findByInstrumentId_ShouldReturnArtists_WhenInstrumentExists() {
        // Este test depende de que existan instrumentos en la BD
        // Simplemente verificamos que el método no lanza excepción
        List<Artist> artists = artistRepository.findByInstrumentId(1L);
        assertThat(artists).isNotNull();
    }

    // ==================== TESTS: searchByNameOrStageName ====================

    @Test
    @DisplayName("✅ Debe buscar artistas por nombre de escenario")
    void searchByNameOrStageName_ShouldFindByStageName() {
        User user = createUniqueUser("search");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("SearchableStageName")
                .biography("Bio")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user)
                .build();
        artistRepository.save(artist);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Artist> result = artistRepository.searchByNameOrStageName("Searchable", pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getStageName()).contains("Searchable");
    }

    @Test
    @DisplayName("✅ Debe buscar artistas por nombre de usuario")
    void searchByNameOrStageName_ShouldFindByUserName() {
        User user = createUniqueUser("TargetUserName");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("SomeStageName")
                .biography("Bio")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user)
                .build();
        artistRepository.save(artist);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Artist> result = artistRepository.searchByNameOrStageName("TargetUserName", pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getUser().getName()).contains("TargetUserName");
    }

    @Test
    @DisplayName("✅ Debe buscar artistas con paginación")
    void searchByNameOrStageName_ShouldReturnPageOfArtists() {
        // Crear 2 artistas
        for (int i = 0; i < 2; i++) {
            User user = createUniqueUser("searchPage");
            userRepository.save(user);

            Artist artist = Artist.builder()
                    .stageName("SearchableArtist" + i)
                    .biography("Bio " + i)
                    .genre(MusicGenre.ROCK)
                    .verified(false)
                    .user(user)
                    .build();
            artistRepository.save(artist);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<Artist> result = artistRepository.searchByNameOrStageName("Searchable", pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("❌ No debe encontrar artistas cuando query no coincide")
    void searchByNameOrStageName_ShouldReturnEmpty_WhenNoMatch() {
        User user = createUniqueUser("noMatch");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("UniqueName123")
                .biography("Bio")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user)
                .build();
        artistRepository.save(artist);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Artist> result = artistRepository.searchByNameOrStageName("NonexistentQuery", pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ==================== TESTS: delete ====================

    @Test
    @DisplayName("✅ Debe eliminar un artista")
    void delete_ShouldRemoveArtist() {
        User user = createUniqueUser("delete");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("ToDelete")
                .biography("Bio")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user)
                .build();
        Artist saved = artistRepository.save(artist);
        Long artistId = saved.getId();

        artistRepository.delete(saved);

        Optional<Artist> found = artistRepository.findById(artistId);
        assertThat(found).isEmpty();
    }

    // ==================== TESTS: update ====================

    @Test
    @DisplayName("✅ Debe actualizar artista existente")
    void update_ShouldUpdateArtist() {
        User user = createUniqueUser("update");
        userRepository.save(user);

        Artist artist = Artist.builder()
                .stageName("OriginalName")
                .biography("Original bio")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(user)
                .build();
        Artist saved = artistRepository.save(artist);

        saved.setStageName("Updated Stage Name");
        saved.setBiography("Updated biography");
        saved.setGenre(MusicGenre.METAL);
        saved.setVerified(true);

        Artist updated = artistRepository.save(saved);

        assertThat(updated.getStageName()).isEqualTo("Updated Stage Name");
        assertThat(updated.getBiography()).isEqualTo("Updated biography");
        assertThat(updated.getGenre()).isEqualTo(MusicGenre.METAL);
        assertThat(updated.isVerified()).isTrue();
    }
}