package com.ventas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Agregar o actualizar producto
    public static void upsert(Product p) throws SQLException {
        try (Connection c = Database.getConnection()) {
            if (p.id == 0) {
                String sql = "INSERT INTO products(code, name, price, stock) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, p.code);
                    ps.setString(2, p.name);
                    ps.setDouble(3, p.price);
                    ps.setInt(4, p.stock);
                    ps.executeUpdate();
                }
            } else {
                String sql = "UPDATE products SET code=?, name=?, price=?, stock=? WHERE id=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, p.code);
                    ps.setString(2, p.name);
                    ps.setDouble(3, p.price);
                    ps.setInt(4, p.stock);
                    ps.setInt(5, p.id);
                    ps.executeUpdate();
                }
            }
        }
    }

    // Eliminar producto
    public static void delete(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Listar todos los productos
    public static List<Product> listAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, code, name, price, stock FROM products ORDER BY name")) {
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                ));
            }
        }
        return list;
    }

    // Ajustar existencias (suma o resta)
    public static void adjustStock(int productId, int delta) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE products SET stock = stock + ? WHERE id=?")) {
            ps.setInt(1, delta);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }
}
