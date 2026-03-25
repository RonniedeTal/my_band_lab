package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.entity.Artist;
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

    @GetMapping("/groups")
    public List<MusicGroup> getAllGroups() throws Exception {
        return musicGroupService.getAllGroups();
    }

    @GetMapping("/groups/{id}")
    public MusicGroup getGroupById(@PathVariable Long id) throws Exception {
        return musicGroupService.getGroupById(id);
    }
}