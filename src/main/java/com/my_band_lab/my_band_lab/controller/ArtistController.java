package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.CreateArtistForCurrentUserRequest;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.service.ArtistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private static final Logger log = LoggerFactory.getLogger(ArtistController.class);

    @Autowired
    private ArtistService artistService;

    @PostMapping("/create")
    public Artist createArtist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateArtistForCurrentUserRequest request) throws Exception {

        log.info("=== CREATE ARTIST REQUEST ===");
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "null");
        log.info("Request: stageName={}, genre={}, instrumentIds={}",
                request.getStageName(), request.getGenre(), request.getInstrumentIds());

        if (userDetails == null) {
            throw new Exception("User not authenticated");
        }

        Artist artist = artistService.createArtistForCurrentUser(
                request.getStageName(),
                request.getBiography(),
                request.getGenre(),
                request.getInstrumentIds(),
                request.getMainInstrumentId()
        );

        log.info("Artist created: id={}, stageName={}", artist.getId(), artist.getStageName());

        return artist;
    }
}