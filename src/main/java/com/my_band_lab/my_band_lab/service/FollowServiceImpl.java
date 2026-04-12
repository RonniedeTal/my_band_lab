package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.FollowResponse;
import com.my_band_lab.my_band_lab.dto.FavoriteResponse;
import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.*;
import com.my_band_lab.my_band_lab.repository.ArtistRepository;
import com.my_band_lab.my_band_lab.repository.FavoriteRepository;
import com.my_band_lab.my_band_lab.repository.FollowRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.my_band_lab.my_band_lab.entity.GroupFollow;
import com.my_band_lab.my_band_lab.entity.GroupFavorite;
import com.my_band_lab.my_band_lab.repository.GroupFollowRepository;
import com.my_band_lab.my_band_lab.repository.GroupFavoriteRepository;
import com.my_band_lab.my_band_lab.repository.MusicGroupRepository;

@Service
@Transactional
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private GroupFollowRepository groupFollowRepository;

    @Autowired
    private GroupFavoriteRepository groupFavoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private MusicGroupRepository musicGroupRepository;

    @Override
    public FollowResponse followArtist(Long artistId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new Exception("Artist not found"));

        if (followRepository.existsByUserAndArtist(user, artist)) {
            return FollowResponse.builder()
                    .success(false)
                    .message("Already following this artist")
                    .followersCount(getFollowersCount(artistId))
                    .build();
        }

        Follow follow = Follow.builder()
                .user(user)
                .artist(artist)
                .build();

        followRepository.save(follow);

        return FollowResponse.builder()
                .success(true)
                .message("Now following " + artist.getStageName())
                .followersCount(getFollowersCount(artistId))
                .build();
    }

    @Override
    public FollowResponse unfollowArtist(Long artistId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new Exception("Artist not found"));

        if (!followRepository.existsByUserAndArtist(user, artist)) {
            return FollowResponse.builder()
                    .success(false)
                    .message("Not following this artist")
                    .followersCount(getFollowersCount(artistId))
                    .build();
        }

        followRepository.deleteByUserAndArtist(user, artist);

        return FollowResponse.builder()
                .success(true)
                .message("Unfollowed " + artist.getStageName())
                .followersCount(getFollowersCount(artistId))
                .build();
    }

    @Override
    public boolean isFollowingArtist(Long artistId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new Exception("Artist not found"));

        return followRepository.existsByUserAndArtist(user, artist);
    }

    @Override
    public PageResponse<Artist> getFollowedArtists(Long userId, Pageable pageable) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Page<Artist> artistsPage = followRepository.findFollowedArtistsByUser(user, pageable);

        return PageResponse.<Artist>builder()
                .content(artistsPage.getContent())
                .totalElements(artistsPage.getTotalElements())
                .totalPages(artistsPage.getTotalPages())
                .currentPage(artistsPage.getNumber())
                .size(artistsPage.getSize())
                .hasNext(artistsPage.hasNext())
                .hasPrevious(artistsPage.hasPrevious())
                .build();
    }

    @Override
    public int getFollowersCount(Long artistId) throws Exception {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new Exception("Artist not found"));

        return (int) followRepository.countFollowersByArtist(artist);
    }

    // Favorite methods
    @Override
    public FavoriteResponse addFavoriteArtist(Long artistId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new Exception("Artist not found"));

        if (favoriteRepository.existsByUserAndArtist(user, artist)) {
            return FavoriteResponse.builder()
                    .success(false)
                    .message("Artist already in favorites")
                    .build();
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .artist(artist)
                .build();

        favoriteRepository.save(favorite);

        return FavoriteResponse.builder()
                .success(true)
                .message(artist.getStageName() + " added to favorites")
                .build();
    }

    @Override
    public FavoriteResponse removeFavoriteArtist(Long artistId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new Exception("Artist not found"));

        if (!favoriteRepository.existsByUserAndArtist(user, artist)) {
            return FavoriteResponse.builder()
                    .success(false)
                    .message("Artist not in favorites")
                    .build();
        }

        favoriteRepository.deleteByUserAndArtist(user, artist);

        return FavoriteResponse.builder()
                .success(true)
                .message(artist.getStageName() + " removed from favorites")
                .build();
    }

    @Override
    public boolean isFavoriteArtist(Long artistId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new Exception("Artist not found"));

        return favoriteRepository.existsByUserAndArtist(user, artist);
    }

    @Override
    public PageResponse<Artist> getFavoriteArtists(Long userId, Pageable pageable) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Page<Artist> artistsPage = favoriteRepository.findFavoriteArtistsByUser(user, pageable);

        return PageResponse.<Artist>builder()
                .content(artistsPage.getContent())
                .totalElements(artistsPage.getTotalElements())
                .totalPages(artistsPage.getTotalPages())
                .currentPage(artistsPage.getNumber())
                .size(artistsPage.getSize())
                .hasNext(artistsPage.hasNext())
                .hasPrevious(artistsPage.hasPrevious())
                .build();
    }

    // ============ NUEVOS MÉTODOS PARA GRUPOS ============

    @Override
    public FollowResponse followGroup(Long groupId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        if (groupFollowRepository.existsByUserAndGroup(user, group)) {
            return FollowResponse.builder()
                    .success(false)
                    .message("Already following this group")
                    .followersCount(getGroupFollowersCount(groupId))
                    .build();
        }

        GroupFollow follow = GroupFollow.builder()
                .user(user)
                .group(group)
                .build();

        groupFollowRepository.save(follow);

        return FollowResponse.builder()
                .success(true)
                .message("Now following " + group.getName())
                .followersCount(getGroupFollowersCount(groupId))
                .build();
    }

    @Override
    public FollowResponse unfollowGroup(Long groupId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        if (!groupFollowRepository.existsByUserAndGroup(user, group)) {
            return FollowResponse.builder()
                    .success(false)
                    .message("Not following this group")
                    .followersCount(getGroupFollowersCount(groupId))
                    .build();
        }

        groupFollowRepository.deleteByUserAndGroup(user, group);

        return FollowResponse.builder()
                .success(true)
                .message("Unfollowed " + group.getName())
                .followersCount(getGroupFollowersCount(groupId))
                .build();
    }

    @Override
    public boolean isFollowingGroup(Long groupId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        return groupFollowRepository.existsByUserAndGroup(user, group);
    }

    @Override
    public PageResponse<MusicGroup> getFollowedGroups(Long userId, Pageable pageable) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Page<MusicGroup> groupsPage = groupFollowRepository.findFollowedGroupsByUser(user, pageable);

        return PageResponse.<MusicGroup>builder()
                .content(groupsPage.getContent())
                .totalElements(groupsPage.getTotalElements())
                .totalPages(groupsPage.getTotalPages())
                .currentPage(groupsPage.getNumber())
                .size(groupsPage.getSize())
                .hasNext(groupsPage.hasNext())
                .hasPrevious(groupsPage.hasPrevious())
                .build();
    }

    @Override
    public int getGroupFollowersCount(Long groupId) throws Exception {
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        return (int) groupFollowRepository.countByGroup(group);
    }

    @Override
    public FavoriteResponse addFavoriteGroup(Long groupId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        if (groupFavoriteRepository.existsByUserAndGroup(user, group)) {
            return FavoriteResponse.builder()
                    .success(false)
                    .message("Group already in favorites")
                    .build();
        }

        GroupFavorite favorite = GroupFavorite.builder()
                .user(user)
                .group(group)
                .build();

        groupFavoriteRepository.save(favorite);

        return FavoriteResponse.builder()
                .success(true)
                .message(group.getName() + " added to favorites")
                .build();
    }

    @Override
    public FavoriteResponse removeFavoriteGroup(Long groupId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        if (!groupFavoriteRepository.existsByUserAndGroup(user, group)) {
            return FavoriteResponse.builder()
                    .success(false)
                    .message("Group not in favorites")
                    .build();
        }

        groupFavoriteRepository.deleteByUserAndGroup(user, group);

        return FavoriteResponse.builder()
                .success(true)
                .message(group.getName() + " removed from favorites")
                .build();
    }

    @Override
    public boolean isFavoriteGroup(Long groupId, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        return groupFavoriteRepository.existsByUserAndGroup(user, group);
    }

    @Override
    public PageResponse<MusicGroup> getFavoriteGroups(Long userId, Pageable pageable) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        Page<MusicGroup> groupsPage = groupFavoriteRepository.findFavoriteGroupsByUser(user, pageable);

        return PageResponse.<MusicGroup>builder()
                .content(groupsPage.getContent())
                .totalElements(groupsPage.getTotalElements())
                .totalPages(groupsPage.getTotalPages())
                .currentPage(groupsPage.getNumber())
                .size(groupsPage.getSize())
                .hasNext(groupsPage.hasNext())
                .hasPrevious(groupsPage.hasPrevious())
                .build();
    }
}