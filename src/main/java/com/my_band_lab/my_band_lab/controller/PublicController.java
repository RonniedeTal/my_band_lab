package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.service.ArtistService;
import com.my_band_lab.my_band_lab.service.MusicGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private MusicGroupService musicGroupService;

    // GET /api/public/artists - Lista pública de artistas
    @GetMapping("/artists")
    public List<Artist> getAllArtists() throws Exception {
        return artistService.getAllArtists();
    }

    // GET /api/public/artists/{id} - Detalle público de un artista
    @GetMapping("/artists/{id}")
    public Artist getArtistById(@PathVariable Long id) throws Exception {
        return artistService.getArtistById(id);
    }

    @GetMapping("/artists/paginated")
    public PageResponse<Artist> getArtistsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        return artistService.getAllArtistsPaginated(page, size);
    }

    @GetMapping("/groups")
    public List<MusicGroup> getAllGroups() throws Exception {
        return musicGroupService.getAllGroups();
    }

    @GetMapping("/groups/{id}")
    public MusicGroup getGroupById(@PathVariable Long id) throws Exception {
        return musicGroupService.getGroupById(id);
    }

    //Grupos con paginación
    @GetMapping("/groups/paginated")
    public PageResponse<MusicGroup> getGroupsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        return musicGroupService.getAllGroupsPaginated(page, size);
    }

    //Search endpoints
    @GetMapping("/search/artists")
    public PageResponse<Artist> searchArtists(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) MusicGenre genre) throws Exception {
        return artistService.searchArtists(q, page, size, country, city, genre);
    }

    @GetMapping("/search/groups")
    public PageResponse<MusicGroup> searchGroups(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) MusicGenre genre) throws Exception {
        return musicGroupService.searchGroups(q, page, size, country, city, genre);
    }
}