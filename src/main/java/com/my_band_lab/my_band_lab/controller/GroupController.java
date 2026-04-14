package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.dto.ImageUploadResponse;
import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.ImageUploadService;
import com.my_band_lab.my_band_lab.service.MusicGroupService;
import com.my_band_lab.my_band_lab.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private MusicGroupService musicGroupService;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private UserService userService;

    // GET /api/groups - Listar todos los grupos (protegido)
    @GetMapping
    public List<MusicGroup> getAllGroups() throws Exception {
        return musicGroupService.getAllGroups();
    }

    // GET /api/groups/{id} - Obtener grupo por ID
    @GetMapping("/{id}")
    public MusicGroup getGroupById(@PathVariable Long id) throws Exception {
        return musicGroupService.getGroupById(id);
    }

    @PostMapping("/create")
    public MusicGroup createGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam MusicGenre genre) throws Exception {

        if (userDetails == null) {
            throw new Exception("User not authenticated");
        }

        // Pasamos null como founderId, el servicio obtendrá el usuario autenticado
        return musicGroupService.createGroup(name, description, genre, null);
    }

    @PostMapping("/{groupId}/members/{userId}")
    public MusicGroup addMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long userId) throws Exception {

        if (userDetails == null) {
            throw new Exception("User not authenticated");
        }

        try {
            return musicGroupService.addMember(groupId, userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public MusicGroup removeMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long userId) throws Exception {

        if (userDetails == null) {
            throw new Exception("User not authenticated");
        }

        try {
            return musicGroupService.removeMember(groupId, userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
    //logo grupo===============================
    @PostMapping("/{groupId}/logo")
    public ResponseEntity<ImageUploadResponse> uploadGroupLogo(
            @PathVariable Long groupId,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        MusicGroup group = musicGroupService.getGroupById(groupId);
        User currentUser = userService.findUserByEmail(userDetails.getUsername());

        // Verificar que el usuario es el fundador del grupo
        if (!group.getFounder().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("No tienes permiso para modificar este grupo")
                            .build());
        }

        try {
            String imageUrl = imageUploadService.uploadImage(file, "groups");
            group.setLogoUrl(imageUrl);
            musicGroupService.save(group);

            return ResponseEntity.ok(ImageUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .message("Logo del grupo subido exitosamente")
                    .success(true)
                    .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.builder()
                            .success(false)
                            .message("Error al subir el logo: " + e.getMessage())
                            .build());
        }
    }
}