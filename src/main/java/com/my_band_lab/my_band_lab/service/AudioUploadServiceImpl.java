package com.my_band_lab.my_band_lab.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.my_band_lab.my_band_lab.service.AudioUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioUploadServiceImpl implements AudioUploadService {

    private final Cloudinary cloudinary;

    private static final int MAX_FILE_SIZE_MB = 10;
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

    @Override
    public String uploadMp3(MultipartFile file, String folder) throws IOException {
        log.info("=== SUBIENDO MP3 A CLOUDINARY ===");
        log.info("Nombre: {}", file.getOriginalFilename());
        log.info("Tamaño: {} bytes", file.getSize());
        log.info("Tipo: {}", file.getContentType());

        // Validar que no esté vacío
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        // Validar tipo de archivo (solo MP3)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("audio/mpeg")) {
            throw new IllegalArgumentException("Solo se permiten archivos MP3. Tipo recibido: " + contentType);
        }

        // Validar tamaño máximo (10MB)
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(String.format(
                    "El archivo no puede superar los %dMB. Tamaño actual: %.2fMB",
                    MAX_FILE_SIZE_MB, file.getSize() / (1024.0 * 1024.0)
            ));
        }

        // Configurar parámetros para Cloudinary
        Map<String, Object> params = ObjectUtils.asMap(
                "folder", "mybandlab/" + folder,
                "resource_type", "video", // Cloudinary usa "video" para audio
                "format", "mp3"
        );

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String audioUrl = (String) uploadResult.get("secure_url");
            log.info("✅ MP3 subido exitosamente: {}", audioUrl);
            return audioUrl;
        } catch (IOException e) {
            log.error("❌ Error al subir MP3 a Cloudinary", e);
            throw new IOException("Error al procesar el archivo MP3: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteMp3(String audioUrl) {
        try {
            if (audioUrl != null && !audioUrl.isEmpty()) {
                String publicId = extractPublicIdFromUrl(audioUrl);
                if (publicId != null) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
                    log.info("🗑️ MP3 eliminado: {}", publicId);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error al eliminar MP3 de Cloudinary: {}", e.getMessage());
        }
    }

    /**
     * Extrae el public_id de una URL de Cloudinary
     */
    private String extractPublicIdFromUrl(String audioUrl) {
        try {
            // Ejemplo URL: https://res.cloudinary.com/.../upload/v1234567890/mybandlab/songs/song123.mp3
            String[] parts = audioUrl.split("/");
            String filename = parts[parts.length - 1];
            String folderWithVersion = parts[parts.length - 2];

            // Remover versión si existe (v1234567890/)
            String folder = folderWithVersion.replaceAll("^v\\d+/", "");

            // Remover extensión del archivo
            String publicIdWithoutExt = filename.contains(".")
                    ? filename.substring(0, filename.lastIndexOf('.'))
                    : filename;

            return "mybandlab/" + folder + "/" + publicIdWithoutExt;
        } catch (Exception e) {
            log.error("Error al extraer public_id de URL: {}", audioUrl, e);
            return null;
        }
    }
}