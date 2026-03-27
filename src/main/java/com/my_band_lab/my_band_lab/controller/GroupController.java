package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.service.MusicGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private MusicGroupService musicGroupService;

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

        return musicGroupService.addMember(groupId, userId);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public MusicGroup removeMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long userId) throws Exception {

        if (userDetails == null) {
            throw new Exception("User not authenticated");
        }

        return musicGroupService.removeMember(groupId, userId);
    }
}