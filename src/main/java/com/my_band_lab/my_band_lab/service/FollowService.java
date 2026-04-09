package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.FollowResponse;
import com.my_band_lab.my_band_lab.dto.FavoriteResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import org.springframework.data.domain.Pageable;

public interface FollowService {

    // Follow
    FollowResponse followArtist(Long artistId, Long userId) throws Exception;
    FollowResponse unfollowArtist(Long artistId, Long userId) throws Exception;
    boolean isFollowingArtist(Long artistId, Long userId) throws Exception;
    PageResponse<Artist> getFollowedArtists(Long userId, Pageable pageable) throws Exception;
    int getFollowersCount(Long artistId) throws Exception;

    // Favorite
    FavoriteResponse addFavoriteArtist(Long artistId, Long userId) throws Exception;
    FavoriteResponse removeFavoriteArtist(Long artistId, Long userId) throws Exception;
    boolean isFavoriteArtist(Long artistId, Long userId) throws Exception;
    PageResponse<Artist> getFavoriteArtists(Long userId, Pageable pageable) throws Exception;
}