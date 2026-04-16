package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.ImageUploadResponse;
import com.my_band_lab.my_band_lab.entity.Playlist;
import com.my_band_lab.my_band_lab.entity.PlaylistSong;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final PlaylistSongService playlistSongService;
    private final UserService userService;
    private final ImageUploadService imageUploadService;

    // ========== CRUD Playlists ==========

    @PostMapping("/create")
    public ResponseEntity<Playlist> createPlaylist(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean isPublic,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Playlist playlist = playlistService.createPlaylist(
                currentUser.getId(), title, description, null, isPublic);

        return ResponseEntity.ok(playlist);
    }

    @PostMapping("/{playlistId}/cover")
    public ResponseEntity<ImageUploadResponse> uploadCoverImage(
            @PathVariable Long playlistId,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        Playlist playlist = playlistService.getPlaylistById(playlistId, currentUser.getId());

        try {
            String imageUrl = imageUploadService.uploadImage(file, "playlists");
            playlistService.updatePlaylist(playlistId, currentUser.getId(), null, null, imageUrl, null);

            return ResponseEntity.ok(ImageUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .message("Portada de playlist subida exitosamente")
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

    @PutMapping("/{playlistId}")
    public ResponseEntity<Playlist> updatePlaylist(
            @PathVariable Long playlistId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPublic,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        Playlist playlist = playlistService.updatePlaylist(
                playlistId, currentUser.getId(), title, description, null, isPublic);

        return ResponseEntity.ok(playlist);
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        playlistService.deletePlaylist(playlistId, currentUser.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<Playlist> getPlaylistById(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        Playlist playlist = playlistService.getPlaylistById(playlistId, currentUser.getId());

        return ResponseEntity.ok(playlist);
    }

    @GetMapping("/user/me")
    public ResponseEntity<Page<Playlist>> getMyPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        Page<Playlist> playlists = playlistService.getUserPlaylists(currentUser.getId(), pageable);

        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<Playlist>> getPublicPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Playlist> playlists = playlistService.getPublicPlaylists(pageable);

        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<Playlist>> searchPublicPlaylists(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Playlist> playlists = playlistService.searchPublicPlaylists(q, pageable);

        return ResponseEntity.ok(playlists);
    }

    // ========== Gestión de canciones en playlists ==========

    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<PlaylistSong> addSongToPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        PlaylistSong playlistSong = playlistSongService.addSongToPlaylist(
                playlistId, currentUser.getId(), songId);

        return ResponseEntity.ok(playlistSong);
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        playlistSongService.removeSongFromPlaylist(playlistId, currentUser.getId(), songId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<List<PlaylistSong>> getPlaylistSongs(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<PlaylistSong> songs = playlistSongService.getPlaylistSongs(playlistId, currentUser.getId());

        return ResponseEntity.ok(songs);
    }

    @PutMapping("/{playlistId}/songs/reorder")
    public ResponseEntity<Void> reorderPlaylistSongs(
            @PathVariable Long playlistId,
            @RequestBody Map<String, List<Long>> request,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        List<Long> songIds = request.get("songIds");

        playlistSongService.reorderPlaylistSongs(playlistId, currentUser.getId(), songIds);

        return ResponseEntity.ok().build();
    }
}