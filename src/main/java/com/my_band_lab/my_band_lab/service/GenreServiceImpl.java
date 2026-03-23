package com.my_band_lab.my_band_lab.service;


import com.my_band_lab.my_band_lab.entity.MusicGenre;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class GenreServiceImpl implements GenreService {

    @Override
    public List<MusicGenre>getAllGenres() {
        return Arrays.asList(MusicGenre.values());
    }

    @Override
    public MusicGenre getGenreByName(String name) {
        try {
            return MusicGenre.valueOf(name.toUpperCase());
        }catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid genre name" + name + "Available genres: " + getAvailableGenres());
        }
    }

    @Override
    public MusicGenre getGenreByDisplayName(String displayName) {
        return Arrays.stream(MusicGenre.values())
                .filter(genre -> genre.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid genre: " + displayName +
                        ". Available genres: " + getAvailableGenres()));
    }

    @Override
    public Map<String, String> getGenreMap() {
        return Arrays.stream(MusicGenre.values())
                .collect(Collectors.toMap(
                        genre -> genre.name(),
                        MusicGenre::getDisplayName
                ));
    }

    @Override
    public boolean isValidGenre(String genreName) {
        return Arrays.stream(MusicGenre.values())
                .anyMatch(genre -> genre.name().equalsIgnoreCase(genreName) ||
                        genre.getDisplayName().equalsIgnoreCase(genreName));
    }

    private String getAvailableGenres() {
        return Arrays.stream(MusicGenre.values())
                .map(genre -> genre.getDisplayName())
                .collect(Collectors.joining(", "));
    }

}
