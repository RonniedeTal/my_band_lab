package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.CreateArtistRequest;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.*;
import com.my_band_lab.my_band_lab.repository.ArtistRepository;
import com.my_band_lab.my_band_lab.repository.InstrumentRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistService Tests")
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @InjectMocks
    private ArtistServiceImpl artistService;

    private User testUser;
    private User artistUser;
    private Artist testArtist;
    private Artist verifiedArtist;
    private Instrument guitar;
    private Instrument piano;
    private CreateArtistRequest createRequest;

    @BeforeEach
    void setUp() {
        // Usuario normal
        testUser = User.builder()
                .id(1L)
                .name("Test")
                .surname("User")
                .email("test@example.com")
                .password("encoded")
                .role(Role.USER)
                .build();

        // Usuario artista
        artistUser = User.builder()
                .id(2L)
                .name("Artist")
                .surname("User")
                .email("artist@example.com")
                .password("encoded")
                .role(Role.ARTIST)
                .build();

        // Artista de prueba
        testArtist = Artist.builder()
                .id(1L)
                .stageName("Test Artist")
                .biography("Test biography")
                .genre(MusicGenre.ROCK)
                .verified(false)
                .user(testUser)
                .instruments(new ArrayList<>())
                .build();

        // Artista verificado
        verifiedArtist = Artist.builder()
                .id(2L)
                .stageName("Verified Artist")
                .biography("Verified bio")
                .genre(MusicGenre.POP)
                .verified(true)
                .user(artistUser)
                .instruments(new ArrayList<>())
                .build();

        // Instrumentos
        guitar = Instrument.builder()
                .id(1L)
                .name("Guitarra")
                .category("cuerda")
                .build();

        piano = Instrument.builder()
                .id(2L)
                .name("Piano")
                .category("teclado")
                .build();

        // CreateArtistRequest
        createRequest = new CreateArtistRequest();
        createRequest.setUserId(1L);
        createRequest.setStageName("New Artist");
        createRequest.setBiography("New bio");
        createRequest.setGenre(MusicGenre.ROCK);
        createRequest.setInstrumentIds(List.of(1L, 2L));
        createRequest.setMainInstrumentId(1L);
    }

    // ==================== TESTS: createArtist ====================

    @Nested
    @DisplayName("createArtist Tests")
    class CreateArtistTests {

        @Test
        @DisplayName("✅ Debe crear artista exitosamente")
        void createArtist_ShouldCreateArtist_WhenValid() throws Exception {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(artistRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(artistRepository.save(any(Artist.class))).thenReturn(testArtist);
            when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));
            when(instrumentRepository.findById(2L)).thenReturn(Optional.of(piano));

            Artist result = artistService.createArtist(createRequest);

            assertThat(result).isNotNull();
            verify(artistRepository, times(2)).save(any(Artist.class));
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario no existe")
        void createArtist_ShouldThrowException_WhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            createRequest.setUserId(999L);

            assertThatThrownBy(() -> artistService.createArtist(createRequest))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario ya es artista")
        void createArtist_ShouldThrowException_WhenUserIsAlreadyArtist() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(artistRepository.findByUserId(1L)).thenReturn(Optional.of(testArtist));

            assertThatThrownBy(() -> artistService.createArtist(createRequest))
                    .isInstanceOf(Exception.class)
                    .hasMessage("User is already an artist");
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando instrumento no existe")
        void createArtist_ShouldThrowException_WhenInstrumentNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(artistRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(artistRepository.save(any(Artist.class))).thenReturn(testArtist);
            when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));
            when(instrumentRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artistService.createArtist(createRequest))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Instrument not found");
        }
    }

    // ==================== TESTS: getArtistById ====================

    @Nested
    @DisplayName("getArtistById Tests")
    class GetArtistByIdTests {

        @Test
        @DisplayName("✅ Debe retornar artista por ID")
        void getArtistById_ShouldReturnArtist_WhenExists() throws Exception {
            when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));

            Artist result = artistService.getArtistById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStageName()).isEqualTo("Test Artist");
            verify(artistRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando artista no existe")
        void getArtistById_ShouldThrowException_WhenNotFound() {
            when(artistRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artistService.getArtistById(999L))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Artist not found");
        }
    }

    // ==================== TESTS: getArtistByUserId ====================

    @Nested
    @DisplayName("getArtistByUserId Tests")
    class GetArtistByUserIdTests {

        @Test
        @DisplayName("✅ Debe retornar artista por userId")
        void getArtistByUserId_ShouldReturnArtist_WhenExists() throws Exception {
            when(artistRepository.findByUserId(1L)).thenReturn(Optional.of(testArtist));

            Artist result = artistService.getArtistByUserId(1L);

            assertThat(result).isNotNull();
            assertThat(result.getUser().getId()).isEqualTo(1L);
            verify(artistRepository, times(1)).findByUserId(1L);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando usuario no tiene artista")
        void getArtistByUserId_ShouldThrowException_WhenNotFound() {
            when(artistRepository.findByUserId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artistService.getArtistByUserId(999L))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Artist not found for user");
        }
    }

    // ==================== TESTS: getArtistsByGenre ====================

    @Nested
    @DisplayName("getArtistsByGenre Tests")
    class GetArtistsByGenreTests {

        @Test
        @DisplayName("✅ Debe retornar artistas por género")
        void getArtistsByGenre_ShouldReturnArtists() throws Exception {
            List<Artist> artists = List.of(testArtist);
            when(artistRepository.findByGenre(MusicGenre.ROCK)).thenReturn(artists);

            List<Artist> result = artistService.getArtistsByGenre(MusicGenre.ROCK);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getGenre()).isEqualTo(MusicGenre.ROCK);
        }

        @Test
        @DisplayName("✅ Debe retornar lista vacía cuando no hay artistas")
        void getArtistsByGenre_ShouldReturnEmptyList_WhenNoArtists() throws Exception {
            when(artistRepository.findByGenre(MusicGenre.METAL)).thenReturn(List.of());

            List<Artist> result = artistService.getArtistsByGenre(MusicGenre.METAL);

            assertThat(result).isEmpty();
        }
    }

    // ==================== TESTS: getAllArtists ====================

    @Nested
    @DisplayName("getAllArtists Tests")
    class GetAllArtistsTests {

        @Test
        @DisplayName("✅ Debe retornar todos los artistas")
        void getAllArtists_ShouldReturnAllArtists() throws Exception {
            List<Artist> artists = List.of(testArtist, verifiedArtist);
            when(artistRepository.findAll()).thenReturn(artists);

            List<Artist> result = artistService.getAllArtists();

            assertThat(result).hasSize(2);
            verify(artistRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando no hay artistas")
        void getAllArtists_ShouldThrowException_WhenNoArtists() throws Exception {
            when(artistRepository.findAll()).thenReturn(List.of());

            assertThatThrownBy(() -> artistService.getAllArtists())
                    .isInstanceOf(Exception.class)
                    .hasMessage("No artists found");
        }
    }

    // ==================== TESTS: updateArtist ====================

    @Nested
    @DisplayName("updateArtist Tests")
    class UpdateArtistTests {

        @Test
        @DisplayName("✅ Debe actualizar artista exitosamente")
        void updateArtist_ShouldUpdateArtist() throws Exception {
            when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
            when(artistRepository.save(any(Artist.class))).thenReturn(testArtist);

            Artist result = artistService.updateArtist(1L, "New Stage Name", "New bio", MusicGenre.POP);

            assertThat(result.getStageName()).isEqualTo("New Stage Name");
            assertThat(result.getBiography()).isEqualTo("New bio");
            assertThat(result.getGenre()).isEqualTo(MusicGenre.POP);
        }

        @Test
        @DisplayName("✅ Debe mantener valores cuando no se envían nuevos")
        void updateArtist_ShouldKeepValues_WhenNull() throws Exception {
            when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
            when(artistRepository.save(any(Artist.class))).thenReturn(testArtist);

            Artist result = artistService.updateArtist(1L, null, null, null);

            assertThat(result.getStageName()).isEqualTo("Test Artist");
            assertThat(result.getBiography()).isEqualTo("Test biography");
            assertThat(result.getGenre()).isEqualTo(MusicGenre.ROCK);
        }
    }

    // ==================== TESTS: deleteArtist ====================

    @Nested
    @DisplayName("deleteArtist Tests")
    class DeleteArtistTests {

        @Test
        @DisplayName("✅ Debe eliminar artista exitosamente")
        void deleteArtist_ShouldDeleteArtist() throws Exception {
            when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
            doNothing().when(artistRepository).delete(testArtist);

            artistService.deleteArtist(1L);

            verify(artistRepository, times(1)).delete(testArtist);
        }
    }

    // ==================== TESTS: getArtistsByInstrument ====================

    @Nested
    @DisplayName("getArtistsByInstrument Tests")
    class GetArtistsByInstrumentTests {

        @Test
        @DisplayName("✅ Debe retornar artistas por instrumento")
        void getArtistsByInstrument_ShouldReturnArtists() throws Exception {
            List<Artist> artists = List.of(testArtist);
            when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));
            when(artistRepository.findByInstrumentId(1L)).thenReturn(artists);

            List<Artist> result = artistService.getArtistsByInstrument(1L);

            assertThat(result).hasSize(1);
            verify(instrumentRepository, times(1)).findById(1L);
            verify(artistRepository, times(1)).findByInstrumentId(1L);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando instrumento no existe")
        void getArtistsByInstrument_ShouldThrowException_WhenInstrumentNotFound() {
            when(instrumentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> artistService.getArtistsByInstrument(999L))
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Instrument not found");
        }
    }

    // ==================== TESTS: getAllArtistsPaginated ====================

    @Nested
    @DisplayName("getAllArtistsPaginated Tests")
    class GetAllArtistsPaginatedTests {

        @Test
        @DisplayName("✅ Debe retornar página de artistas")
        void getAllArtistsPaginated_ShouldReturnPage() throws Exception {
            List<Artist> artists = List.of(testArtist, verifiedArtist);
            Page<Artist> artistPage = new PageImpl<>(artists, PageRequest.of(0, 10), artists.size());
            when(artistRepository.findAll(any(Pageable.class))).thenReturn(artistPage);

            PageResponse<Artist> result = artistService.getAllArtistsPaginated(0, 10);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getCurrentPage()).isEqualTo(0);
        }
    }

    // ==================== TESTS: getUnverifiedArtists ====================

    @Nested
    @DisplayName("getUnverifiedArtists Tests")
    class GetUnverifiedArtistsTests {

        @Test
        @DisplayName("✅ Debe retornar artistas no verificados")
        void getUnverifiedArtists_ShouldReturnUnverifiedArtists() throws Exception {
            List<Artist> unverifiedArtists = List.of(testArtist);
            when(artistRepository.findByVerifiedFalse()).thenReturn(unverifiedArtists);

            List<Artist> result = artistService.getUnverifiedArtists();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isVerified()).isFalse();
        }

        @Test
        @DisplayName("✅ Debe retornar lista vacía cuando no hay artistas no verificados")
        void getUnverifiedArtists_ShouldReturnEmptyList_WhenNone() throws Exception {
            when(artistRepository.findByVerifiedFalse()).thenReturn(new ArrayList<>());

            List<Artist> result = artistService.getUnverifiedArtists();

            assertThat(result).isEmpty();
        }
    }

    // ==================== TESTS: verifyArtist ====================

    @Nested
    @DisplayName("verifyArtist Tests")
    class VerifyArtistTests {

        @Test
        @DisplayName("✅ Debe verificar artista exitosamente")
        void verifyArtist_ShouldVerifyArtist() throws Exception {
            when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
            when(artistRepository.save(any(Artist.class))).thenAnswer(invocation -> {
                Artist artist = invocation.getArgument(0);
                artist.setVerified(true);
                return artist;
            });

            Artist result = artistService.verifyArtist(1L);

            assertThat(result.isVerified()).isTrue();
            verify(artistRepository, times(1)).save(testArtist);
        }

        @Test
        @DisplayName("❌ Debe lanzar excepción cuando artista ya está verificado")
        void verifyArtist_ShouldThrowException_WhenAlreadyVerified() throws Exception {
            when(artistRepository.findById(2L)).thenReturn(Optional.of(verifiedArtist));

            assertThatThrownBy(() -> artistService.verifyArtist(2L))
                    .isInstanceOf(Exception.class)
                    .hasMessage("Artist is already verified");
        }
    }

    // ==================== TESTS: getUnverifiedArtistsPaginated ====================

    @Nested
    @DisplayName("getUnverifiedArtistsPaginated Tests")
    class GetUnverifiedArtistsPaginatedTests {

        @Test
        @DisplayName("✅ Debe retornar página de artistas no verificados")
        void getUnverifiedArtistsPaginated_ShouldReturnPage() throws Exception {
            List<Artist> unverifiedArtists = List.of(testArtist);
            Page<Artist> artistPage = new PageImpl<>(unverifiedArtists, PageRequest.of(0, 10), unverifiedArtists.size());
            when(artistRepository.findByVerifiedFalse(any(Pageable.class))).thenReturn(artistPage);

            PageResponse<Artist> result = artistService.getUnverifiedArtistsPaginated(0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).isVerified()).isFalse();
            verify(artistRepository, times(1)).findByVerifiedFalse(any(Pageable.class));
        }
    }
}