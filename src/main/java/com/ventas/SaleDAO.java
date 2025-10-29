package com.ventas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    // 🔹 Registrar una nueva venta (transacción)
    public static boolean createSale(int productId, int quantity, String seller) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Obtener información del producto
                int currentStock = 0;
                double unitPrice = 0;
                String productName = null;

                try (PreparedStatement ps = c.prepareStatement("SELECT stock, price, name FROM products WHERE id=?")) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Producto no encontrado");
                        currentStock = rs.getInt("stock");
                        unitPrice = rs.getDouble("price");
                        productName = rs.getString("name");
                    }
                }

                if (quantity <= 0) throw new SQLException("Cantidad inválida");
                if (currentStock < quantity) throw new SQLException("Stock insuficiente");

                // Registrar la venta
                double total = unitPrice * quantity;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO sales(product_id, quantity, unit_price, total, seller) VALUES(?,?,?,?,?)")) {
                    ps.setInt(1, productId);
                    ps.setInt(2, quantity);
                    ps.setDouble(3, unitPrice);
                    ps.setDouble(4, total);
                    ps.setString(5, seller);
                    ps.executeUpdate();
                }

                // Descontar stock
                try (PreparedStatement ps = c.prepareStatement("UPDATE products SET stock = stock - ? WHERE id=?")) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }

                c.commit();
                return true;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    // 🔹 Listar las últimas ventas
    public static List<Sale> listRecent(int limit) throws SQLException {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.id, s.date, s.product_id, p.name AS product_name, s.quantity, s.unit_price, s.total, s.seller " +
                     "FROM sales s JOIN products p ON s.product_id=p.id " +
                     "ORDER BY s.id DESC LIMIT ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Sale(
                            rs.getInt("id"),
                            rs.getString("date"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("total"),
                            rs.getString("seller")
                    ));
                }
            }
        }
        return list;
    }
}
