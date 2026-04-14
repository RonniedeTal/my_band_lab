package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.CreateArtistForCurrentUserRequest;
import com.my_band_lab.my_band_lab.dto.ImageUploadResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    private static final Logger log = LoggerFactory.getLogger(ArtistController.class);

    @PostMapping("/create")
    public Artist createArtist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateArtistForCurrentUserRequest request) throws Exception {

        log.info("=== CREATE ARTIST REQUEST ===");
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "null");
        log.info("Request: stageName={}, genre={}, instrumentIds={}",
                request.getStageName(), request.getGenre(), request.getInstrumentIds());

        if (userDetails == null) {
            throw new Exception("User not authenticated");
        }

        Artist artist = artistService.createArtistForCurrentUser(
                request.getStageName(),
                request.getBiography(),
                request.getGenre(),
                request.getInstrumentIds(),
                request.getMainInstrumentId()
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
}