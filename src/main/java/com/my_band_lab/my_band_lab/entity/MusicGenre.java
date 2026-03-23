package com.my_band_lab.my_band_lab.entity;

public enum MusicGenre {
    ROCK("Rock", "Rock music, characterized by guitar, bass, and drums"),
    POP("Pop", "Popular music with catchy melodies"),
    JAZZ("Jazz", "Improvisational music with complex harmonies"),
    CLASSICAL("Classical", "Orchestral and instrumental music"),
    HIP_HOP("Hip Hop", "Rhythmic music with rapping"),
    ELECTRONIC("Electronic", "Music produced with electronic instruments"),
    REGGAE("Reggae", "Jamaican music with offbeat rhythms"),
    BLUES("Blues", "Soulful music with blue notes"),
    COUNTRY("Country", "American folk music with guitars and fiddles"),
    METAL("Metal", "Heavy music with distorted guitars"),
    PUNK("Punk", "Fast and aggressive rock music"),
    SOUL("Soul", "Music combining gospel and rhythm and blues"),
    FUNK("Funk", "Groovy music with strong bass lines"),
    LATIN("Latin", "Music from Latin America and Spain"),
    INDIE("Indie", "Independent alternative music"),
    ALTERNATIVE("Alternative", "Independent music");

    private final String displayName;
    private final String description;
    MusicGenre(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;

    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
