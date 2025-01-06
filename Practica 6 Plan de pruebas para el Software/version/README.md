# Book & Movie Recommendations System

## API Documentation

Este proyecto proporciona una API REST para acceder a información sobre libros y shows (series/películas). La API requiere autenticación para todos sus endpoints.

### Autenticación

Todos los endpoints requieren autenticación mediante token JWT. Para obtener un token, use el endpoint de login:

```http
POST /auth/login
```

El token debe ser incluido en el header de las peticiones:
```http
Authorization: Bearer <your_token>
```

### Books API Endpoints

#### Buscar Libros
```http
GET /api/books/search?query={query}
```
- **Descripción**: Busca libros por término de búsqueda
- **Parámetros**:
  - `query` (requerido): Término de búsqueda
- **Ejemplo**: `/api/books/search?query=harry+potter`
- **Respuesta**: Array de objetos libro
```json
[
  {
    "title": "Harry Potter and the Philosopher's Stone",
    "author": "J.K. Rowling",
    "isbn": "9780747532699",
    "publishYear": "1997",
    "coverUrl": "https://..."
  }
]
```

#### Obtener Libro por ISBN
```http
GET /api/books/{isbn}
```
- **Descripción**: Obtiene detalles de un libro específico
- **Parámetros**:
  - `isbn` (requerido): ISBN del libro
- **Ejemplo**: `/api/books/9780747532699`
- **Respuesta**: Objeto libro con detalles completos
```json
{
  "title": "Harry Potter and the Philosopher's Stone",
  "author": "J.K. Rowling",
  "isbn": "9780747532699",
  "publishYear": "1997",
  "description": "...",
  "coverUrl": "https://...",
  "publisher": "Bloomsbury",
  "genres": ["Fantasy", "Young Adult"]
}
```

#### Obtener Libros Destacados
```http
GET /api/books/featured
```
- **Descripción**: Obtiene una lista de libros destacados
- **Respuesta**: Array de objetos libro
```json
[
  {
    "title": "Book Title",
    "author": "Author Name",
    "isbn": "1234567890",
    "rating": "4.5",
    "coverUrl": "https://..."
  }
]
```

### Shows API Endpoints

#### Buscar Shows/Series
```http
GET /api/shows/search?query={query}
```
- **Descripción**: Busca shows/series por término de búsqueda
- **Parámetros**:
  - `query` (requerido): Término de búsqueda
- **Ejemplo**: `/api/shows/search?query=breaking+bad`
- **Respuesta**: Array de objetos show/serie
```json
[
  {
    "title": "Breaking Bad",
    "year": "2008",
    "genres": ["Crime", "Drama", "Thriller"],
    "rating": "9.5",
    "coverUrl": "https://..."
  }
]
```

#### Obtener Show/Serie por ID
```http
GET /api/shows/{id}
```
- **Descripción**: Obtiene detalles de un show/serie específico
- **Parámetros**:
  - `id` (requerido): ID del show/serie
- **Ejemplo**: `/api/shows/169`
- **Respuesta**: Objeto show/serie con detalles completos
```json
{
  "title": "Breaking Bad",
  "year": "2008",
  "genres": ["Crime", "Drama", "Thriller"],
  "rating": "9.5",
  "description": "...",
  "coverUrl": "https://...",
  "cast": ["Bryan Cranston", "Aaron Paul", "Anna Gunn"]
}
```

#### Obtener Shows/Series Destacados
```http
GET /api/shows/featured
```
- **Descripción**: Obtiene una lista de shows/series destacados
- **Respuesta**: Array de objetos show/serie
```json
[
  {
    "title": "Show Title",
    "year": "Year",
    "genres": ["Genre1", "Genre2"],
    "rating": "Rating",
    "coverUrl": "https://..."
  }
]
```

### User API Endpoints

#### Obtener Perfil de Usuario
```http
GET /api/users/profile
```
- **Descripción**: Obtiene el perfil del usuario autenticado
- **Requiere**: Token JWT
- **Respuesta**: Objeto con datos del usuario
```json
{
  "id": 1,
  "username": "usuario123",
  "email": "usuario@email.com",
  "nombre": "Nombre",
  "apellido": "Apellido",
  "profilePhotoUrl": "https://...",
  "rol": "USER",
  "fechaRegistro": "2024-01-20T10:30:00Z"
}
```

