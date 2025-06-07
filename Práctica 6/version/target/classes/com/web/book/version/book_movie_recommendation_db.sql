-- Creación de Base de Datos
CREATE DATABASE book_movie_recommendation_db;
USE book_movie_recommendation_db;

-- Tabla de Usuarios
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    PASSWORD VARCHAR(255) NOT NULL,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rol VARCHAR(100) DEFAULT 'USUARIO',
    fotoUrl VARCHAR(255),
    activo VARCHAR(100) DEFAULT 'TRUE'
);

-- Tabla de Historial de Búsquedas
CREATE TABLE historial_busquedas (
    id_busqueda INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    fecha_busqueda TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    terminos_busqueda VARCHAR(255) NOT NULL,
    libro VARCHAR(100) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    serie VARCHAR(100) NOT NULL,
    pelicula VARCHAR(100) NOT NULL
);

-- Tabla de Favoritos
CREATE TABLE favoritos (
    id_favorito INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    libro VARCHAR(100) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    serie VARCHAR(100) NOT NULL,
    pelicula VARCHAR(100) NOT NULL,
    id_contenido VARCHAR(100) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    descripcion VARCHAR(100) NOT NULL,
    imagen_url VARCHAR(500)
);

-- Tabla de Recomendaciones
CREATE TABLE recomendaciones (
    id_recomendacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo_contenido VARCHAR(100) NOT NULL,
    libro VARCHAR(100) DEFAULT '',
    autor VARCHAR(100) DEFAULT '',
    serie VARCHAR(100) DEFAULT '',
    pelicula VARCHAR(100) DEFAULT '',
    id_contenido VARCHAR(100) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    imagen_url VARCHAR(500),
    fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    relevancia FLOAT DEFAULT 0.0
);

-- Tabla de Contenido de Libros (desde Open Library API)
CREATE TABLE libros (
    id_libro VARCHAR(100) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255),
    fecha_publicacion DATE,
    descripcion TEXT,
    imagen_url VARCHAR(500),
    editorial VARCHAR(255),
    categoria VARCHAR(100)
);

-- Tabla de Contenido de Series y Películas (desde TVMaze API)
CREATE TABLE contenido_audiovisual (
    id_contenido VARCHAR(100) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    tipo ENUM('SERIE', 'PELICULA') NOT NULL,
    descripcion TEXT,
    fecha_estreno DATE,
    imagen_url VARCHAR(500),
    genero VARCHAR(100),
    calificacion FLOAT
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_usuario_historial ON historial_busquedas(id_usuario);
CREATE INDEX idx_usuario_favoritos ON favoritos(id_usuario);
CREATE INDEX idx_usuario_recomendaciones ON recomendaciones(id_usuario);

-- Triggers para mantener la integridad de datos

-- Trigger para validar email único
DELIMITER $$
CREATE TRIGGER validar_email_unico 
BEFORE INSERT ON usuarios
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios WHERE email = NEW.email) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El correo electrónico ya está registrado';
    END IF;
END$$
DELIMITER ;

-- Trigger para validar username único
DELIMITER $$
CREATE TRIGGER validar_username_unico
BEFORE INSERT ON usuarios
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM usuarios WHERE username = NEW.username) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El nombre de usuario ya está en uso';
    END IF;
END$$
DELIMITER ;