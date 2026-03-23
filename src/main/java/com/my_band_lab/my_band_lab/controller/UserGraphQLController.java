package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.entity.*;
import com.my_band_lab.my_band_lab.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;

@Controller
public class UserGraphQLController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private MusicGroupService musicGroupService;

    @Autowired
    private GenreService genreService;

    // User Queries
    @QueryMapping
    public User userById(@Argument Long id) throws Exception {
        return userService.findUserById(id);
    }

    @QueryMapping
    public User userByName(@Argument String name) throws Exception {
        return userService.findUserByName(name);
    }

    @QueryMapping
    public User userBySurname(@Argument String surname) throws Exception {
        return userService.findUserBySurname(surname);
    }

    @QueryMapping
    public User userByNameAndSurname(@Argument String name, @Argument String surname) throws Exception {
        return userService.findUserByNameAndSurname(name, surname);
    }

    @QueryMapping
    public List<User> users() throws Exception {
        return userService.findAllUsers();
    }

    // Artist Queries
    @QueryMapping
    public List<Artist> artists() throws Exception {
        return artistService.getAllArtists();
    }

    @QueryMapping
    public Artist artistById(@Argument Long id) throws Exception {
        return artistService.getArtistById(id);
    }

    @QueryMapping
    public Artist artistByUserId(@Argument Long userId) throws Exception {
        return artistService.getArtistByUserId(userId);
    }

    @QueryMapping
    public List<Artist> artistsByGenre(@Argument MusicGenre genre) throws Exception {
        return artistService.getArtistsByGenre(genre);
    }

    // MusicGroup Queries
    @QueryMapping
    public List<MusicGroup> musicGroups() throws Exception {
        return musicGroupService.getAllGroups();
    }

    @QueryMapping
    public MusicGroup musicGroupById(@Argument Long id) throws Exception {
        return musicGroupService.getGroupById(id);
    }

    @QueryMapping
    public List<MusicGroup> musicGroupsByGenre(@Argument MusicGenre genre) throws Exception {
        return musicGroupService.getGroupsByGenre(genre);
    }

    // Genre Queries
    @QueryMapping
    public List<MusicGenre> availableGenres() {
        return Arrays.asList(MusicGenre.values());
    }

    // User Mutations
    @MutationMapping
    public User createUser(@Argument String name, @Argument String surname,
                           @Argument String email, @Argument String password,
                           @Argument String profileImageUrl) throws Exception {
        User user = User.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .password(password)
                .profileImageUrl(profileImageUrl)
                .build();
        return userService.saveUser(user);
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument String name,
                           @Argument String surname, @Argument String email,
                           @Argument String password, @Argument String profileImageUrl) throws Exception {
        User userDetails = User.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .password(password)
                .profileImageUrl(profileImageUrl)
                .build();
        return userService.updateUser(id, userDetails);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) throws Exception {
        userService.deleteUser(id);
        return true;
    }

    // Artist Mutations
    @MutationMapping
    public Artist createArtist(@Argument Long userId, @Argument String stageName,
                               @Argument String biography, @Argument MusicGenre genre) throws Exception {
        return artistService.createArtist(userId, stageName, biography, genre);
    }

    @MutationMapping
    public Artist updateArtist(@Argument Long id, @Argument String stageName,
                               @Argument String biography, @Argument MusicGenre genre) throws Exception {
        return artistService.updateArtist(id, stageName, biography, genre);
    }

    @MutationMapping
    public Boolean deleteArtist(@Argument Long id) throws Exception {
        artistService.deleteArtist(id);
        return true;
    }

    // MusicGroup Mutations
    @MutationMapping
    public MusicGroup createMusicGroup(@Argument String name, @Argument String description,
                                       @Argument MusicGenre genre, @Argument Long leaderId) throws Exception {
        return musicGroupService.createGroup(name, description, genre, leaderId);
    }

    @MutationMapping
    public MusicGroup addMemberToGroup(@Argument Long groupId, @Argument Long userId) throws Exception {
        return musicGroupService.addMember(groupId, userId);
    }

    @MutationMapping
    public MusicGroup removeMemberFromGroup(@Argument Long groupId, @Argument Long userId) throws Exception {
        return musicGroupService.removeMember(groupId, userId);
    }

    @MutationMapping
    public MusicGroup updateMusicGroupGenre(@Argument Long groupId, @Argument MusicGenre genre) throws Exception {
        return musicGroupService.updateGroupGenre(groupId, genre);
    }

    @MutationMapping
    public Boolean deleteMusicGroup(@Argument Long id) throws Exception {
        musicGroupService.deleteGroup(id);
        return true;
    }
}