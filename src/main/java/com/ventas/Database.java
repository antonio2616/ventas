package com.ventas;

import java.sql.*;

public class Database {
    // Ruta del archivo de base de datos
    private static final String URL = "jdbc:sqlite:data.db";

    // Conexión general
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Método que inicializa la base de datos y crea las tablas
    public static void init() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {

            // Tabla de productos
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code TEXT UNIQUE NOT NULL,
                    name TEXT NOT NULL,
                    price REAL NOT NULL,
                    stock INTEGER NOT NULL DEFAULT 0
                )
            """);

            // Tabla de ventas
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                    product_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    unit_price REAL NOT NULL,
                    total REAL NOT NULL,
                    seller TEXT,
                    FOREIGN KEY(product_id) REFERENCES products(id)
                )
            """);

            System.out.println("✅ Base de datos inicializada correctamente.");

        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar la base de datos: " + e.getMessage());
        }
    }
}
