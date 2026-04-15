package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.SongStatsDTO;
import com.my_band_lab.my_band_lab.entity.Song;
import com.my_band_lab.my_band_lab.entity.SongPlay;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.SongPlayRepository;
import com.my_band_lab.my_band_lab.repository.SongRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final SongPlayRepository songPlayRepository;
    private final UserRepository userRepository;

    @Override
    public Song save(Song song) {
        return songRepository.save(song);
    }

    @Override
    public List<Song> getSongsByArtistId(Long artistId) {
        return songRepository.findByArtistIdOrderByCreatedAtDesc(artistId);
    }

    @Override
    public List<Song> getSongsByGroupId(Long groupId) {
        return songRepository.findByMusicGroupIdOrderByCreatedAtDesc(groupId);
    }

    @Override
    public Song getSongById(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canción no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public void incrementPlayCount(Long songId) {
        Song song = getSongById(songId);
        song.setPlayCount(song.getPlayCount() + 1);
        songRepository.save(song);
    }

    @Override
    public void deleteSong(Long songId) {
        songRepository.deleteById(songId);
    }

    @Override
    @Transactional
    public SongPlay registerPlay(Long songId, Long userId) {
        Song song = getSongById(songId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si ya reprodujo esta canción en las últimas 24 horas
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        java.util.Optional<SongPlay> existingPlay = songPlayRepository
                .findByUserIdAndSongIdAndPlayDateAfter(userId, songId, twentyFourHoursAgo);

        if (existingPlay.isPresent()) {
            // No registrar duplicado, pero devolver el existente
            return existingPlay.get();
        }

        // Registrar nueva reproducción
        SongPlay play = SongPlay.builder()
                .user(user)
                .song(song)
                .build();

        // Incrementar contador de la canción
        song.setPlayCount(song.getPlayCount() + 1);
        songRepository.save(song);

        return songPlayRepository.save(play);
    }

    @Override
    public long getTotalPlays(Long songId) {
        return songPlayRepository.countBySongId(songId);
    }

    @Override
    public long getUniqueListenersCount(Long songId) {
        return songPlayRepository.countUniquePlaysBySong(songId);
    }

    @Override
    public List<SongStatsDTO> getTopSongsByArtist(Long artistId, int limit) {
        List<Object[]> results = songPlayRepository.findTopSongsByArtist(artistId);
        return results.stream()
                .limit(limit)
                .map(result -> {
                    Song song = (Song) result[0];
                    Long playCount = (Long) result[1];
                    return SongStatsDTO.builder()
                            .songId(song.getId())
                            .title(song.getTitle())
                            .playCount(playCount.intValue())
                            .uniqueListeners(songPlayRepository.countUniquePlaysBySong(song.getId()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SongStatsDTO> getTopSongsByGroup(Long groupId, int limit) {
        List<Object[]> results = songPlayRepository.findTopSongsByGroup(groupId);
        return results.stream()
                .limit(limit)
                .map(result -> {
                    Song song = (Song) result[0];
                    Long playCount = (Long) result[1];
                    return SongStatsDTO.builder()
                            .songId(song.getId())
                            .title(song.getTitle())
                            .playCount(playCount.intValue())
                            .uniqueListeners(songPlayRepository.countUniquePlaysBySong(song.getId()))
                            .build();
                })
                .collect(Collectors.toList());
    }
}