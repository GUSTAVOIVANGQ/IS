### DropboxStorageService para manejar las operaciones con Dropbox. Aquí está la implementación:

### [DropboxStorageService.java]
Esta implementación proporciona:

1. **Subida de archivos**
   - Método `uploadFile` que sube archivos a Dropbox y devuelve una URL pública
   - Organiza los archivos en carpetas por usuario

2. **Eliminación de archivos**
   - Método `deleteFile` para eliminar archivos de Dropbox
   - Usa la ruta completa del archivo incluyendo el usuario

3. **Obtención de URLs**
   - Método `getFileUrl` para obtener la URL pública de un archivo
   - Devuelve URLs directas para las imágenes

4. **Gestión de nombres de archivo**
   - Método `generateUniqueFilename` para evitar conflictos de nombres
   - Usa timestamps para garantizar unicidad

Para usar este servicio:

1. Inyéctalo en tus controladores o servicios:
```java
@Autowired
private DropboxStorageService dropboxStorageService;
```

2. Úsalo para manejar archivos:
```java
// Subir archivo
String url = dropboxStorageService.uploadFile(multipartFile, username);

// Eliminar archivo
dropboxStorageService.deleteFile(username, filename);

// Obtener URL
Optional<String> url = dropboxStorageService.getFileUrl(username, filename);
```

Los archivos se organizarán en Dropbox en la estructura:
```
/profiles/
  ├── username1/
  │   ├── file1.jpg
  │   └── file2.png
  └── username2/
      └── file3.jpg
```

Made changes.