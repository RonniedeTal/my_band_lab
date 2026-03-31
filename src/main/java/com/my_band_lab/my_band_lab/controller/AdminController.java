package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.dto.UserAdminResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.service.ArtistService;
import com.my_band_lab.my_band_lab.service.MusicGroupService;
import com.my_band_lab.my_band_lab.service.UserService;
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
    @Autowired
    private MusicGroupService musicGroupService;
    @Autowired
    private UserService userService;

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

    // ==================== GRUPOS ====================

    @GetMapping("/groups/unverified")
    public Map<String, Object> getUnverifiedGroups() throws Exception {
        List<MusicGroup> groups = musicGroupService.getUnverifiedGroups();
        Map<String, Object> response = new HashMap<>();

        if (groups.isEmpty()) {
            response.put("message", "No unverified groups found");
            response.put("count", 0);
            response.put("groups", new ArrayList<>());
        } else {
            response.put("message", "Unverified groups found");
            response.put("count", groups.size());
            response.put("groups", groups);
        }

        return response;
    }

    @GetMapping("/groups/unverified/paginated")
    public PageResponse<MusicGroup> getUnverifiedGroupsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        return musicGroupService.getUnverifiedGroupsPaginated(page, size);
    }

    @PutMapping("/groups/{id}/verify")
    public MusicGroup verifyGroup(@PathVariable Long id) throws Exception {
        return musicGroupService.verifyGroup(id);
    }
    // ==================== USUARIOS ====================

    @GetMapping("/users")
    public PageResponse<UserAdminResponse> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {

        if (role != null && !role.isEmpty()) {
            return userService.getUsersByRoleForAdminPaginated(role, page, size);
        }
        return userService.getAllUsersForAdminPaginated(page, size);
    }
}