package com.my_band_lab.my_band_lab.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadServiceImpl implements ImageUploadService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        log.info("=== INICIANDO UPLOAD A CLOUDINARY ===");
        log.info("Folder: {}", folder);
        log.info("File name: {}", file.getOriginalFilename());
        log.info("File size: {} bytes", file.getSize());
        log.info("Content type: {}", file.getContentType());

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        // Validar tamaño (máximo 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La imagen no puede superar los 5MB");
        }

        Map<String, Object> params = ObjectUtils.asMap(
                "folder", "mybandlab/" + folder,
                "allowed_formats", Arrays.asList("jpg", "jpeg", "png", "webp")
        );

        log.info("Params para Cloudinary: {}", params);

        try {
            log.info("Intentando subir a Cloudinary...");
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Imagen subida exitosamente: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("Error al subir imagen a Cloudinary", e);
            throw new IOException("Error al procesar la imagen: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String publicId = extractPublicIdFromUrl(imageUrl);
                if (publicId != null) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    log.info("Imagen eliminada: {}", publicId);
                }
            }
        } catch (Exception e) {
            log.error("Error al eliminar imagen de Cloudinary: {}", e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Ejemplo URL: https://res.cloudinary.com/demo/image/upload/v1234567890/mybandlab/profiles/abc123.jpg
            String[] parts = imageUrl.split("/");
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
            log.error("Error al extraer public_id de URL: {}", imageUrl, e);
            return null;
        }
    }
}