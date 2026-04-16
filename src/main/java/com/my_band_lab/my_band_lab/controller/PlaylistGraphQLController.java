package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Playlist;
import com.my_band_lab.my_band_lab.entity.PlaylistSong;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.PlaylistService;
import com.my_band_lab.my_band_lab.service.PlaylistSongService;
import com.my_band_lab.my_band_lab.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class PlaylistGraphQLController {

    private final PlaylistService playlistService;
    private final PlaylistSongService playlistSongService;
    private final UserService userService;

    // ========== QUERIES ==========

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public PageResponse<Playlist> myPlaylists(
            @Argument Integer page,
            @Argument Integer size,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        Page<Playlist> playlists = playlistService.getUserPlaylists(
                currentUser.getId(), PageRequest.of(pageNum, pageSize));

        return PageResponse.<Playlist>builder()
                .content(playlists.getContent())
                .totalElements(playlists.getTotalElements())
                .totalPages(playlists.getTotalPages())
                .currentPage(playlists.getNumber())
                .size(playlists.getSize())
                .hasNext(playlists.hasNext())
                .hasPrevious(playlists.hasPrevious())
                .build();
    }

    @QueryMapping
    public PageResponse<Playlist> publicPlaylists(
            @Argument Integer page,
            @Argument Integer size) {

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        Page<Playlist> playlists = playlistService.getPublicPlaylists(PageRequest.of(pageNum, pageSize));

        return PageResponse.<Playlist>builder()
                .content(playlists.getContent())
                .totalElements(playlists.getTotalElements())
                .totalPages(playlists.getTotalPages())
                .currentPage(playlists.getNumber())
                .size(playlists.getSize())
                .hasNext(playlists.hasNext())
                .hasPrevious(playlists.hasPrevious())
                .build();
    }

    @QueryMapping
    public PageResponse<Playlist> searchPublicPlaylists(
            @Argument String query,
            @Argument Integer page,
            @Argument Integer size) {

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        Page<Playlist> playlists = playlistService.searchPublicPlaylists(
                query, PageRequest.of(pageNum, pageSize));

        return PageResponse.<Playlist>builder()
                .content(playlists.getContent())
                .totalElements(playlists.getTotalElements())
                .totalPages(playlists.getTotalPages())
                .currentPage(playlists.getNumber())
                .size(playlists.getSize())
                .hasNext(playlists.hasNext())
                .hasPrevious(playlists.hasPrevious())
                .build();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Playlist playlistById(@Argument Long id, @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        return playlistService.getPlaylistById(id, currentUser.getId());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<PlaylistSong> playlistSongs(@Argument Long playlistId, @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        return playlistSongService.getPlaylistSongs(playlistId, currentUser.getId());
    }

    // ========== MUTATIONS ==========

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Playlist createPlaylist(
            @Argument String title,
            @Argument String description,
            @Argument Boolean isPublic,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        boolean publicFlag = isPublic != null ? isPublic : false;

        return playlistService.createPlaylist(currentUser.getId(), title, description, null, publicFlag);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Playlist updatePlaylist(
            @Argument Long id,
            @Argument String title,
            @Argument String description,
            @Argument Boolean isPublic,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        return playlistService.updatePlaylist(id, currentUser.getId(), title, description, null, isPublic);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deletePlaylist(@Argument Long id, @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        playlistService.deletePlaylist(id, currentUser.getId());
        return true;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public PlaylistSong addSongToPlaylist(
            @Argument Long playlistId,
            @Argument Long songId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        return playlistSongService.addSongToPlaylist(playlistId, currentUser.getId(), songId);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean removeSongFromPlaylist(
            @Argument Long playlistId,
            @Argument Long songId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        playlistSongService.removeSongFromPlaylist(playlistId, currentUser.getId(), songId);
        return true;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean reorderPlaylistSongs(
            @Argument Long playlistId,
            @Argument List<Long> songIds,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User currentUser = userService.findUserByEmail(userDetails.getUsername());
        playlistSongService.reorderPlaylistSongs(playlistId, currentUser.getId(), songIds);
        return true;
    }
}