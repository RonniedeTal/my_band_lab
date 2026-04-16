package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlaylistService {

    Playlist createPlaylist(Long userId, String title, String description, String coverImageUrl, boolean isPublic);

    Playlist updatePlaylist(Long playlistId, Long userId, String title, String description, String coverImageUrl, Boolean isPublic);

    void deletePlaylist(Long playlistId, Long userId);

    Playlist getPlaylistById(Long playlistId, Long currentUserId);

    Page<Playlist> getUserPlaylists(Long userId, Pageable pageable);

    Page<Playlist> getPublicPlaylists(Pageable pageable);

    Page<Playlist> searchPublicPlaylists(String query, Pageable pageable);
}