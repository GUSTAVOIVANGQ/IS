package com.web.book.version.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLController {

    // Parámetros de conexión
    private static final String URL = "jdbc:mysql://localhost:3306/book_movie_recommendation_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Usar el nuevo usuario 'admin'
    private static final String PASSWORD = ""; // Contraseña 'admin'

    public static void main(String[] args) {
        Connection connection = null;

        try {
            // Conectar a la base de datos
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a la base de datos MySQL");

        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Conexión cerrada");
                }
            } catch (SQLException e) {
                System.out.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}