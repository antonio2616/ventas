package com.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportsPanel extends JPanel {

    private JLabel lblToday, lblMonth, lblRange;
    private DefaultTableModel lowStockModel;
    private DefaultTableModel rangeSalesModel;
    private org.jdatepicker.impl.JDatePickerImpl startPicker, endPicker;

    public ReportsPanel() {
        setLayout(new BorderLayout(10,10));

        // 🔹 Encabezado general
        JPanel top = new JPanel(new GridLayout(3, 1, 10, 10));

        lblToday = new JLabel("Ventas hoy: $0.00");
        lblMonth = new JLabel("Ventas mes: $0.00");
        lblRange = new JLabel("Ventas en rango: —");

        lblToday.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRange.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JPanel totalsPanel = new JPanel(new GridLayout(1,2,10,10));
        totalsPanel.add(lblToday);
        totalsPanel.add(lblMonth);

        // 🔹 Panel de búsqueda por rango de fechas
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Consultar ventas del: "));

        java.util.Properties props = new java.util.Properties();
        props.put("text.today", "Hoy");
        props.put("text.month", "Mes");
        props.put("text.year", "Año");

        // Fecha inicial
        org.jdatepicker.impl.UtilDateModel modelStart = new org.jdatepicker.impl.UtilDateModel();
        org.jdatepicker.impl.JDatePanelImpl startPanel = new org.jdatepicker.impl.JDatePanelImpl(modelStart, props);
        startPicker = new org.jdatepicker.impl.JDatePickerImpl(startPanel,
                new javax.swing.JFormattedTextField.AbstractFormatter() {
                    private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    @Override public Object stringToValue(String text) throws java.text.ParseException { return sdf.parse(text); }
                    @Override public String valueToString(Object value) {
                        if (value != null) {
                            java.util.Calendar cal = (java.util.Calendar) value;
                            return sdf.format(cal.getTime());
                        }
                        return "";
                    }
                });

        datePanel.add(startPicker);
        datePanel.add(new JLabel("al"));
        
        // Fecha final
        org.jdatepicker.impl.UtilDateModel modelEnd = new org.jdatepicker.impl.UtilDateModel();
        org.jdatepicker.impl.JDatePanelImpl endPanel = new org.jdatepicker.impl.JDatePanelImpl(modelEnd, props);
        endPicker = new org.jdatepicker.impl.JDatePickerImpl(endPanel,
                new javax.swing.JFormattedTextField.AbstractFormatter() {
                    private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    @Override public Object stringToValue(String text) throws java.text.ParseException { return sdf.parse(text); }
                    @Override public String valueToString(Object value) {
                        if (value != null) {
                            java.util.Calendar cal = (java.util.Calendar) value;
                            return sdf.format(cal.getTime());
                        }
                        return "";
                    }
                });

        datePanel.add(endPicker);

        JButton btnSearch = new JButton("Buscar rango");
        btnSearch.addActionListener(e -> searchByRange());
        datePanel.add(btnSearch);
        datePanel.add(lblRange);

        top.add(totalsPanel);
        top.add(datePanel);

        // 🔹 Tabla de ventas por rango
        rangeSalesModel = new DefaultTableModel(new String[]{"Fecha","Producto","Cant.","P. Unit.","Total","Vendedor"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tblRangeSales = new JTable(rangeSalesModel);
        JScrollPane scrollRange = new JScrollPane(tblRangeSales);

        JLabel lblSales = new JLabel("Detalle de ventas en el rango seleccionado:");
        lblSales.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel salesPanel = new JPanel(new BorderLayout(5,5));
        salesPanel.add(lblSales, BorderLayout.NORTH);
        salesPanel.add(scrollRange, BorderLayout.CENTER);

        // 🔹 Tabla de productos con bajo stock
        lowStockModel = new DefaultTableModel(new String[]{"ID", "Código", "Nombre", "Stock"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblLowStock = new JTable(lowStockModel);
        JScrollPane scrollLow = new JScrollPane(tblLowStock);

        JLabel lblLow = new JLabel("Productos con bajo stock (≤ 5 unidades):");
        lblLow.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel lowPanel = new JPanel(new BorderLayout(5,5));
        lowPanel.add(lblLow, BorderLayout.NORTH);
        lowPanel.add(scrollLow, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2,1,10,10));
        bottom.add(salesPanel);
        bottom.add(lowPanel);

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);

        loadData();
    }

    // 🔹 Carga totales generales y bajo stock
    private void loadData() {
        try {
            lblToday.setText(String.format("Ventas hoy: $%.2f", totalToday()));
            lblMonth.setText(String.format("Ventas mes: $%.2f", totalMonth()));
            lblRange.setText("Ventas en rango: —");

            lowStockModel.setRowCount(0);
            for (Product p : lowStock(5)) {
                lowStockModel.addRow(new Object[]{p.id, p.code, p.name, p.stock});
            }

            rangeSalesModel.setRowCount(0);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar reportes: " + e.getMessage());
        }
    }

    private double totalToday() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total),0) FROM sales WHERE date(date)=date('now','localtime')";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    private double totalMonth() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total),0) FROM sales WHERE strftime('%Y-%m', date)=strftime('%Y-%m','now','localtime')";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    // 🔹 Buscar ventas entre dos fechas
    private void searchByRange() {
        java.util.Date start = (java.util.Date) startPicker.getModel().getValue();
        java.util.Date end = (java.util.Date) endPicker.getModel().getValue();

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this, "Selecciona las dos fechas (inicio y fin).");
            return;
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String startStr = sdf.format(start);
        String endStr = sdf.format(end);

        if (start.after(end)) {
            JOptionPane.showMessageDialog(this, "La fecha inicial no puede ser posterior a la final.");
            return;
        }

        String sqlTotal = "SELECT COALESCE(SUM(total),0) FROM sales WHERE date(date) BETWEEN ? AND ?";
        String sqlDetail = "SELECT s.date, p.name, s.quantity, s.unit_price, s.total, s.seller " +
                "FROM sales s JOIN products p ON s.product_id=p.id " +
                "WHERE date(s.date) BETWEEN ? AND ? ORDER BY s.date";

        try (Connection c = Database.getConnection()) {
            // 🔹 Total del rango
            try (PreparedStatement ps = c.prepareStatement(sqlTotal)) {
                ps.setString(1, startStr);
                ps.setString(2, endStr);
                try (ResultSet rs = ps.executeQuery()) {
                    double total = rs.next() ? rs.getDouble(1) : 0.0;
                    lblRange.setText(String.format("Ventas entre %s y %s: $%.2f", startStr, endStr, total));
                }
            }

            // 🔹 Detalle del rango
            rangeSalesModel.setRowCount(0);
            try (PreparedStatement ps = c.prepareStatement(sqlDetail)) {
                ps.setString(1, startStr);
                ps.setString(2, endStr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rangeSalesModel.addRow(new Object[]{
                                rs.getString("date"),
                                rs.getString("name"),
                                rs.getInt("quantity"),
                                rs.getDouble("unit_price"),
                                rs.getDouble("total"),
                                rs.getString("seller")
                        });
                    }
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al consultar el rango: " + e.getMessage());
        }
    }

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

    public void reload() {
        loadData();
    }
}