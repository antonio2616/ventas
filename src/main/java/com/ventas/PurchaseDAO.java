package com.ventas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    public static void addPurchase(int productId, int quantity, double unitCost) throws SQLException {
        double total = quantity * unitCost;
        try (Connection c = Database.getConnection()) {
            // Insertar compra
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO purchases (product_id, quantity, unit_cost, total) VALUES (?,?,?,?)");
            ps.setInt(1, productId);
            ps.setInt(2, quantity);
            ps.setDouble(3, unitCost);
            ps.setDouble(4, total);
            ps.executeUpdate();

            // Actualizar stock (sumar)
            PreparedStatement upd = c.prepareStatement("UPDATE products SET stock = stock + ? WHERE id = ?");
            upd.setInt(1, quantity);
            upd.setInt(2, productId);
            upd.executeUpdate();
        }
    }

    public static List<Purchase> listPurchases() throws SQLException {
        List<Purchase> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, product_id, quantity, unit_cost, total, date FROM purchases ORDER BY date DESC")) {
            while (rs.next()) {
                list.add(new Purchase(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_cost"),
                        rs.getDouble("total"),
                        rs.getString("date")
                ));
            }
        }
        return list;
    }

    public static double totalPurchases() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total),0) FROM purchases";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }
}
