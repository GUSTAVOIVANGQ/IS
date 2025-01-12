package com.web.book.version.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class DropboxStorageService {
    private static final Logger logger = LoggerFactory.getLogger(DropboxStorageService.class);

    private final DbxClientV2 dropboxClient;
    private static final String DROPBOX_BASE_PATH = "/profiles/";

    @Autowired
    public DropboxStorageService(DbxClientV2 dropboxClient) {
        this.dropboxClient = dropboxClient;
    }

    /**
     * Sube un archivo a Dropbox
     * @param file Archivo a subir
     * @param username Nombre de usuario para crear la estructura de carpetas
     * @return URL del archivo compartido
     */
    public String uploadFile(MultipartFile file, String username) throws IOException, DbxException {
        String filename = generateUniqueFilename(file.getOriginalFilename());
        String dropboxPath = DROPBOX_BASE_PATH + username + "/" + filename;

        try (InputStream in = file.getInputStream()) {
            FileMetadata metadata = dropboxClient.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(in);

            // Crear enlace compartido y convertirlo a URL directa
            SharedLinkMetadata sharedLinkMetadata = dropboxClient.sharing().createSharedLinkWithSettings(dropboxPath);
            String directUrl = sharedLinkMetadata.getUrl()
                .replace("www.dropbox.com", "dl.dropboxusercontent.com")
                .replace("?dl=0", "")
                .replace("&dl=0", "");
            
            logger.info("URL generada para archivo: {}", directUrl);
            return directUrl;
        }
    }

    /**
     * Elimina un archivo de Dropbox
     * @param username Nombre de usuario
     * @param filename Nombre del archivo
     */
    public void deleteFile(String username, String filename) throws DbxException {
        String dropboxPath = DROPBOX_BASE_PATH + username + "/" + filename;
        dropboxClient.files().deleteV2(dropboxPath);
    }

    /**
     * Obtiene la URL de un archivo
     * @param username Nombre de usuario
     * @param filename Nombre del archivo
     * @return URL del archivo
     */
    public Optional<String> getFileUrl(String username, String filename) {
        try {
            String dropboxPath = DROPBOX_BASE_PATH + username + "/" + filename;
            SharedLinkMetadata metadata = dropboxClient.sharing().createSharedLinkWithSettings(dropboxPath);
            return Optional.of(metadata.getUrl().replace("?dl=0", "?raw=1"));
        } catch (DbxException e) {
            return Optional.empty();
        }
    }

    /**
     * Genera un nombre único para el archivo
     * @param originalFilename Nombre original del archivo
     * @return Nombre único del archivo
     */
    private String generateUniqueFilename(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return timestamp + extension;
    }
}
