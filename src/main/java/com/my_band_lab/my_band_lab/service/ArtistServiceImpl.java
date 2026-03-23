package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.ArtistRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArtistServiceImpl implements ArtistService {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Artist createArtist(Long userId, String stageName, String biography, MusicGenre genre) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        // Verificar si el usuario ya es artista
        if (artistRepository.findByUserId(userId).isPresent()) {
            throw new Exception("User is already an artist");
        }

        Artist artist = Artist.builder()
                .user(user)
                .stageName(stageName)
                .biography(biography)
                .genre(genre)
                .verified(false)
                .build();

        return artistRepository.save(artist);
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
        return artistRepository.findAll().stream()
                .filter(artist -> artist.getGenre() == genre)
                .collect(Collectors.toList());
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
}