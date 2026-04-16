package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Playlist;
import com.my_band_lab.my_band_lab.entity.PlaylistSong;
import com.my_band_lab.my_band_lab.entity.Song;
import com.my_band_lab.my_band_lab.repository.PlaylistRepository;
import com.my_band_lab.my_band_lab.repository.PlaylistSongRepository;
import com.my_band_lab.my_band_lab.repository.SongRepository;
import com.my_band_lab.my_band_lab.service.PlaylistSongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistSongServiceImpl implements PlaylistSongService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;

    @Override
    @Transactional
    public PlaylistSong addSongToPlaylist(Long playlistId, Long userId, Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));

        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para modificar esta playlist");
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada"));

        // Verificar si ya existe
        if (playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId).isPresent()) {
            throw new RuntimeException("La canción ya está en la playlist");
        }

        int currentCount = playlistSongRepository.countByPlaylistId(playlistId);

        PlaylistSong playlistSong = PlaylistSong.builder()
                .playlist(playlist)
                .song(song)
                .position(currentCount)
                .build();

        log.info("🎵 Canción '{}' añadida a playlist '{}'", song.getTitle(), playlist.getTitle());
        return playlistSongRepository.save(playlistSong);
    }

    @Override
    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long userId, Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));

        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para modificar esta playlist");
        }

        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
        log.info("🎵 Canción removida de playlist '{}'", playlist.getTitle());
    }

    @Override
    public List<PlaylistSong> getPlaylistSongs(Long playlistId, Long userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));

        if (!playlist.isPublic() && !playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para ver esta playlist");
        }

        return playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlistId);
    }

    @Override
    @Transactional
    public void reorderPlaylistSongs(Long playlistId, Long userId, List<Long> songIdsInOrder) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist no encontrada"));

        if (!playlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para modificar esta playlist");
        }

        for (int i = 0; i < songIdsInOrder.size(); i++) {
            Long songId = songIdsInOrder.get(i);
            PlaylistSong playlistSong = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                    .orElseThrow(() -> new RuntimeException("Canción no encontrada en la playlist"));
            playlistSong.setPosition(i);
            playlistSongRepository.save(playlistSong);
        }

        log.info("📀 Playlist '{}' reordenada", playlist.getTitle());
    }
}