#### Actualizar Perfil de Usuario
```http
PUT /api/users/profile
```
- **Descripción**: Actualiza los datos del perfil del usuario
- **Requiere**: Token JWT
- **Body**:
```json
{
  "nombre": "Nuevo Nombre",
  "apellido": "Nuevo Apellido",
  "email": "nuevo@email.com"
}
```
- **Respuesta**: Objeto con datos actualizados

#### Subir Foto de Perfil
```http
POST /api/users/profile/photo
```
- **Descripción**: Sube o actualiza la foto de perfil
- **Requiere**: Token JWT
- **Body**: Form-data con archivo de imagen
- **Parámetros**:
  - `photo` (requerido): Archivo de imagen (max 5MB, formatos: jpg, png)
- **Respuesta**: URL de la nueva foto

#### Gestión de Usuarios (Solo Admin)
```http
GET /api/users
```
- **Descripción**: Lista todos los usuarios
- **Requiere**: Token JWT con rol ADMIN
- **Respuesta**: Array de usuarios

```http
DELETE /api/users/{id}
```
- **Descripción**: Elimina un usuario
- **Requiere**: Token JWT con rol ADMIN
- **Parámetros**:
  - `id` (requerido): ID del usuario
- **Respuesta**: Confirmación de eliminación

### Códigos de Error

La API utiliza códigos de estado HTTP estándar:

- 200: Éxito
- 400: Error en la solicitud
- 401: No autorizado
- 403: Prohibido
- 404: No encontrado
- 500: Error interno del servidor

### Limitaciones de Uso

- Máximo 100 peticiones por hora por usuario
- Tamaño máximo de respuesta: 1MB
- Caché de resultados: 1 hora

### Recomendaciones de Uso

1. Utilizar caché del lado del cliente
2. Implementar manejo de errores robusto
3. Usar paginación cuando sea posible
4. Incluir timeout en las llamadas

## Tecnologías Utilizadas

- Spring Boot
- Spring Security
- MySQL
- OpenLibrary API
- TVMaze API

## Configuración de Claves API

### Google OAuth2

El sistema implementa autenticación con Google OAuth2 de forma opcional. El sistema funcionará sin estas claves, pero la funcionalidad de inicio de sesión con Google estará deshabilitada.

#### Obtener Credenciales de Google

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Ve a "APIs & Services" > "Credentials"
4. Haz clic en "Create Credentials" > "OAuth client ID"
5. Configura tu aplicación:
   - Application Type: Web application
   - Name: Book & Movie Recommendations
   - Authorized JavaScript origins: `http://localhost:8083`
   - Authorized redirect URIs:
     - `http://localhost:8083/login/oauth2/code/google`
     - `http://localhost:8083/oauth2/authorization/google`
6. Guarda las credenciales generadas (Client ID y Client Secret)

#### Configuración en el Proyecto

Hay tres formas de configurar las credenciales:

1. **Variables de Entorno** (Recomendado para producción):
```bash
export GOOGLE_CLIENT_ID=tu_client_id
export GOOGLE_CLIENT_SECRET=tu_client_secret
export GOOGLE_AUTH_ENABLED=true
```

2. **application.properties** (Desarrollo local):
```properties
api.keys.google-client-id=tu_client_id
api.keys.google-client-secret=tu_client_secret
api.keys.google-auth-enabled=true
```

3. **application-local.properties** (No incluido en control de versiones):
Crea un archivo `application-local.properties` en `src/main/resources/` con:
```properties
api.keys.google-client-id=tu_client_id
api.keys.google-client-secret=tu_client_secret
api.keys.google-auth-enabled=true
```

#### Verificación de la Configuración

Para verificar que las claves están correctamente configuradas:

1. Inicia la aplicación
2. Ve a la página de login
3. Deberías ver el botón "Iniciar sesión con Google"
4. Si las claves no están configuradas, verás un mensaje indicando que el login con Google no está disponible

#### Solución de Problemas

Si encuentras problemas con la autenticación de Google:

1. Verifica que las URLs de redirección coincidan exactamente
2. Asegúrate de que el proyecto de Google Cloud tenga la API de OAuth2 habilitada
3. Revisa los logs de la aplicación para mensajes de error específicos
4. Verifica que las variables de entorno estén correctamente configuradas

#### Funcionamiento sin Claves API

El sistema está diseñado para funcionar sin las claves de Google OAuth2:

- La autenticación tradicional (usuario/contraseña) funcionará normalmente
- El botón de login con Google no se mostrará
- Se mostrará un mensaje informativo sobre la no disponibilidad del servicio
- Todas las demás funcionalidades del sistema permanecerán operativas

## Instalación y Configuración

[Instrucciones detalladas de instalación serán agregadas]
