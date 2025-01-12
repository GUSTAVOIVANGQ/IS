package com.web.book.version.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.dropbox.core.DbxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Service
public class FileUploadService {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    
    private final DropboxStorageService dropboxStorageService;

    @Autowired
    public FileUploadService(DropboxStorageService dropboxStorageService) {
        this.dropboxStorageService = dropboxStorageService;
    }
    
    public String uploadProfilePhoto(MultipartFile file, String username) throws IOException {
        try {
            logger.info("Iniciando carga de archivo para el usuario: {}", username);
            String fileUrl = dropboxStorageService.uploadFile(file, username);
            
            // Verifica que la URL sea válida
            if (fileUrl == null || fileUrl.isEmpty()) {
                throw new IOException("No se pudo obtener una URL válida para el archivo");
            }
            
            logger.info("URL de archivo generada: {}", fileUrl);
            return fileUrl;
        } catch (DbxException e) {
            logger.error("Error al cargar archivo en Dropbox: {}", e.getMessage());
            throw new IOException("Error al cargar el archivo en Dropbox", e);
        }
    }

    public void deleteProfilePhoto(String username, String filename) throws IOException {
        try {
            logger.info("Eliminando archivo {} del usuario {}", filename, username);
            dropboxStorageService.deleteFile(username, filename);
            logger.info("Archivo eliminado exitosamente");
        } catch (DbxException e) {
            logger.error("Error al eliminar archivo de Dropbox: {}", e.getMessage());
            throw new IOException("Error al eliminar el archivo de Dropbox", e);
        }
    }
}