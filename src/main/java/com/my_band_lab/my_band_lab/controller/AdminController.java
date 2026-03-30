package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private ArtistService artistService;

    // GET /api/admin/artists/unverified - Listar artistas no verificados
    @GetMapping("/artists/unverified")
    public Map<String, Object> getUnverifiedArtists() throws Exception {
        List<Artist> artists = artistService.getUnverifiedArtists();
        Map<String, Object> response = new HashMap<>();

        if (artists.isEmpty()) {
            response.put("message", "No unverified artists found");
            response.put("count", 0);
            response.put("artists", new ArrayList<>());
        } else {
            response.put("message", "Unverified artists found");
            response.put("count", artists.size());
            response.put("artists", artists);
        }

        return response;
    }

    // GET /api/admin/artists/unverified/paginated - Listar con paginación
    @GetMapping("/artists/unverified/paginated")
    public PageResponse<Artist> getUnverifiedArtistsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        return artistService.getUnverifiedArtistsPaginated(page, size);
    }

    // PUT /api/admin/artists/{id}/verify - Verificar artista
    @PutMapping("/artists/{id}/verify")
    public Artist verifyArtist(@PathVariable Long id) throws Exception {
        return artistService.verifyArtist(id);
    }
}