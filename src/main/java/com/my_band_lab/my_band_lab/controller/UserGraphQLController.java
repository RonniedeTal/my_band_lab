package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.CreateArtistRequest;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.*;
import com.my_band_lab.my_band_lab.repository.InstrumentRepository;
import com.my_band_lab.my_band_lab.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.SchemaMapping;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
public class UserGraphQLController {

    private static final Logger log = LoggerFactory.getLogger(UserGraphQLController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private MusicGroupService musicGroupService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private InstrumentService instrumentService;

    // User Queries
    @QueryMapping
    public User userById(@Argument Long id) throws Exception {
        return userService.findUserById(id);
    }

    @QueryMapping
    public User userByName(@Argument String name) throws Exception {
        return userService.findUserByName(name);
    }

    @QueryMapping
    public User userBySurname(@Argument String surname) throws Exception {
        return userService.findUserBySurname(surname);
    }

    @QueryMapping
    public User userByNameAndSurname(@Argument String name, @Argument String surname) throws Exception {
        return userService.findUserByNameAndSurname(name, surname);
    }

    @QueryMapping
    public List<User> users() throws Exception {
        return userService.findAllUsers();
    }

    // Artist Queries
    @QueryMapping
    public List<Artist> artists() throws Exception {
        return artistService.getAllArtists();
    }

    @QueryMapping
    public Artist artistById(@Argument Long id) throws Exception {
        return artistService.getArtistById(id);
    }

    @QueryMapping
    public Artist artistByUserId(@Argument Long userId) throws Exception {
        try {
            return artistService.getArtistByUserId(userId);
        } catch (Exception e) {
            log.error("Artist not found for userId: {}", userId);
            return null;  // 👈 Devolver null, no lanzar excepción
        }
    }

    @QueryMapping
    public List<Artist> artistsByGenre(@Argument MusicGenre genre) throws Exception {
        return artistService.getArtistsByGenre(genre);
    }

    // MusicGroup Queries
    @QueryMapping
    public List<MusicGroup> musicGroups() throws Exception {
        return musicGroupService.getAllGroups();
    }

    @QueryMapping
    public MusicGroup musicGroupById(@Argument Long id) throws Exception {
        return musicGroupService.getGroupById(id);
    }

    @QueryMapping
    public List<MusicGroup> musicGroupsByGenre(@Argument MusicGenre genre) throws Exception {
        return musicGroupService.getGroupsByGenre(genre);
    }

    // Genre Queries
    @QueryMapping
    public List<MusicGenre> availableGenres() {
        return Arrays.asList(MusicGenre.values());
    }

    // User Mutations
    @MutationMapping
    public User createUser(@Argument String name, @Argument String surname,
                           @Argument String email, @Argument String password,
                           @Argument String profileImageUrl) throws Exception {
        User user = User.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .password(password)
                .profileImageUrl(profileImageUrl)
                .build();
        return userService.saveUser(user);
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument String name,
                           @Argument String surname, @Argument String email,
                           @Argument String password, @Argument String profileImageUrl) throws Exception {
        User userDetails = User.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .password(password)
                .profileImageUrl(profileImageUrl)
                .build();
        return userService.updateUser(id, userDetails);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) throws Exception {
        userService.deleteUser(id);
        return true;
    }

    @MutationMapping
    public User updateProfileImage(@Argument String profileImageUrl) throws Exception {
        return userService.updateProfileImage(profileImageUrl);
    }

    // Artist Mutations
    @MutationMapping
    public Artist createArtist(
            @Argument Long userId,
            @Argument String stageName,
            @Argument String biography,
            @Argument MusicGenre genre,
            @Argument List<Long> instrumentIds,
            @Argument Long mainInstrumentId) throws Exception {

        CreateArtistRequest request = new CreateArtistRequest();
        request.setUserId(userId);
        request.setStageName(stageName);
        request.setBiography(biography);
        request.setGenre(genre);
        request.setInstrumentIds(instrumentIds);
        request.setMainInstrumentId(mainInstrumentId);

        return artistService.createArtist(request);
    }

    @MutationMapping
    public Artist updateArtist(@Argument Long id, @Argument String stageName,
                               @Argument String biography, @Argument MusicGenre genre) throws Exception {
        return artistService.updateArtist(id, stageName, biography, genre);
    }

    @MutationMapping
    public Boolean deleteArtist(@Argument Long id) throws Exception {
        artistService.deleteArtist(id);
        return true;
    }

    @MutationMapping
    public Artist createArtistForCurrentUser(
            @Argument String stageName,
            @Argument String biography,
            @Argument MusicGenre genre,
            @Argument List<Long> instrumentIds,
            @Argument Long mainInstrumentId,
            @Argument String country,
            @Argument String city) throws Exception {

        return artistService.createArtistForCurrentUser(
                stageName, biography, genre, instrumentIds, mainInstrumentId, country, city);
    }

    // MusicGroup Mutations
    @MutationMapping
    public MusicGroup createMusicGroup(@Argument String name, @Argument String description,
                                       @Argument MusicGenre genre, @Argument Long founderId, @Argument String country,@Argument String city) throws Exception {
        return musicGroupService.createGroup(name, description, genre, founderId, country, city);
    }

    @MutationMapping
    public MusicGroup addMemberToGroup(@Argument Long groupId, @Argument Long userId) throws Exception {
        return musicGroupService.addMember(groupId, userId);
    }

    @MutationMapping
    public MusicGroup removeMemberFromGroup(@Argument Long groupId, @Argument Long userId) throws Exception {
        return musicGroupService.removeMember(groupId, userId);
    }

    @MutationMapping
    public MusicGroup updateMusicGroupGenre(@Argument Long groupId, @Argument MusicGenre genre) throws Exception {
        return musicGroupService.updateGroupGenre(groupId, genre);
    }

    @MutationMapping
    public Boolean deleteMusicGroup(@Argument Long id) throws Exception {
        musicGroupService.deleteGroup(id);
        return true;
    }
    // ==================== QUERIES PARA INSTRUMENTOS ====================

//    @Autowired
//    private InstrumentService instrumentService;

    @QueryMapping
    public List<Instrument> instruments() throws Exception {
        return instrumentService.getAllInstruments();
    }

    @QueryMapping
    public List<Instrument> instrumentsByCategory(@Argument String category) throws Exception {
        return instrumentService.getInstrumentsByCategory(category);
    }

    @QueryMapping
    public List<Artist> artistsByInstrument(@Argument Long instrumentId) throws Exception {
        List<Artist> artists = artistService.getArtistsByInstrument(instrumentId);
        return artists != null ? artists : new ArrayList<>();
    }

    @QueryMapping
    public List<Instrument> artistInstruments(@Argument Long artistId) throws Exception {
        return artistService.getArtistInstruments(artistId);
    }

    // ==================== QUERIES CON PAGINACIÓN ====================

    @QueryMapping
    public PageResponse<Artist> artistsPaginated(
            @Argument int page,
            @Argument int size) throws Exception {
        return artistService.getAllArtistsPaginated(page, size);
    }

    @QueryMapping
    public PageResponse<MusicGroup> musicGroupsPaginated(
            @Argument int page,
            @Argument int size) throws Exception {
        return musicGroupService.getAllGroupsPaginated(page, size);
    }
    // ==================== QUERIES DE BÚSQUEDA ====================

    @QueryMapping
    public PageResponse<Artist> searchArtists(
            @Argument String query,
            @Argument int page,
            @Argument int size,
            @Argument String country,
            @Argument String city,
            @Argument MusicGenre genre) throws Exception {
        return artistService.searchArtists(query, page, size, country, city, genre);
    }

    @QueryMapping
    public PageResponse<MusicGroup> searchGroups(
            @Argument String query,
            @Argument int page,
            @Argument int size,
            @Argument String country,
            @Argument String city,
            @Argument MusicGenre genre) throws Exception {
        return musicGroupService.searchGroups(query, page, size, country, city, genre);
    }

    @QueryMapping
    public List<Artist> artistsLookingForBand(
            @Argument String genre,
            @Argument List<Long> instrumentIds,
            @Argument String country,
            @Argument String city
    ) {
        log.info("=== GraphQL Query: artistsLookingForBand ===");
        log.info("Filters - genre: {}, instrumentIds: {}, country: {}, city: {}",
                genre, instrumentIds, country, city);

        try {
            // 👇 Hacer la variable final o efectivamente final
            final Long currentUserId = getCurrentUserId(); // Extraer a un método

            List<Artist> artists = artistService.findArtistsWithFilters(genre, instrumentIds, country, city);

            List<Artist> filteredArtists = artists.stream()
                    .filter(artist -> currentUserId == null ||
                            artist.getUser() == null ||
                            !artist.getUser().getId().equals(currentUserId))
                    .collect(Collectors.toList());

            log.info("Artistas encontrados: {}", filteredArtists.size());
            return filteredArtists;

        } catch (Exception e) {
            log.error("Error en artistsLookingForBand: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // Método auxiliar para obtener el userId actual
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            try {
                String email = ((UserDetails) auth.getPrincipal()).getUsername();
                User user = userService.findUserByEmail(email);
                if (user != null) {
                    return user.getId();
                }
            } catch (Exception e) {
                log.error("Error getting current user: {}", e.getMessage());
            }
        }
        return null;
    }

    // ========== LOOKING FOR INSTRUMENTS - GRAPHQL ==========

    /**
     * Query resolver para obtener los instrumentos que el artista busca tocar
     */
    @SchemaMapping(typeName = "Artist", field = "lookingForInstruments")
    public List<Instrument> lookingForInstruments(Artist artist) {
        log.info("=== GraphQL SchemaMapping: lookingForInstruments for artistId: {} ===", artist.getId());

        try {
            if (artist == null) {
                log.warn("Artist is null");
                return new ArrayList<>();
            }

            List<Long> instrumentIds = artist.getLookingForInstrumentIds();
            log.info("Instrument IDs from artist: {}", instrumentIds);

            if (instrumentIds == null || instrumentIds.isEmpty()) {
                log.info("No instrument IDs found for artist {}", artist.getId());
                return new ArrayList<>();
            }

            List<Instrument> instruments = instrumentRepository.findAllById(instrumentIds);
            log.info("Found {} instruments", instruments != null ? instruments.size() : 0);

            return instruments != null ? instruments : new ArrayList<>();

        } catch (Exception e) {
            log.error("Error in lookingForInstruments: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    /**
     * Mutation resolver para actualizar los instrumentos que el artista busca tocar
     */
    @MutationMapping
    public Artist updateLookingForInstruments(
            @Argument Long artistId,
            @Argument List<Long> instrumentIds) throws Exception {

        log.info("=== GraphQL Mutation: updateLookingForInstruments ===");
        log.info("ArtistId: {}, InstrumentIds: {}", artistId, instrumentIds);

        // Validar que los instrumentos existen
        if (instrumentIds != null && !instrumentIds.isEmpty()) {
            for (Long instrumentId : instrumentIds) {
                boolean exists = instrumentRepository.existsById(instrumentId);
                if (!exists) {
                    throw new Exception("Instrument not found with id: " + instrumentId);
                }
            }
        }

        return artistService.updateLookingForInstruments(artistId, instrumentIds);
    }

    @SchemaMapping(typeName = "Artist", field = "lookingForGenres")
    public List<String> lookingForGenres(Artist artist) {
        log.info("=== GraphQL SchemaMapping: lookingForGenres for artistId: {} ===", artist.getId());

        if (artist == null || artist.getLookingForGenres() == null) {
            return new ArrayList<>();
        }
        return artist.getLookingForGenres();
    }

    @MutationMapping
    public Artist updateLookingForGenres(
            @Argument Long artistId,
            @Argument List<String> genres) throws Exception {

        log.info("=== GraphQL Mutation: updateLookingForGenres ===");
        log.info("ArtistId: {}, Genres: {}", artistId, genres);

        // Validar que los géneros existen
        if (genres != null) {
            for (String genre : genres) {
                try {
                    MusicGenre.valueOf(genre);
                } catch (IllegalArgumentException e) {
                    throw new Exception("Invalid genre: " + genre);
                }
            }
        }

        return artistService.updateLookingForGenres(artistId, genres);
    }


}