package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.ImageUploadResponse;
import com.my_band_lab.my_band_lab.entity.*;
import com.my_band_lab.my_band_lab.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final AudioUploadService audioUploadService;
    private final UserService userService;
    private final ArtistService artistService;
    private final MusicGroupService musicGroupService;
    private final SongService songService;

    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponse> uploadSong(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "artistId", required = false) Long artistId,
            @RequestParam(value = "groupId", required = false) Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        // Validar que se proporcione artista O grupo (no ambos)
        if ((artistId == null && groupId == null) || (artistId != null && groupId != null)) {
            return ResponseEntity.badRequest()
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("Debes proporcionar artistId O groupId, no ambos")
                            .build());
        }

        // Validar permisos y estado verificado
        if (artistId != null) {
            Artist artist = artistService.getArtistById(artistId);

            // Verificar que el usuario es el dueño del artista
            if (!artist.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ImageUploadResponse.builder()
                                .success(false)
                                .message("No tienes permiso para subir canciones a este artista")
                                .build());
            }

            // Verificar que el artista está verificado
            if (!artist.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ImageUploadResponse.builder()
                                .success(false)
                                .message("Solo artistas verificados pueden subir canciones")
                                .build());
            }
        } else if (groupId != null) {
            MusicGroup group = musicGroupService.getGroupById(groupId);

            // Verificar que el usuario es el fundador del grupo
            if (!group.getFounder().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ImageUploadResponse.builder()
                                .success(false)
                                .message("No tienes permiso para subir canciones a este grupo")
                                .build());
            }

            // Verificar que el grupo está verificado
            if (!group.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ImageUploadResponse.builder()
                                .success(false)
                                .message("Solo grupos verificados pueden subir canciones")
                                .build());
            }
        }

        try {
            // Subir archivo MP3 a Cloudinary
            String audioUrl = audioUploadService.uploadMp3(file, "songs");

            // Crear la canción (duración por ahora 0, luego se puede extraer)
            Song song = Song.builder()
                    .title(title)
                    .duration(0) // TODO: Extraer duración real del MP3
                    .fileUrl(audioUrl)
                    .playCount(0)
                    .artist(artistId != null ? artistService.getArtistById(artistId) : null)
                    .musicGroup(groupId != null ? musicGroupService.getGroupById(groupId) : null)
                    .build();

            Song savedSong = songService.save(song);

            return ResponseEntity.ok(ImageUploadResponse.builder()
                    .imageUrl(audioUrl)
                    .message("Canción subida exitosamente")
                    .success(true)
                    .build());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("Error al subir la canción: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<ImageUploadResponse> deleteSong(
            @PathVariable Long songId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        Song song = songService.getSongById(songId);

        // Verificar permisos
        boolean isAuthorized = false;

        if (song.getArtist() != null) {
            isAuthorized = song.getArtist().getUser().getId().equals(currentUser.getId());
        } else if (song.getMusicGroup() != null) {
            isAuthorized = song.getMusicGroup().getFounder().getId().equals(currentUser.getId());
        }

        if (!isAuthorized) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("No tienes permiso para eliminar esta canción")
                            .build());
        }

        // Eliminar archivo de Cloudinary
        audioUploadService.deleteMp3(song.getFileUrl());

        // Eliminar de la base de datos
        songService.deleteSong(songId);

        return ResponseEntity.ok(ImageUploadResponse.builder()
                .success(true)
                .message("Canción eliminada exitosamente")
                .build());
    }

    @PostMapping("/{songId}/play")
    public ResponseEntity<ImageUploadResponse> registerPlay(
            @PathVariable Long songId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        SongPlay play = songService.registerPlay(songId, currentUser.getId());

        return ResponseEntity.ok(ImageUploadResponse.builder()
                .success(true)
                .message("Reproducción registrada")
                .build());
    }
}