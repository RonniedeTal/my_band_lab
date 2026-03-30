package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private ArtistService artistService;

    // GET /api/admin/artists/unverified - Listar artistas no verificados
    @GetMapping("/artists/unverified")
    public List<Artist> getUnverifiedArtists() throws Exception {
        return artistService.getUnverifiedArtists();
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