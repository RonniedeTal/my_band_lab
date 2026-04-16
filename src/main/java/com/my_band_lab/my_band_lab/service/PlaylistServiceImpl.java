package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Playlist;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.PlaylistRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import com.my_band_lab.my_band_lab.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Playlist createPlaylist(Long userId, String title, String description, String coverImageUrl, boolean isPublic) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Playlist playlist = Playlist.builder()
                .title(title)
                .description(description)
                .coverImageUrl(coverImageUrl)
                .isPublic(isPublic)
                .user(user)
                .build();

        log.info("📀 Playlist creada: {} por usuario {}", title, user.getEmail());
        return playlistRepository.save(playlist);
    }

    @Override
    @Transactional
    public Playlist updatePlaylist(Long playlistId, Long userId, String title, String description, String coverImageUrl, Boolean isPublic) {
        Playlist playlist = getPlaylistById(playlistId, userId);

        if (title != null) playlist.setTitle(title);
        if (description != null) playlist.setDescription(description);
        if (coverImageUrl != null) playlist.setCoverImageUrl(coverImageUrl);
        if (isPublic != null) playlist.setPublic(isPublic);

        log.info("📀 Playlist actualizada: {}", playlist.getTitle());
        return playlistRepository.save(playlist);
    }

    @Override
    @Transactional
    public void deletePlaylist(Long playlistId, Long userId) {
        Playlist playlist = getPlaylistById(playlistId, userId);
        playlistRepository.delete(playlist);
        log.info("🗑️ Playlist eliminada: {}", playlist.getTitle());
    }

    @Override
    public Playlist getPlaylistById(Long playlistId, Long currentUserId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));

        if (!playlist.isPublic() && !playlist.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("No tienes permiso para ver esta playlist");
        }

        return playlist;
    }

    @Override
    public Page<Playlist> getUserPlaylists(Long userId, Pageable pageable) {
        return playlistRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Playlist> getPublicPlaylists(Pageable pageable) {
        return playlistRepository.findPublicPlaylists(pageable);
    }

    @Override
    public Page<Playlist> searchPublicPlaylists(String query, Pageable pageable) {
        return playlistRepository.searchPublicPlaylists(query, pageable);
    }
}