package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.CreateArtistRequest;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.*;
import com.my_band_lab.my_band_lab.repository.ArtistRepository;
import com.my_band_lab.my_band_lab.repository.InstrumentRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ArtistServiceImpl implements ArtistService {
    private static final Logger log = LoggerFactory.getLogger(ArtistServiceImpl.class);

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Override
    @Transactional
    public Artist createArtist(CreateArtistRequest request) throws Exception {
        // 1. Verificar que el usuario existe
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new Exception("User not found with id: " + request.getUserId()));

        // 2. Verificar que el usuario no es ya artista
        if (artistRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new Exception("User is already an artist");
        }

        // 3. Crear el artista
        Artist artist = Artist.builder()
                .user(user)
                .stageName(request.getStageName())
                .biography(request.getBiography())
                .genre(request.getGenre())
                .verified(false)
                .build();

        Artist savedArtist = artistRepository.save(artist);

        // 4. Añadir instrumentos si se enviaron
        if (request.getInstrumentIds() != null && !request.getInstrumentIds().isEmpty()) {
            List<Instrument> instruments = new ArrayList<>();
            for (Long instrumentId : request.getInstrumentIds()) {
                Instrument instrument = instrumentRepository.findById(instrumentId)
                        .orElseThrow(() -> new Exception("Instrument not found with id: " + instrumentId));
                instruments.add(instrument);
            }
            savedArtist.setInstruments(instruments);

            // 5. Establecer instrumento principal
            if (request.getMainInstrumentId() != null) {
                boolean exists = request.getInstrumentIds().contains(request.getMainInstrumentId());
                if (!exists) {
                    throw new Exception("Main instrument must be one of the selected instruments");
                }
                savedArtist.setMainInstrumentId(request.getMainInstrumentId());
            }

            savedArtist = artistRepository.save(savedArtist);
        }

        return savedArtist;
    }

    @Override
    public Artist getArtistByUserId(Long userId) throws Exception {
        return artistRepository.findByUserId(userId)
                .orElseThrow(() -> new Exception("Artist not found for user: " + userId));
    }

    @Override
    public Artist getArtistById(Long id) throws Exception {
        System.out.println("=== GET ARTIST BY ID ===");
        System.out.println("Artist ID: " + id);

        Optional<Artist> artist = artistRepository.findById(id);
        System.out.println("Artist found: " + artist.isPresent());

        if (artist.isEmpty()) {
            throw new Exception("Artist not found with id: " + id);
        }

        Artist result = artist.get();
        System.out.println("Artist stageName: " + result.getStageName());
        System.out.println("Artist verified: " + result.isVerified());
        System.out.println("Artist instruments size: " + (result.getInstruments() != null ? result.getInstruments().size() : 0));

        return result;
    }

    @Override
    public List<Artist> getArtistsByGenre(MusicGenre genre) throws Exception {
        return artistRepository.findByGenre(genre);
    }

    @Override
    public List<Artist> getAllArtists() throws Exception {
        List<Artist> artists = artistRepository.findAll();
//        if (artists.isEmpty()) {
//            throw new Exception("No artists found");
//        }
//        return artists;
        return artists != null ? artists : new ArrayList<>();
    }

    @Override
    @Transactional
    public Artist updateArtist(Long artistId, String stageName, String biography, MusicGenre genre) throws Exception {
        Artist artist = getArtistById(artistId);

        if (stageName != null && !stageName.isEmpty()) {
            artist.setStageName(stageName);
        }

        if (biography != null) {
            artist.setBiography(biography);
        }

        if (genre != null) {
            artist.setGenre(genre);
        }

        return artistRepository.save(artist);
    }

    @Override
    public void deleteArtist(Long artistId) throws Exception {
        Artist artist = getArtistById(artistId);
        artistRepository.delete(artist);
    }

    @Override
    @Transactional
    public Artist updateArtistInstruments(Long artistId, List<Long> instrumentIds, Long mainInstrumentId) throws Exception {
        Artist artist = getArtistById(artistId);

        List<Instrument> instruments = new ArrayList<>();
        for (Long instId : instrumentIds) {
            Instrument instrument = instrumentRepository.findById(instId)
                    .orElseThrow(() -> new Exception("Instrument not found with id: " + instId));
            instruments.add(instrument);
        }

        artist.setInstruments(instruments);

        if (mainInstrumentId != null) {
            boolean exists = instrumentIds.contains(mainInstrumentId);
            if (!exists) {
                throw new Exception("Main instrument must be one of the selected instruments");
            }
            artist.setMainInstrumentId(mainInstrumentId);
        }

        return artistRepository.save(artist);
    }

    @Override
    public List<Instrument> getArtistInstruments(Long artistId) throws Exception {
        Artist artist = getArtistById(artistId);
        return artist.getInstruments();
    }

