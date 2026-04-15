package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.SongStatsDTO;
import com.my_band_lab.my_band_lab.entity.Song;
import com.my_band_lab.my_band_lab.entity.SongPlay;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.SongService;
import com.my_band_lab.my_band_lab.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SongGraphQLController {

    private final SongService songService;
    private final UserService userService;

    @QueryMapping
    public List<Song> songsByArtist(@Argument Long artistId) {
        log.info("📀 Query: songsByArtist - artistId: {}", artistId);
        return songService.getSongsByArtistId(artistId);
    }

    @QueryMapping
    public List<Song> songsByGroup(@Argument Long groupId) {
        log.info("📀 Query: songsByGroup - groupId: {}", groupId);
        return songService.getSongsByGroupId(groupId);
    }

    @QueryMapping
    public Song songById(@Argument Long id) {
        log.info("📀 Query: songById - id: {}", id);
        return songService.getSongById(id);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteSong(@Argument Long id, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("🗑️ Mutation: deleteSong - id: {}", id);

        try {
            User currentUser = userService.findUserByEmail(userDetails.getUsername());
            Song song = songService.getSongById(id);

            boolean isAuthorized = false;

            if (song.getArtist() != null) {
                isAuthorized = song.getArtist().getUser().getId().equals(currentUser.getId());
            } else if (song.getMusicGroup() != null) {
                isAuthorized = song.getMusicGroup().getFounder().getId().equals(currentUser.getId());
            }

            if (!isAuthorized) {
                throw new SecurityException("No tienes permiso para eliminar esta canción");
            }

            songService.deleteSong(id);
            log.info("✅ Canción eliminada exitosamente");
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar canción: {}", e.getMessage());
            return false;
        }
    }

    @MutationMapping
    public Song incrementPlayCount(@Argument Long id) {
        log.info("📈 Mutation: incrementPlayCount - id: {}", id);
        songService.incrementPlayCount(id);
        return songService.getSongById(id);
    }

    // ========== NUEVOS METODOS PARA REGISTRO DE REPRODUCCIONES ==========

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean registerPlay(@Argument Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("🎵 Mutation: registerPlay - songId: {}", songId);

        try {
            User currentUser = userService.findUserByEmail(userDetails.getUsername());
            SongPlay play = songService.registerPlay(songId, currentUser.getId());
            log.info("✅ Reproducción registrada - playId: {}", play.getId());
            return true;
        } catch (Exception e) {
            log.error("Error al registrar reproducción: {}", e.getMessage());
            return false;
        }
    }

    @QueryMapping
    public SongStatsDTO songStats(@Argument Long songId) {
        log.info("📊 Query: songStats - songId: {}", songId);

        Song song = songService.getSongById(songId);
        long uniqueListeners = songService.getUniqueListenersCount(songId);

        return SongStatsDTO.builder()
                .songId(songId)
                .title(song.getTitle())
                .playCount(song.getPlayCount())
                .uniqueListeners(uniqueListeners)
                .build();
    }

    @QueryMapping
    public List<SongStatsDTO> topSongsByArtist(@Argument Long artistId, @Argument Integer limit) {
        log.info("📊 Query: topSongsByArtist - artistId: {}, limit: {}", artistId, limit);

        int maxResults = limit != null ? limit : 5;
        return songService.getTopSongsByArtist(artistId, maxResults);
    }

    @QueryMapping
    public List<SongStatsDTO> topSongsByGroup(@Argument Long groupId, @Argument Integer limit) {
        log.info("📊 Query: topSongsByGroup - groupId: {}, limit: {}", groupId, limit);

        int maxResults = limit != null ? limit : 5;
        return songService.getTopSongsByGroup(groupId, maxResults);
    }
}