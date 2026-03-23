package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.MusicGenre;

import java.util.List;
import java.util.Map;

public interface GenreService {
    List<MusicGenre> getAllGenres();
    MusicGenre getGenreByName(String name);
    MusicGenre getGenreByDisplayName(String displayName);
    Map<String,String> getGenreMap();
    boolean isValidGenre(String genreName);
}
