package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.CreateArtistRequest;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.Instrument;
import com.my_band_lab.my_band_lab.entity.MusicGenre;

import java.util.List;

public interface ArtistService {

    // MODIFICADO: ahora recibe CreateArtistRequest
    Artist createArtist(CreateArtistRequest request) throws Exception;

    Artist getArtistByUserId(Long userId) throws Exception;

    Artist getArtistById(Long id) throws Exception;

    List<Artist> getArtistsByGenre(MusicGenre genre) throws Exception;

    List<Artist> getAllArtists() throws Exception;

    Artist updateArtist(Long artistId, String stageName, String biography, MusicGenre genre) throws Exception;

    void deleteArtist(Long artistId) throws Exception;

    // Métodos para instrumentos
    Artist updateArtistInstruments(Long artistId, List<Long> instrumentIds, Long mainInstrumentId) throws Exception;

    List<Instrument> getArtistInstruments(Long artistId) throws Exception;

    List<Artist> getArtistsByInstrument(Long instrumentId) throws Exception;

    PageResponse<Artist> getAllArtistsPaginated(int page, int size) throws Exception;
}