package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.ImageUploadResponse;
import com.my_band_lab.my_band_lab.entity.Album;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.AlbumService;
import com.my_band_lab.my_band_lab.service.ImageUploadService;
import com.my_band_lab.my_band_lab.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final UserService userService;
    private final ImageUploadService imageUploadService;

    @PostMapping("/create")
    public ResponseEntity<Album> createAlbum(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        // Validar permisos (el usuario debe ser dueño del artista o fundador del grupo)
        if (artistId != null) {
            // Verificar que el usuario es dueño del artista
            // (Se puede implementar según tu lógica)
        } else if (groupId != null) {
            // Verificar que el usuario es fundador del grupo
            // (Se puede implementar según tu lógica)
        }

        Album album = albumService.createAlbum(title, description, releaseDate, null, artistId, groupId);
        return ResponseEntity.ok(album);
    }

    @PostMapping("/{albumId}/cover")
    public ResponseEntity<ImageUploadResponse> uploadCoverImage(
            @PathVariable Long albumId,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Album album = albumService.getAlbumById(albumId);
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        // Verificar permisos
        boolean isAuthorized = false;

        if (album.getArtist() != null) {
            isAuthorized = album.getArtist().getUser().getId().equals(currentUser.getId());
        } else if (album.getMusicGroup() != null) {
            isAuthorized = album.getMusicGroup().getFounder().getId().equals(currentUser.getId());
        }

        if (!isAuthorized && !currentUser.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("No tienes permiso para modificar este álbum")
                            .build());
        }


        try {
            String imageUrl = imageUploadService.uploadImage(file, "albums");
            album.setCoverImageUrl(imageUrl);
            albumService.updateAlbum(albumId, null, null, null, imageUrl);

            return ResponseEntity.ok(ImageUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .message("Portada del álbum subida exitosamente")
                    .success(true)
                    .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("Error al subir la portada: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<Album> updateAlbum(
            @PathVariable Long albumId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Album album = albumService.updateAlbum(albumId, title, description, releaseDate, null);
        return ResponseEntity.ok(album);
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
            @PathVariable Long albumId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        albumService.deleteAlbum(albumId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<Album>> getAlbumsByArtist(@PathVariable Long artistId) throws Exception {
        return ResponseEntity.ok(albumService.getAlbumsByArtistId(artistId));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Album>> getAlbumsByGroup(@PathVariable Long groupId) throws Exception {
        return ResponseEntity.ok(albumService.getAlbumsByGroupId(groupId));
    }

    @PostMapping("/{albumId}/songs/{songId}")
    public ResponseEntity<Album> addSongToAlbum(
            @PathVariable Long albumId,
            @PathVariable Long songId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Album album = albumService.addSongToAlbum(albumId, songId);
        return ResponseEntity.ok(album);
    }

    @DeleteMapping("/{albumId}/songs/{songId}")
    public ResponseEntity<Album> removeSongFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long songId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Album album = albumService.removeSongFromAlbum(albumId, songId);
        return ResponseEntity.ok(album);
    }
}