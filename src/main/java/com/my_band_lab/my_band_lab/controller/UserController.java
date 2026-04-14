package com.my_band_lab.my_band_lab.controller;


import com.my_band_lab.my_band_lab.dto.UpdateProfileImageRequest;
import com.my_band_lab.my_band_lab.dto.UpdateProfileRequest;
import com.my_band_lab.my_band_lab.dto.UserProfileResponse;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.MusicGroupService;
import com.my_band_lab.my_band_lab.service.UserService;
import com.my_band_lab.my_band_lab.service.ImageUploadService;
import com.my_band_lab.my_band_lab.service.ArtistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private ArtistService artistService;


    @PostMapping("/saveUser")
    public User saveUser(@Valid @RequestBody User user) {return userService.saveUser(user);}

    @GetMapping("/user/id/{id}")
    User findUserById(@PathVariable Long id) throws Exception{
        return userService.findUserById(id);
    }

    @GetMapping("/user/name/{name}")
    User findUserByName(@PathVariable String name) throws Exception{
        return userService.findUserByName(name);
    }

    @GetMapping("/user/surname/{surname}")
    User findUserBySurname(@PathVariable String surname) throws Exception{
        return userService.findUserBySurname(surname);
    }

    @GetMapping("/user/fullname")
    User findUserByFullName(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String fullName) throws Exception {

        // Si viene fullName, lo dividimos
        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.split(" ");
            if (parts.length >= 2) {
                name = parts[0];
                surname = parts[1];
            }
        }

        // Validar que tenemos ambos campos
        if (name == null || surname == null || name.isEmpty() || surname.isEmpty()) {
            throw new Exception("Please provide name and surname");
        }

        return userService.findUserByNameAndSurname(name, surname);
    }


    @PutMapping("/user/update/{id}")
    User updateUser(@Valid @RequestBody User user, @PathVariable Long id) throws Exception {
        return userService.updateUser(id, user);
    }

    @PatchMapping("/user/update/{id}")
    User patchUser(@Valid @RequestBody User user, @PathVariable Long id) throws Exception {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/user/delete/{id}")
    String deleteUser(@PathVariable Long id) throws Exception {
        userService.deleteUser(id);
        return "User with id " + id + " deleted. " ;
    }
    @GetMapping("/api/me")
    public UserProfileResponse getCurrentUserProfile() throws Exception {
        return userService.getCurrentUserProfile();
    }

    @PutMapping("/api/me")
    public UserProfileResponse updateCurrentUserProfile(@Valid @RequestBody UpdateProfileRequest request) throws Exception {
        return userService.updateCurrentUserProfile(request);
    }

    @PutMapping("/api/me/profile-image")
    public User updateProfileImage(
            @RequestBody UpdateProfileImageRequest request) throws Exception {
        return userService.updateProfileImage(request.getProfileImageUrl());
    }
// ========== ENDPOINTS PARA SUBIR IMAGENES ==========

    @PostMapping("/api/upload/profile-image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User user = userService.findUserByEmail(userDetails.getUsername());
        String imageUrl = imageUploadService.uploadImage(file, "profiles");
        user.setProfileImageUrl(imageUrl);
        userService.saveUser(user);

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        return ResponseEntity.ok(response);
    }
}
