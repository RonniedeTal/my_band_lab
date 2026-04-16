package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.PlaylistSong;
import java.util.List;

public interface PlaylistSongService {

    PlaylistSong addSongToPlaylist(Long playlistId, Long userId, Long songId);

    void removeSongFromPlaylist(Long playlistId, Long userId, Long songId);

    List<PlaylistSong> getPlaylistSongs(Long playlistId, Long userId);

    void reorderPlaylistSongs(Long playlistId, Long userId, List<Long> songIdsInOrder);
}