//    @Override
//    public List<Artist> getArtistsByInstrument(Long instrumentId) throws Exception {
//        // Verificar que el instrumento existe
//        Instrument instrument = instrumentRepository.findById(instrumentId)
//                .orElseThrow(() -> new Exception("Instrument not found with id: " + instrumentId));
//
//        // Buscar artistas que tocan ese instrumento
//        List<Artist> artists = artistRepository.findByInstrumentId(instrumentId);
//
//        // Devolver lista vacía si no hay resultados (nunca null)
//        return artists != null ? artists : new ArrayList<>();
//    }

    @Override
    public List<Artist> getArtistsByInstrument(Long instrumentId) throws Exception {
        // Verificar que el instrumento existe
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new Exception("Instrument not found with id: " + instrumentId));

        // Buscar artistas que tocan ese instrumento
        List<Artist> artists = artistRepository.findByInstrumentId(instrumentId);

        // ✅ Devolver lista vacía si no hay resultados
        return artists != null ? artists : new ArrayList<>();
    }

    @Override
    public PageResponse<Artist> getAllArtistsPaginated(int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<Artist> artistPage = artistRepository.findAll(pageable);

        return PageResponse.<Artist>builder()
                .content(artistPage.getContent())
                .totalElements(artistPage.getTotalElements())
                .totalPages(artistPage.getTotalPages())
                .currentPage(artistPage.getNumber())
                .size(artistPage.getSize())
                .hasNext(artistPage.hasNext())
                .hasPrevious(artistPage.hasPrevious())
                .build();
    }
    @Override
    public PageResponse<Artist> searchArtists(String query, int page, int size,
                                              String country, String city, MusicGenre genre) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<Artist> artistPage;

        // Log para depuración
        System.out.println("=== BÚSQUEDA DE ARTISTAS ===");
        System.out.println("Query original: '" + query + "'");
        System.out.println("Country recibido: '" + country + "'");
        System.out.println("City recibida: '" + city + "'");
        System.out.println("Genre recibido: " + genre);

        // Normalizar query: si es "*" o vacío, usar null
        String normalizedQuery = query;
        if (query == null || query.trim().isEmpty() || "*".equals(query.trim())) {
            normalizedQuery = "";
            System.out.println("Query normalizada a vacío para búsqueda amplia");
        }

        // Normalizar country y city
        String normalizedCountry = (country != null && !country.trim().isEmpty()) ? country : null;
        String normalizedCity = (city != null && !city.trim().isEmpty()) ? city : null;

        System.out.println("Query final: '" + normalizedQuery + "'");
        System.out.println("Country final: " + normalizedCountry);
        System.out.println("City final: " + normalizedCity);

        // Siempre usar searchWithFilters, pero con valores null apropiados
        artistPage = artistRepository.searchWithFilters(
                normalizedQuery,
                normalizedCountry,
                normalizedCity,
                genre,
                pageable
        );

        System.out.println("✅ Resultados encontrados: " + artistPage.getTotalElements());
        artistPage.getContent().forEach(artist -> {
            System.out.println("  - " + artist.getStageName() +
                    " (País: " + artist.getCountry() +
                    ", Ciudad: " + artist.getCity() + ")");
        });

        return PageResponse.<Artist>builder()
                .content(artistPage.getContent())
                .totalElements(artistPage.getTotalElements())
                .totalPages(artistPage.getTotalPages())
                .currentPage(artistPage.getNumber())
                .size(artistPage.getSize())
                .hasNext(artistPage.hasNext())
                .hasPrevious(artistPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public Artist createArtistForCurrentUser(String stageName, String biography, MusicGenre genre,
                                             List<Long> instrumentIds, Long mainInstrumentId, String country, String city) throws Exception {

        // Obtener usuario autenticado
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new Exception("User not authenticated");
        }

        String email = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("User not found"));

        // Verificar si el usuario ya es artista
        if (artistRepository.findByUserId(currentUser.getId()).isPresent()) {
            throw new Exception("User is already an artist");
        }

        // Verificar rol
        if (!currentUser.getRole().name().equals("USER")) {
            throw new Exception("Only users with USER role can become artists");
        }

        // Crear artista
        Artist artist = Artist.builder()
                .user(currentUser)
                .stageName(stageName)
                .biography(biography)
                .genre(genre)
                .country(country)
                .city(city)
                .verified(false)
                .build();

        Artist savedArtist = artistRepository.save(artist);

        // Añadir instrumentos
        if (instrumentIds != null && !instrumentIds.isEmpty()) {
            List<Instrument> instruments = new ArrayList<>();
            for (Long instrumentId : instrumentIds) {
                Instrument instrument = instrumentRepository.findById(instrumentId)
                        .orElseThrow(() -> new Exception("Instrument not found with id: " + instrumentId));
                instruments.add(instrument);
            }

            // IMPORTANTE: Obtener el artista de la base de datos para asegurar que es una entidad gestionada
            Artist managedArtist = artistRepository.findById(savedArtist.getId())
                    .orElseThrow(() -> new Exception("Artist not found"));

            // Establecer los instrumentos
            managedArtist.getInstruments().clear();
            managedArtist.getInstruments().addAll(instruments);

            if (mainInstrumentId != null) {
                boolean exists = instrumentIds.contains(mainInstrumentId);
                if (!exists) {
                    throw new Exception("Main instrument must be one of the selected instruments");
                }
                managedArtist.setMainInstrumentId(mainInstrumentId);
            }

            // Guardar para persistir la relación
            savedArtist = artistRepository.save(managedArtist);
        }

        // Cambiar rol del usuario
        currentUser.setRole(Role.ARTIST);
        userRepository.save(currentUser);

        return savedArtist;
    }
    @Override
    public List<Artist> getUnverifiedArtists() throws Exception {
        List<Artist> artists = artistRepository.findByVerifiedFalse();
//        if (artists.isEmpty()) {
//            throw new Exception("No unverified artists found");
//        }
//        return artists;

        // Devolver lista vacía en lugar de lanzar excepción
        return artists == null ? new ArrayList<>() : artists;
    }

    @Override
    public PageResponse<Artist> getUnverifiedArtistsPaginated(int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<Artist> artistPage = artistRepository.findByVerifiedFalse(pageable);

        return PageResponse.<Artist>builder()
                .content(artistPage.getContent())
                .totalElements(artistPage.getTotalElements())
                .totalPages(artistPage.getTotalPages())
                .currentPage(artistPage.getNumber())
                .size(artistPage.getSize())
                .hasNext(artistPage.hasNext())
                .hasPrevious(artistPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public Artist verifyArtist(Long artistId) throws Exception {
        Artist artist = getArtistById(artistId);

        if (artist.isVerified()) {
            throw new Exception("Artist is already verified");
        }

        artist.setVerified(true);
        return artistRepository.save(artist);
    }
    @Override
    public Artist save(Artist artist) {
        return artistRepository.save(artist);
    }

    @Override
    @Transactional
    public Artist updateLookingForBandStatus(Long artistId, boolean isLookingForBand) throws Exception {
        log.info("Actualizando estado isLookingForBand para artista {}: {}", artistId, isLookingForBand);

        Artist artist = getArtistById(artistId);

        // Verificar que el artista pertenece al usuario autenticado
        User currentUser = getCurrentUser();
        if (!artist.getUser().getId().equals(currentUser.getId())) {
            throw new Exception("No tienes permiso para modificar este artista");
        }

        artist.setLookingForBand(isLookingForBand);
        artist.setUpdatedAt(LocalDateTime.now());

        Artist saved = artistRepository.save(artist);
        log.info("Estado actualizado correctamente. Nuevo estado: {}", saved.isLookingForBand());

        return saved;
    }


    @Override
    public boolean getLookingForBandStatus(Long artistId) throws Exception {
        log.info("Obteniendo estado isLookingForBand para artista {}", artistId);

        Artist artist = getArtistById(artistId);
        boolean status = artist.isLookingForBand();

        log.info("Estado actual: {}", status);
        return status;
    }

    private User getCurrentUser() throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new Exception("Usuario no autenticado");
        }

        String email = ((UserDetails) principal).getUsername();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
    }

    @Override
    public List<Artist> getArtistsLookingForBand() {
        log.info("Buscando artistas que buscan banda...");
        try {
            List<Artist> artists = artistRepository.findByIsLookingForBandTrue();
            log.info("Encontrados: {} artistas", artists != null ? artists.size() : 0);
            return artists != null ? artists : new ArrayList<>();
        } catch (Exception e) {
            log.error("Error al buscar artistas: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    @Override
    @Transactional
    public Artist updateLookingForInstruments(Long artistId, List<Long> instrumentIds) throws Exception {
        log.info("Actualizando instrumentos buscados para artista {}: {}", artistId, instrumentIds);

        Artist artist = getArtistById(artistId);

        // Verificar permisos (el artista pertenece al usuario autenticado)
        User currentUser = getCurrentUser();
        if (!artist.getUser().getId().equals(currentUser.getId())) {
            throw new Exception("No tienes permiso para modificar este artista");
        }

        artist.setLookingForInstrumentIds(instrumentIds);
        artist.setUpdatedAt(LocalDateTime.now());

        return artistRepository.save(artist);
    }

    @Override
    @Transactional
    public Artist updateLookingForGenres(Long artistId, List<String> genres) throws Exception {
        log.info("Actualizando géneros buscados para artista {}: {}", artistId, genres);

        Artist artist = getArtistById(artistId);

        User currentUser = getCurrentUser();
        if (!artist.getUser().getId().equals(currentUser.getId())) {
            throw new Exception("No tienes permiso para modificar este artista");
        }

        artist.setLookingForGenres(genres);
        artist.setUpdatedAt(LocalDateTime.now());

        return artistRepository.save(artist);
    }

    @Override
    public List<String> getLookingForGenres(Long artistId) throws Exception {
        Artist artist = getArtistById(artistId);
        return artist.getLookingForGenres() != null ? artist.getLookingForGenres() : new ArrayList<>();
    }

    @Override
    public List<Artist> findArtistsWithFilters(String genre, List<Long> instrumentIds, String country, String city) throws Exception {
        log.info("Buscando artistas con filtros - genre: {}, instrumentIds: {}, country: {}, city: {}",
                genre, instrumentIds, country, city);

        return artistRepository.findWithFilters(genre, instrumentIds, country, city);
    }

}

