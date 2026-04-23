// src/main/java/com/my_band_lab/my_band_lab/controller/LookingForBandController.java

package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.entity.Artist;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.ArtistService;
import com.my_band_lab.my_band_lab.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/looking-for-band")
public class LookingForBandController {

    private static final Logger log = LoggerFactory.getLogger(LookingForBandController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ArtistService artistService;

    /**
     * PUT /api/looking-for-band/status
     * Activar/desactivar la opción "Quiero formar banda"
     */
    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestBody Map<String, Boolean> request) {

        Boolean isLookingForBand = request.get("isLookingForBand");
        log.info("PUT /api/looking-for-band/status - isLookingForBand: {}", isLookingForBand);

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !(auth.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no autenticado"));
            }

            String email = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userService.findUserByEmail(email);

            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            Artist artist = artistService.getArtistByUserId(user.getId());

            if (artist == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tienes un perfil de artista"));
            }

            artist.setLookingForBand(isLookingForBand);
            Artist saved = artistService.save(artist);

            String message = saved.isLookingForBand()
                    ? "✅ Ahora aparecerás en el directorio de músicos disponibles"
                    : "❌ Ya no aparecerás en el directorio";

            return ResponseEntity.ok(Map.of(
                    "isLookingForBand", saved.isLookingForBand(),
                    "message", message
            ));

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/looking-for-band/status
     * Obtener el estado actual
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {

        log.info("GET /api/looking-for-band/status");

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !(auth.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.ok(Map.of("isLookingForBand", false));
            }

            String email = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userService.findUserByEmail(email);

            if (user == null) {
                return ResponseEntity.ok(Map.of("isLookingForBand", false));
            }

            Artist artist = artistService.getArtistByUserId(user.getId());

            if (artist == null) {
                return ResponseEntity.ok(Map.of("isLookingForBand", false));
            }

            return ResponseEntity.ok(Map.of(
                    "isLookingForBand", artist.isLookingForBand()
            ));

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("isLookingForBand", false));
        }
    }

}
