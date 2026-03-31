package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.dto.RoleChangeRequest;
import com.my_band_lab.my_band_lab.dto.UserAdminResponse;
import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.ArtistService;
import com.my_band_lab.my_band_lab.service.MusicGroupService;
import com.my_band_lab.my_band_lab.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserAdminResponse user = userService.getUserByIdForAdmin(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found with id: " + id));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleChangeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        // Obtener el ID del admin actual
        String email = userDetails.getUsername();
        User currentAdmin = userService.getCurrentUser();

        try {
            UserAdminResponse updatedUser = userService.changeUserRole(id, request.getRole(), currentAdmin.getId());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            if (e.getMessage().equals("You cannot change your own role")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", e.getMessage()));
            }
            if (e.getMessage().contains("Invalid role")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", e.getMessage(), "valid_roles", List.of("USER", "ARTIST", "ADMIN")));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        // Obtener el ID del admin actual
        User currentAdmin = userService.getCurrentUser();

        try {
            userService.deleteUserByAdmin(id, currentAdmin.getId());
            return ResponseEntity.ok(Map.of(
                    "message", "User with id " + id + " has been deleted successfully",
                    "deletedUserId", id
            ));
        } catch (Exception e) {
            if (e.getMessage().equals("You cannot delete your own account")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", e.getMessage()));
            }
            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found with id: " + id));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}