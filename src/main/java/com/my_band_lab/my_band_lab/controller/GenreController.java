package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @GetMapping
    public List<MusicGenre> getAllGenres() {
        return genreService.getAllGenres();
    }

    @GetMapping("/map")
    public Map<String, String> getGenreMap() {
        return genreService.getGenreMap();
    }
}