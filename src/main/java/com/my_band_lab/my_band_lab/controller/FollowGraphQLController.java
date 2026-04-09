package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.FollowResponse;
import com.my_band_lab.my_band_lab.dto.FavoriteResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.security.CustomUserDetailsService;
import com.my_band_lab.my_band_lab.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public class FollowGraphQLController {

    @Autowired
    private FollowService followService;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return 1L; // Temporal, ajusta según tu lógica
    }

    // Follow mutations
    @MutationMapping
    public FollowResponse followArtist(@Argument Long artistId) throws Exception {
        Long userId = getCurrentUserId();
        return followService.followArtist(artistId, userId);
    }

    @MutationMapping
    public FollowResponse unfollowArtist(@Argument Long artistId) throws Exception {
        Long userId = getCurrentUserId();
        return followService.unfollowArtist(artistId, userId);
    }

    // Follow queries
    @QueryMapping
    public boolean isFollowingArtist(@Argument Long userId, @Argument Long artistId) throws Exception {
        return followService.isFollowingArtist(artistId, userId);
    }

    @QueryMapping
    public PageResponse<Artist> followedArtists(@Argument Long userId, @Argument int page, @Argument int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        return followService.getFollowedArtists(userId, pageable);
    }

    // Favorite mutations
    @MutationMapping
    public FavoriteResponse addFavoriteArtist(@Argument Long artistId) throws Exception {
        Long userId = getCurrentUserId();
        return followService.addFavoriteArtist(artistId, userId);
    }

    @MutationMapping
    public FavoriteResponse removeFavoriteArtist(@Argument Long artistId) throws Exception {
        Long userId = getCurrentUserId();
        return followService.removeFavoriteArtist(artistId, userId);
    }

    // Favorite queries
    @QueryMapping
    public boolean isFavoriteArtist(@Argument Long userId, @Argument Long artistId) throws Exception {
        return followService.isFavoriteArtist(artistId, userId);
    }

    @QueryMapping
    public PageResponse<Artist> favoriteArtists(@Argument Long userId, @Argument int page, @Argument int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        return followService.getFavoriteArtists(userId, pageable);
    }
}