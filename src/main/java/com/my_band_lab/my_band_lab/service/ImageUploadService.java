package com.my_band_lab.my_band_lab.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ImageUploadService {

    /**
     * Sube una imagen a Cloudinary
     * @param file Archivo de imagen
     * @param folder Carpeta destino (profiles, artists, groups)
     * @return URL segura de la imagen subida
     * @throws IOException Si hay error al subir la imagen
     */
    String uploadImage(MultipartFile file, String folder) throws IOException;

    /**
     * Elimina una imagen de Cloudinary
     * @param imageUrl URL de la imagen a eliminar
     */
    void deleteImage(String imageUrl);
}