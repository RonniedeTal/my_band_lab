package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Album;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.Song;
import com.my_band_lab.my_band_lab.repository.AlbumRepository;
import com.my_band_lab.my_band_lab.repository.ArtistRepository;
import com.my_band_lab.my_band_lab.repository.MusicGroupRepository;
import com.my_band_lab.my_band_lab.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final MusicGroupRepository musicGroupRepository;
    private final SongRepository songRepository;

    @Override
    @Transactional
    public Album createAlbum(String title, String description, LocalDate releaseDate, String coverImageUrl, Long artistId, Long groupId) throws Exception {
        // Validar que se proporcione artista O grupo
        if ((artistId == null && groupId == null) || (artistId != null && groupId != null)) {
            throw new IllegalArgumentException("Debes proporcionar artistId O groupId, no ambos");
        }

        Album album = Album.builder()
                .title(title)
                .description(description)
                .releaseDate(releaseDate)
                .coverImageUrl(coverImageUrl)
                .build();

        if (artistId != null) {
            Artist artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new Exception("Artista no encontrado"));
            album.setArtist(artist);
        } else if (groupId != null) {
            MusicGroup group = musicGroupRepository.findById(groupId)
                    .orElseThrow(() -> new Exception("Grupo no encontrado"));
            album.setMusicGroup(group);
        }

        log.info("📀 Álbum creado: {}", title);
        return albumRepository.save(album);
    }

    @Override
    @Transactional
    public Album updateAlbum(Long albumId, String title, String description, LocalDate releaseDate, String coverImageUrl) throws Exception {
        Album album = getAlbumById(albumId);

        if (title != null) album.setTitle(title);
        if (description != null) album.setDescription(description);
        if (releaseDate != null) album.setReleaseDate(releaseDate);
        if (coverImageUrl != null) album.setCoverImageUrl(coverImageUrl);

        log.info("📀 Álbum actualizado: {}", album.getTitle());
        return albumRepository.save(album);
    }

    @Override
    @Transactional
    public void deleteAlbum(Long albumId) throws Exception {
        Album album = getAlbumById(albumId);
        albumRepository.delete(album);
        log.info("🗑️ Álbum eliminado: {}", album.getTitle());
    }

    @Override
    public Album getAlbumById(Long albumId) throws Exception {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new Exception("Álbum no encontrado con id: " + albumId));
    }

    @Override
    public List<Album> getAlbumsByArtistId(Long artistId) throws Exception {
        return albumRepository.findByArtistIdOrderByReleaseDateDesc(artistId);
    }

    @Override
    public List<Album> getAlbumsByGroupId(Long groupId) throws Exception {
        return albumRepository.findByMusicGroupIdOrderByReleaseDateDesc(groupId);
    }

    @Override
    @Transactional
    public Album addSongToAlbum(Long albumId, Long songId) throws Exception {
        Album album = getAlbumById(albumId);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new Exception("Canción no encontrada"));

        song.setAlbum(album);
        songRepository.save(song);

        log.info("🎵 Canción '{}' agregada al álbum '{}'", song.getTitle(), album.getTitle());
        return album;
    }

    @Override
    @Transactional
    public Album removeSongFromAlbum(Long albumId, Long songId) throws Exception {
        Album album = getAlbumById(albumId);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new Exception("Canción no encontrada"));

        song.setAlbum(null);
        songRepository.save(song);

        log.info("🎵 Canción '{}' removida del álbum '{}'", song.getTitle(), album.getTitle());
        return album;
    }
}