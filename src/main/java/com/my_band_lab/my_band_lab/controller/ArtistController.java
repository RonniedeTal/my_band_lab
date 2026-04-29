package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.*;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.repository.InstrumentRepository;
import com.my_band_lab.my_band_lab.entity.Instrument;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.ArtistService;
import com.my_band_lab.my_band_lab.service.ImageUploadService;
import com.my_band_lab.my_band_lab.service.MusicGroupService;
import com.my_band_lab.my_band_lab.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import com.my_band_lab.my_band_lab.entity.MusicGenre;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    @Autowired
    private UserService userService;
    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private MusicGroupService musicGroupService;

    @Autowired
    private InstrumentRepository instrumentRepository;

    private static final Logger log = LoggerFactory.getLogger(ArtistController.class);

    @PostMapping("/create")
    public Artist createArtist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateArtistForCurrentUserRequest request) throws Exception {

        log.info("=== CREATE ARTIST REQUEST ===");
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "null");
        log.info("Request: stageName={}, genre={}, instrumentIds={}",
                request.getStageName(), request.getGenre(), request.getInstrumentIds(), request.getCountry(), request.getCity());

        if (userDetails == null) {
            throw new Exception("User not authenticated");
        }

        Artist artist = artistService.createArtistForCurrentUser(
                request.getStageName(),
                request.getBiography(),
                request.getGenre(),
                request.getInstrumentIds(),
                request.getMainInstrumentId(),
                request.getCountry(),
                request.getCity()
        );

        log.info("Artist created: id={}, stageName={}", artist.getId(), artist.getStageName());

        return artist;
    }

    @GetMapping("/{id}")
    public Artist getArtistById(@PathVariable Long id) throws Exception {
        System.out.println("=== GET ARTIST CONTROLLER ===");
        System.out.println("Requested ID: " + id);
        return artistService.getArtistById(id);
    }

    // ========== NUEVO ENDPOINT PARA SUBIR IMAGEN DE ARTISTA ==========

    @PostMapping("/{artistId}/image")
    public ResponseEntity<ImageUploadResponse> uploadArtistImage(
            @PathVariable Long artistId,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        log.info("=== UPLOAD ARTIST IMAGE ===");
        log.info("Artist ID: {}", artistId);
        log.info("User: {}", userDetails.getUsername());

        Artist artist = artistService.getArtistById(artistId);
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        // Validar que el usuario sea el dueño del artista o admin
        if (!artist.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("No tienes permiso para modificar este artista")
                            .build());
        }

        try {
            String imageUrl = imageUploadService.uploadImage(file, "artists");

            artist.setProfileImageUrl(imageUrl);
            artistService.save(artist);

            return ResponseEntity.ok(ImageUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .message("Imagen del artista subida exitosamente")
                    .success(true)
                    .build());
        } catch (IOException e) {
            log.error("Error uploading artist image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("Error al subir la imagen: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/{artistId}/image")
    public ResponseEntity<?> deleteArtistImage(
            @PathVariable Long artistId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Artist artist = artistService.getArtistById(artistId);
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        if (!artist.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String oldImageUrl = artist.getProfileImageUrl();
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            imageUploadService.deleteImage(oldImageUrl);
            artist.setProfileImageUrl(null);
            artistService.save(artist);
        }

        return ResponseEntity.ok().build();
    }

    //================logo  de artista ===================
    @PostMapping("/{artistId}/logo")
    public ResponseEntity<ImageUploadResponse> uploadArtistLogo(
            @PathVariable Long artistId,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Artist artist = artistService.getArtistById(artistId);
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        // Verificar que el usuario es el dueño del artista
        if (!artist.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("No tienes permiso para modificar este artista")
                            .build());
        }

        try {
            String imageUrl = imageUploadService.uploadImage(file, "artists");
            artist.setLogoUrl(imageUrl);
            artistService.save(artist);

            return ResponseEntity.ok(ImageUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .message("Logo del artista subido exitosamente")
                    .success(true)
                    .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("Error al subir el logo: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/{artistId}/profile-image")
    public ResponseEntity<ImageUploadResponse> uploadArtistProfileImage(
            @PathVariable Long artistId,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Artist artist = artistService.getArtistById(artistId);
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        if (!artist.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("No tienes permiso para modificar este artista")
                            .build());
        }

        try {
            String imageUrl = imageUploadService.uploadImage(file, "artists");
            artist.setProfileImageUrl(imageUrl);
            artistService.save(artist);

            return ResponseEntity.ok(ImageUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .message("Imagen de perfil del artista subida exitosamente")
                    .success(true)
                    .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("Error al subir la imagen: " + e.getMessage())
                            .build());
        }
    }
    // ========== LOOKING FOR BAND - ENDPOINTS ==========

    /**
     * PUT /api/artists/looking-for-band/status
     * Activar/desactivar la opción "Quiero formar banda"
     */
    @PutMapping("/looking-for-band/status")
    public ResponseEntity<?> updateLookingForBandStatus(@RequestBody Map<String, Boolean> request) {

        log.info("=== PUT /api/artists/looking-for-band/status ===");
        Boolean isLookingForBand = request.get("isLookingForBand");
        log.info("isLookingForBand: {}", isLookingForBand);

        if (isLookingForBand == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Falta el campo isLookingForBand"));
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !(auth.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no autenticado"));
            }

            String email = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userService.findUserByEmail(email);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
            }

            Artist artist = artistService.getArtistByUserId(user.getId());

            if (artist == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No tienes un perfil de artista"));
            }

            artist.setLookingForBand(isLookingForBand);
            Artist saved = artistService.save(artist);

            String message = saved.isLookingForBand()
                    ? "✅ Ahora aparecerás en el directorio de músicos disponibles"
                    : "❌ Ya no aparecerás en el directorio de músicos disponibles";

            return ResponseEntity.ok(Map.of(
                    "isLookingForBand", saved.isLookingForBand(),
                    "message", message
            ));

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/artists/looking-for-band/status
     * Obtener el estado actual
     */
    @GetMapping("/looking-for-band/status")
    public ResponseEntity<?> getLookingForBandStatus() {

        log.info("=== GET /api/artists/looking-for-band/status ===");

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !(auth.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("isLookingForBand", false));
            }

            String email = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userService.findUserByEmail(email);

            if (user == null) {
                return ResponseEntity.ok(Map.of("isLookingForBand", false));
            }

            Artist artist = artistService.getArtistByUserId(user.getId());

            if (artist == null) {
                return ResponseEntity.ok(Map.of("isLookingForBand", false));
            }

            String message = artist.isLookingForBand()
                    ? "🎸 Estás visible en el directorio de músicos disponibles"
                    : "📢 No estás visible en el directorio. Activa la opción para aparecer.";

            return ResponseEntity.ok(Map.of(
                    "isLookingForBand", artist.isLookingForBand(),
                    "message", message
            ));

        } catch (Exception e) {
            log.error("Error en getLookingForBandStatus: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("isLookingForBand", false));
        }
    }

    @GetMapping("/simple")
    public ResponseEntity<?> getSimpleArtists() {
        log.info("=== GET /api/artists/simple ===");
        try {
            List<Artist> artists = artistService.getAllArtists();

            // Respuesta extremadamente simple
            List<Map<String, Object>> simpleResponse = new ArrayList<>();
            for (Artist artist : artists) {
                Map<String, Object> simple = new HashMap<>();
                simple.put("id", artist.getId());
                simple.put("stageName", artist.getStageName());
                simple.put("lookingForBand", artist.isLookingForBand());
                simple.put("genre", artist.getGenre() != null ? artist.getGenre().name() : "");
                simple.put("city", artist.getCity());
                simple.put("country", artist.getCountry());
                simple.put("profileImageUrl", artist.getProfileImageUrl());
                simple.put("instruments", new ArrayList<>()); // Vacío por ahora
                simpleResponse.add(simple);
            }

            return ResponseEntity.ok(simpleResponse);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

//    @GetMapping
//    public ResponseEntity<?> getAllArtists() {
//        log.info("=== GET /api/artists ===");
//        try {
//            // Obtener el usuario actual
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            Long currentUserId = null;
//
//            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
//                String email = ((UserDetails) auth.getPrincipal()).getUsername();
//                User currentUser = userService.findUserByEmail(email);
//                if (currentUser != null) {
//                    currentUserId = currentUser.getId();
//                }
//            }
//
//            List<Artist> artists = artistService.getAllArtists();
//
//            // Filtrar para excluir al artista del usuario logueado
//            List<Map<String, Object>> response = new ArrayList<>();
//            for (Artist artist : artists) {
//                // Saltar al artista del usuario actual
//                if (currentUserId != null && artist.getUser() != null &&
//                        artist.getUser().getId().equals(currentUserId)) {
//                    continue;
//                }
//
//                Map<String, Object> map = new HashMap<>();
//                map.put("id", artist.getId());
//                map.put("stageName", artist.getStageName());
//                map.put("genre", artist.getGenre() != null ? artist.getGenre().name() : "");
//                map.put("city", artist.getCity());
//                map.put("country", artist.getCountry());
//                map.put("lookingForBand", artist.isLookingForBand());
//                map.put("lookingForGenres", artist.getLookingForGenres());
//                map.put("profileImageUrl", artist.getProfileImageUrl());
//                map.put("verified", artist.isVerified());
//
//                // Instrumentos como lista simple
//                List<Map<String, Object>> instruments = new ArrayList<>();
//                if (artist.getInstruments() != null) {
//                    for (Instrument instrument : artist.getInstruments()) {
//                        Map<String, Object> inst = new HashMap<>();
//                        inst.put("id", instrument.getId());
//                        inst.put("name", instrument.getName());
//                        instruments.add(inst);
//                    }
//                }
//                map.put("instruments", instruments);
//                response.add(map);
//            }
//
//            // Filtrar solo los que buscan banda
//            List<Map<String, Object>> lookingForBand = response.stream()
//                    .filter(artist -> Boolean.TRUE.equals(artist.get("lookingForBand")))
//                    .collect(Collectors.toList());
//
//            log.info("Artistas que buscan banda (excluyendo al actual): {}", lookingForBand.size());
//
//            return ResponseEntity.ok(lookingForBand);
//
//        } catch (Exception e) {
//            log.error("Error al obtener artistas: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }

    // ========== LOOKING FOR INSTRUMENTS - ENDPOINTS ==========

    /**
     * PUT /api/artists/looking-for-band/instruments
     * Guardar los instrumentos que el artista puede tocar
     */
    @PutMapping("/looking-for-band/instruments")
    public ResponseEntity<?> updateLookingForInstruments(
            @RequestBody LookingForInstrumentsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("=== PUT /api/artists/looking-for-band/instruments ===");
        log.info("Instrument IDs: {}", request.getInstrumentIds());

        if (request.getInstrumentIds() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Falta el campo instrumentIds"));
        }

        try {
            // Obtener usuario autenticado
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no autenticado"));
            }

            String email = userDetails.getUsername();
            User user = userService.findUserByEmail(email);

            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            Artist artist = artistService.getArtistByUserId(user.getId());

            if (artist == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tienes un perfil de artista"));
            }

            // Validar que los instrumentos existen
            List<Instrument> validInstruments = new ArrayList<>();
            for (Long instrumentId : request.getInstrumentIds()) {
                Instrument instrument = instrumentRepository.findById(instrumentId)
                        .orElseThrow(() -> new Exception("Instrumento no encontrado: " + instrumentId));
                validInstruments.add(instrument);
            }

            // Guardar los IDs de instrumentos
            artist.setLookingForInstrumentIds(request.getInstrumentIds());
            artistService.save(artist);

            // Construir respuesta
            List<LookingForInstrumentsResponse.InstrumentDTO> instrumentDTOs = validInstruments.stream()
                    .map(instr -> LookingForInstrumentsResponse.InstrumentDTO.builder()
                            .id(instr.getId())
                            .name(instr.getName())
                            .category(instr.getCategory())
                            .build())
                    .collect(Collectors.toList());

            LookingForInstrumentsResponse response = LookingForInstrumentsResponse.builder()
                    .instruments(instrumentDTOs)
                    .count(instrumentDTOs.size())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/artists/looking-for-band/instruments
     * Obtener los instrumentos que el artista puede tocar
     */
    @GetMapping("/looking-for-band/instruments")
    public ResponseEntity<?> getLookingForInstruments(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("=== GET /api/artists/looking-for-band/instruments ===");

        try {
            if (userDetails == null) {
                return ResponseEntity.ok(Map.of("instruments", new ArrayList<>(), "count", 0));
            }

            String email = userDetails.getUsername();
            User user = userService.findUserByEmail(email);

            if (user == null) {
                return ResponseEntity.ok(Map.of("instruments", new ArrayList<>(), "count", 0));
            }

            Artist artist = artistService.getArtistByUserId(user.getId());

            if (artist == null || artist.getLookingForInstrumentIds() == null ||
                    artist.getLookingForInstrumentIds().isEmpty()) {
                return ResponseEntity.ok(Map.of("instruments", new ArrayList<>(), "count", 0));
            }

            // Obtener instrumentos por IDs
            List<Instrument> instruments = instrumentRepository.findAllById(artist.getLookingForInstrumentIds());

            List<LookingForInstrumentsResponse.InstrumentDTO> instrumentDTOs = instruments.stream()
                    .map(instr -> LookingForInstrumentsResponse.InstrumentDTO.builder()
                            .id(instr.getId())
                            .name(instr.getName())
                            .category(instr.getCategory())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "instruments", instrumentDTOs,
                    "count", instrumentDTOs.size()
            ));

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== LOOKING FOR GENRES - ENDPOINTS ==========

    /**
     * PUT /api/artists/looking-for-band/genres
     * Guardar los géneros musicales que el artista busca
     */
    @PutMapping("/looking-for-band/genres")
    public ResponseEntity<?> updateLookingForGenres(
            @RequestBody LookingForGenresRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("=== PUT /api/artists/looking-for-band/genres ===");
        log.info("Genres: {}", request.getGenres());

        if (request.getGenres() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Falta el campo genres"));
        }

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no autenticado"));
            }

            String email = userDetails.getUsername();
            User user = userService.findUserByEmail(email);

            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            Artist artist = artistService.getArtistByUserId(user.getId());

            if (artist == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tienes un perfil de artista"));
            }

            // Validar que los géneros existen en el enum
            List<String> validGenres = new ArrayList<>();
            for (String genre : request.getGenres()) {
                try {
                    MusicGenre.valueOf(genre);
                    validGenres.add(genre);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Género inválido: " + genre));
                }
            }

            artist.setLookingForGenres(validGenres);
            artistService.save(artist);

            LookingForGenresResponse response = LookingForGenresResponse.builder()
                    .genres(validGenres)
                    .count(validGenres.size())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/artists/looking-for-band/genres
     * Obtener los géneros musicales que el artista busca
     */
    @GetMapping("/looking-for-band/genres")
    public ResponseEntity<?> getLookingForGenres(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("=== GET /api/artists/looking-for-band/genres ===");

        try {
            if (userDetails == null) {
                return ResponseEntity.ok(Map.of("genres", new ArrayList<>(), "count", 0));
            }

            String email = userDetails.getUsername();
            User user = userService.findUserByEmail(email);

            if (user == null) {
                return ResponseEntity.ok(Map.of("genres", new ArrayList<>(), "count", 0));
            }

            Artist artist = artistService.getArtistByUserId(user.getId());

            if (artist == null || artist.getLookingForGenres() == null) {
                return ResponseEntity.ok(Map.of("genres", new ArrayList<>(), "count", 0));
            }

            return ResponseEntity.ok(Map.of(
                    "genres", artist.getLookingForGenres(),
                    "count", artist.getLookingForGenres().size()
            ));

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllArtists(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) List<Long> instrumentIds,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city
    ) {
        log.info("=== GET /api/artists ===");
        log.info("Filters - genre: {}, instrumentIds: {}, country: {}, city: {}",
                genre, instrumentIds, country, city);

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = null;

            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                String email = ((UserDetails) auth.getPrincipal()).getUsername();
                User currentUser = userService.findUserByEmail(email);
                if (currentUser != null) {
                    currentUserId = currentUser.getId();
                }
            }

            // Aplicar filtros combinados
            List<Artist> artists = artistService.findArtistsWithFilters(
                    genre, instrumentIds, country, city
            );

            // Filtrar para excluir al artista del usuario logueado
            List<Map<String, Object>> response = new ArrayList<>();
            for (Artist artist : artists) {
                if (currentUserId != null && artist.getUser() != null &&
                        artist.getUser().getId().equals(currentUserId)) {
                    continue;
                }

                Map<String, Object> map = new HashMap<>();
                map.put("id", artist.getId());
                map.put("stageName", artist.getStageName());
                map.put("genre", artist.getGenre() != null ? artist.getGenre().name() : "");
                map.put("city", artist.getCity());
                map.put("country", artist.getCountry());
                map.put("lookingForBand", artist.isLookingForBand());
                map.put("lookingForGenres", artist.getLookingForGenres());
                map.put("profileImageUrl", artist.getProfileImageUrl());
                map.put("verified", artist.isVerified());

                // Instrumentos como lista simple
                List<Map<String, Object>> instruments = new ArrayList<>();
                if (artist.getInstruments() != null) {
                    for (Instrument instrument : artist.getInstruments()) {
                        Map<String, Object> inst = new HashMap<>();
                        inst.put("id", instrument.getId());
                        inst.put("name", instrument.getName());
                        instruments.add(inst);
                    }
                }
                map.put("instruments", instruments);
                response.add(map);
            }

            // Filtrar solo los que buscan banda
            List<Map<String, Object>> lookingForBand = response.stream()
                    .filter(artist -> Boolean.TRUE.equals(artist.get("lookingForBand")))
                    .collect(Collectors.toList());

            log.info("Artistas que buscan banda: {}", lookingForBand.size());

            return ResponseEntity.ok(lookingForBand);

        } catch (Exception e) {
            log.error("Error al obtener artistas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


}