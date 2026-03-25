package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.CreateArtistRequest;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.Instrument;
import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.ArtistRepository;
import com.my_band_lab.my_band_lab.repository.InstrumentRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArtistServiceImpl implements ArtistService {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Override
    @Transactional
    public Artist createArtist(CreateArtistRequest request) throws Exception {
        // 1. Verificar que el usuario existe
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new Exception("User not found with id: " + request.getUserId()));

        // 2. Verificar que el usuario no es ya artista
        if (artistRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new Exception("User is already an artist");
        }

        // 3. Crear el artista
        Artist artist = Artist.builder()
                .user(user)
                .stageName(request.getStageName())
                .biography(request.getBiography())
                .genre(request.getGenre())
                .verified(false)
                .build();

        Artist savedArtist = artistRepository.save(artist);

        // 4. Añadir instrumentos si se enviaron
        if (request.getInstrumentIds() != null && !request.getInstrumentIds().isEmpty()) {
            List<Instrument> instruments = new ArrayList<>();
            for (Long instrumentId : request.getInstrumentIds()) {
                Instrument instrument = instrumentRepository.findById(instrumentId)
                        .orElseThrow(() -> new Exception("Instrument not found with id: " + instrumentId));
                instruments.add(instrument);
            }
            savedArtist.setInstruments(instruments);

            // 5. Establecer instrumento principal
            if (request.getMainInstrumentId() != null) {
                boolean exists = request.getInstrumentIds().contains(request.getMainInstrumentId());
                if (!exists) {
                    throw new Exception("Main instrument must be one of the selected instruments");
                }
                savedArtist.setMainInstrumentId(request.getMainInstrumentId());
            }

            savedArtist = artistRepository.save(savedArtist);
        }

        return savedArtist;
    }

    @Override
    public Artist getArtistByUserId(Long userId) throws Exception {
        return artistRepository.findByUserId(userId)
                .orElseThrow(() -> new Exception("Artist not found for user: " + userId));
    }

    @Override
    public Artist getArtistById(Long id) throws Exception {
        return artistRepository.findById(id)
                .orElseThrow(() -> new Exception("Artist not found with id: " + id));
    }

    @Override
    public List<Artist> getArtistsByGenre(MusicGenre genre) throws Exception {
        return artistRepository.findByGenre(genre);
    }

    @Override
    public List<Artist> getAllArtists() throws Exception {
        List<Artist> artists = artistRepository.findAll();
        if (artists.isEmpty()) {
            throw new Exception("No artists found");
        }
        return artists;
    }

    @Override
    @Transactional
    public Artist updateArtist(Long artistId, String stageName, String biography, MusicGenre genre) throws Exception {
        Artist artist = getArtistById(artistId);

        if (stageName != null && !stageName.isEmpty()) {
            artist.setStageName(stageName);
        }

        if (biography != null) {
            artist.setBiography(biography);
        }

        if (genre != null) {
            artist.setGenre(genre);
        }

        return artistRepository.save(artist);
    }

    @Override
    public void deleteArtist(Long artistId) throws Exception {
        Artist artist = getArtistById(artistId);
        artistRepository.delete(artist);
    }

    @Override
    @Transactional
    public Artist updateArtistInstruments(Long artistId, List<Long> instrumentIds, Long mainInstrumentId) throws Exception {
        Artist artist = getArtistById(artistId);

        List<Instrument> instruments = new ArrayList<>();
        for (Long instId : instrumentIds) {
            Instrument instrument = instrumentRepository.findById(instId)
                    .orElseThrow(() -> new Exception("Instrument not found with id: " + instId));
            instruments.add(instrument);
        }

        artist.setInstruments(instruments);

        if (mainInstrumentId != null) {
            boolean exists = instrumentIds.contains(mainInstrumentId);
            if (!exists) {
                throw new Exception("Main instrument must be one of the selected instruments");
            }
            artist.setMainInstrumentId(mainInstrumentId);
        }

        return artistRepository.save(artist);
    }

    @Override
    public List<Instrument> getArtistInstruments(Long artistId) throws Exception {
        Artist artist = getArtistById(artistId);
        return artist.getInstruments();
    }

    @Override
    public List<Artist> getArtistsByInstrument(Long instrumentId) throws Exception {
        // Verificar que el instrumento existe
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new Exception("Instrument not found with id: " + instrumentId));

        // Buscar artistas que tocan ese instrumento
        List<Artist> artists = artistRepository.findByInstrumentId(instrumentId);

        // Devolver lista vacía si no hay resultados (nunca null)
        return artists != null ? artists : new ArrayList<>();
    }

    @Override
    public PageResponse<Artist> getAllArtistsPaginated(int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<Artist> artistPage = artistRepository.findAll(pageable);

        return PageResponse.<Artist>builder()
                .content(artistPage.getContent())
                .totalElements(artistPage.getTotalElements())
                .totalPages(artistPage.getTotalPages())
                .currentPage(artistPage.getNumber())
                .size(artistPage.getSize())
                .hasNext(artistPage.hasNext())
                .hasPrevious(artistPage.hasPrevious())
                .build();
    }
}