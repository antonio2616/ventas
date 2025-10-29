package com.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportsPanel extends JPanel {

    private JLabel lblToday, lblMonth, lblByDate;
    private DefaultTableModel lowStockModel;
    private JTextField tfDate; // campo para escribir o seleccionar fecha (YYYY-MM-DD)

    public ReportsPanel() {
        setLayout(new BorderLayout(10,10));

        // 🔹 Encabezado con totales
        JPanel top = new JPanel(new GridLayout(3, 1, 10, 10));

        lblToday = new JLabel("Ventas hoy: $0.00");
        lblMonth = new JLabel("Ventas mes: $0.00");
        lblByDate = new JLabel("Ventas en fecha seleccionada: —");

        lblToday.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblByDate.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JPanel totalsPanel = new JPanel(new GridLayout(1,2,10,10));
        totalsPanel.add(lblToday);
        totalsPanel.add(lblMonth);

        // 🔹 Panel de búsqueda por fecha
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Consultar ventas del día (YYYY-MM-DD): "));
        tfDate = new JTextField(10);
        JButton btnSearch = new JButton("Buscar");
        btnSearch.addActionListener(e -> searchByDate());
        datePanel.add(tfDate);
        datePanel.add(btnSearch);
        datePanel.add(lblByDate);

        top.add(totalsPanel);
        top.add(datePanel);

        // 🔹 Tabla de productos con bajo stock
        lowStockModel = new DefaultTableModel(new String[]{"ID", "Código", "Nombre", "Stock"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblLowStock = new JTable(lowStockModel);
        JScrollPane scroll = new JScrollPane(tblLowStock);

        JLabel lblLow = new JLabel("Productos con bajo stock (≤ 5 unidades):");
        lblLow.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel bottom = new JPanel(new BorderLayout(5,5));
        bottom.add(lblLow, BorderLayout.NORTH);
        bottom.add(scroll, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);

        loadData();
    }

    // 🔹 Carga los totales y la lista de bajo stock
    private void loadData() {
        try {
            lblToday.setText(String.format("Ventas hoy: $%.2f", totalToday()));
            lblMonth.setText(String.format("Ventas mes: $%.2f", totalMonth()));
            lblByDate.setText("Ventas en fecha seleccionada: —");

            lowStockModel.setRowCount(0);
            for (Product p : lowStock(5)) {
                lowStockModel.addRow(new Object[]{p.id, p.code, p.name, p.stock});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar reportes: " + e.getMessage());
        }
    }

    // 🔹 Total de ventas de hoy
    private double totalToday() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total),0) FROM sales WHERE date(date)=date('now','localtime')";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    // 🔹 Total de ventas del mes
    private double totalMonth() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total),0) FROM sales WHERE strftime('%Y-%m', date)=strftime('%Y-%m','now','localtime')";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    // 🔹 Buscar ventas por fecha (YYYY-MM-DD)
    private void searchByDate() {
        String dateStr = tfDate.getText().trim();
        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, escribe una fecha (ejemplo: 2025-10-20)");
            return;
        }

        String sql = "SELECT COALESCE(SUM(total),0) FROM sales WHERE date(date)=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dateStr);
            try (ResultSet rs = ps.executeQuery()) {
                double total = rs.next() ? rs.getDouble(1) : 0.0;
                lblByDate.setText(String.format("Ventas en %s: $%.2f", dateStr, total));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al consultar la fecha: " + e.getMessage());
        }
    }

    // 🔹 Productos con stock bajo
    private List<Product> lowStock(int threshold) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, code, name, price, stock FROM products WHERE stock <= ? ORDER BY stock ASC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
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
        }
        return list;
    }

    // 🔹 Permitir actualización manual
    public void reload() {
        loadData();
    }
}
