package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.SongStatsDTO;
import com.my_band_lab.my_band_lab.entity.Song;
import com.my_band_lab.my_band_lab.entity.SongPlay;

import java.util.List;

public interface SongService {
    Song save(Song song);
    List<Song> getSongsByArtistId(Long artistId);
    List<Song> getSongsByGroupId(Long groupId);
    Song getSongById(Long id);
    void incrementPlayCount(Long songId);
    void deleteSong(Long songId);
    SongPlay registerPlay(Long songId, Long userId);
    long getTotalPlays(Long songId);
    long getUniqueListenersCount(Long songId);
    List<SongStatsDTO> getTopSongsByArtist(Long artistId, int limit);
    List<SongStatsDTO> getTopSongsByGroup(Long groupId, int limit);
}