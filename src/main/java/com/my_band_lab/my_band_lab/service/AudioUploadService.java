package com.my_band_lab.my_band_lab.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface AudioUploadService {

    /**
     * Sube un archivo MP3 a Cloudinary
     * @param file Archivo MP3
     * @param folder Carpeta destino (songs)
     * @return URL segura del archivo subido
     * @throws IOException Si hay error en la subida
     */
    String uploadMp3(MultipartFile file, String folder) throws IOException;

    /**
     * Elimina un archivo MP3 de Cloudinary
     * @param audioUrl URL del archivo a eliminar
     */
    void deleteMp3(String audioUrl);
}