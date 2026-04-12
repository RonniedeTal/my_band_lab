package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.FollowResponse;
import com.my_band_lab.my_band_lab.dto.FavoriteResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("User not found"));
        return user.getId();
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
        // Si no se proporciona userId, usar el del usuario autenticado
        if (userId == null) {
            userId = getCurrentUserId();
        }
        return followService.isFollowingArtist(artistId, userId);
    }

    @QueryMapping
    public PageResponse<Artist> followedArtists(@Argument Long userId, @Argument int page, @Argument int size) throws Exception {
        if (userId == null) {
            userId = getCurrentUserId();
        }
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
        if (userId == null) {
            userId = getCurrentUserId();
        }
        return followService.isFavoriteArtist(artistId, userId);
    }

    @QueryMapping
    public PageResponse<Artist> favoriteArtists(@Argument Long userId, @Argument int page, @Argument int size) throws Exception {
        if (userId == null) {
            userId = getCurrentUserId();
        }
        Pageable pageable = PageRequest.of(page, size);
        return followService.getFavoriteArtists(userId, pageable);
    }

    // ============ GRUPOS ============

    @MutationMapping
    public FollowResponse followGroup(@Argument Long groupId) throws Exception {
        Long userId = getCurrentUserId();
        return followService.followGroup(groupId, userId);
    }

    @MutationMapping
    public FollowResponse unfollowGroup(@Argument Long groupId) throws Exception {
        Long userId = getCurrentUserId();
        return followService.unfollowGroup(groupId, userId);
    }

    @QueryMapping
    public boolean isFollowingGroup(@Argument Long userId, @Argument Long groupId) throws Exception {
        if (userId == null) {
            userId = getCurrentUserId();
        }
        return followService.isFollowingGroup(groupId, userId);
    }

    @QueryMapping
    public PageResponse<MusicGroup> followedGroups(@Argument Long userId, @Argument int page, @Argument int size) throws Exception {
        if (userId == null) {
            userId = getCurrentUserId();
        }
        Pageable pageable = PageRequest.of(page, size);
        return followService.getFollowedGroups(userId, pageable);
    }

    @MutationMapping
    public FavoriteResponse addFavoriteGroup(@Argument Long groupId) throws Exception {
        Long userId = getCurrentUserId();
        return followService.addFavoriteGroup(groupId, userId);
    }

    @MutationMapping
    public FavoriteResponse removeFavoriteGroup(@Argument Long groupId) throws Exception {
        Long userId = getCurrentUserId();
        return followService.removeFavoriteGroup(groupId, userId);
    }

    @QueryMapping
    public boolean isFavoriteGroup(@Argument Long userId, @Argument Long groupId) throws Exception {
        if (userId == null) {
            userId = getCurrentUserId();
        }
        return followService.isFavoriteGroup(groupId, userId);
    }

    @QueryMapping
    public PageResponse<MusicGroup> favoriteGroups(@Argument Long userId, @Argument int page, @Argument int size) throws Exception {
        if (userId == null) {
            userId = getCurrentUserId();
        }
        Pageable pageable = PageRequest.of(page, size);
        return followService.getFavoriteGroups(userId, pageable);
    }
}