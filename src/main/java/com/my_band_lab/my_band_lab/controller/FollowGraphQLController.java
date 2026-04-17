package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.FollowResponse;
import com.my_band_lab.my_band_lab.dto.FavoriteResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import com.my_band_lab.my_band_lab.service.ArtistService;
import com.my_band_lab.my_band_lab.service.FollowService;
import com.my_band_lab.my_band_lab.service.MusicGroupService;
import com.my_band_lab.my_band_lab.service.NotificationService;
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

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private MusicGroupService musicGroupService;

    private Long getCurrentUserId() throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("User not found"));
        return user.getId();
    }
    private User getCurrentUser() throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("User not found"));
    }

    // Follow artist
    @MutationMapping
    public FollowResponse followArtist(@Argument Long artistId) throws Exception {
        Long userId = getCurrentUserId();
        User currentUser = getCurrentUser();
        FollowResponse response = followService.followArtist(artistId, userId);
        if (response.isSuccess()) {
            Artist artist = artistService.getArtistById(artistId);
            String followerName = currentUser.getName() + " " + currentUser.getSurname();

            notificationService.createNotification(
                    artist.getUser().getId(),
                    "Nuevo seguidor",
                    followerName + " ha comenzado a seguirte",
                    "FOLLOW",
                    artistId
            );

//            notificationService.sendPushNotification(
//                    artist.getUser().getId(),
//                    "Nuevo seguidor",
//                    followerName + " ha comenzado a seguirte",
//                    "/artists/" + artistId,
//                    null
//            );
        }

        return response;
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

    // Favorite Artist
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
        User currentUser = getCurrentUser();
        FollowResponse response = followService.followGroup(groupId, userId);

        if (response.isSuccess()) {
            MusicGroup group = musicGroupService.getGroupById(groupId);
            String followerName = currentUser.getName() + " " + currentUser.getSurname();

            notificationService.createNotification(
                    group.getFounder().getId(),
                    "Nuevo seguidor",
                    followerName + " ha comenzado a seguir tu grupo " + group.getName(),
                    "FOLLOW",
                    groupId
            );

//            notificationService.sendPushNotification(
//                    group.getFounder().getId(),
//                    "Nuevo seguidor",
//                    followerName + " ha comenzado a seguir " + group.getName(),
//                    "/groups/" + groupId,
//                    null
//            );
        }

        return response;
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

    // ============ FAVORITE GROUP ============
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