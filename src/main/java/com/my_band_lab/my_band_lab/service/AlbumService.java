package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Album;
import com.my_band_lab.my_band_lab.entity.Song;
import java.time.LocalDate;
import java.util.List;

public interface AlbumService {
    Album createAlbum(String title, String description, LocalDate releaseDate, String coverImageUrl, Long artistId, Long groupId) throws Exception;
    Album updateAlbum(Long albumId, String title, String description, LocalDate releaseDate, String coverImageUrl) throws Exception;
    void deleteAlbum(Long albumId) throws Exception;
    Album getAlbumById(Long albumId) throws Exception;
    List<Album> getAlbumsByArtistId(Long artistId) throws Exception;
    List<Album> getAlbumsByGroupId(Long groupId) throws Exception;
    Album addSongToAlbum(Long albumId, Long songId) throws Exception;
    Album removeSongFromAlbum(Long albumId, Long songId) throws Exception;
}