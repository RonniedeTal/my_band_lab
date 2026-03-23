package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGenre;

import java.util.List;

public interface ArtistService {
    Artist createArtist(Long userId, String stageName, String biography, MusicGenre genre) throws Exception;
    Artist getArtistByUserId(Long userId) throws Exception;
    Artist getArtistById(Long id) throws Exception;
    List<Artist> getArtistsByGenre(MusicGenre genre) throws Exception;
    List<Artist> getAllArtists() throws Exception;
    Artist updateArtist(Long artistId, String stageName, String biography, MusicGenre genre) throws Exception;
    void deleteArtist(Long artistId) throws Exception;
}
