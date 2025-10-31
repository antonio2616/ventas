package com.ventas;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;



public class ReportsPanel extends JPanel {

    private org.jdatepicker.impl.JDatePickerImpl startPicker, endPicker;
    private DefaultTableModel modelSales, modelPurchases;
    private JLabel lblSalesTotal, lblPurchasesTotal, lblBalance;
    private double totalSales, totalPurchases;

    public ReportsPanel() {
        setLayout(new BorderLayout(10,10));

        // 🔹 Panel de búsqueda por rango
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Rango de fechas: "));

        java.util.Properties props = new java.util.Properties();
        props.put("text.today", "Hoy");
        props.put("text.month", "Mes");
        props.put("text.year", "Año");

        org.jdatepicker.impl.UtilDateModel modelStart = new org.jdatepicker.impl.UtilDateModel();
        org.jdatepicker.impl.JDatePanelImpl panelStart = new org.jdatepicker.impl.JDatePanelImpl(modelStart, props);
        startPicker = new org.jdatepicker.impl.JDatePickerImpl(panelStart, new javax.swing.JFormattedTextField.AbstractFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            @Override public Object stringToValue(String text) throws java.text.ParseException { return sdf.parse(text); }
            @Override public String valueToString(Object value) {
                if (value != null) return sdf.format(((Calendar) value).getTime());
                return "";
            }
        });

        org.jdatepicker.impl.UtilDateModel modelEnd = new org.jdatepicker.impl.UtilDateModel();
        org.jdatepicker.impl.JDatePanelImpl panelEnd = new org.jdatepicker.impl.JDatePanelImpl(modelEnd, props);
        endPicker = new org.jdatepicker.impl.JDatePickerImpl(panelEnd, new javax.swing.JFormattedTextField.AbstractFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            @Override public Object stringToValue(String text) throws java.text.ParseException { return sdf.parse(text); }
            @Override public String valueToString(Object value) {
                if (value != null) return sdf.format(((Calendar) value).getTime());
                return "";
            }
        });

        JButton btnSearch = new JButton("Buscar");
        btnSearch.addActionListener(e -> searchByRange());

        JButton btnPDF = new JButton("Exportar PDF");
        btnPDF.addActionListener(e -> exportPDF());

        top.add(startPicker);
        top.add(new JLabel(" al "));
        top.add(endPicker);
        top.add(btnSearch);
        top.add(btnPDF);

        // 🔹 Tablas
        modelSales = new DefaultTableModel(new String[]{"Fecha","Producto","Cant.","P. Unit.","Total","Vendedor"},0);
        JTable tblSales = new JTable(modelSales);
        JScrollPane spSales = new JScrollPane(tblSales);

        modelPurchases = new DefaultTableModel(new String[]{"Fecha","Producto","Cant.","Costo Unit.","Total"},0);
        JTable tblPurchases = new JTable(modelPurchases);
        JScrollPane spPurchases = new JScrollPane(tblPurchases);

        lblSalesTotal = new JLabel("Total Ventas: $0.00");
        lblPurchasesTotal = new JLabel("Total Compras: $0.00");
        lblBalance = new JLabel("Balance (Ventas - Compras): $0.00");

        lblSalesTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lblPurchasesTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lblBalance.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));

        JPanel panelTotals = new JPanel(new GridLayout(1,3));
        panelTotals.add(lblSalesTotal);
        panelTotals.add(lblPurchasesTotal);
        panelTotals.add(lblBalance);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ventas", spSales);
        tabs.addTab("Compras", spPurchases);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(panelTotals, BorderLayout.SOUTH);
    }

    // 🔹 Buscar entre fechas
    private void searchByRange() {
        java.util.Date start = (java.util.Date) startPicker.getModel().getValue();
        java.util.Date end = (java.util.Date) endPicker.getModel().getValue();

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this, "Selecciona ambas fechas.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startStr = sdf.format(start);
        String endStr = sdf.format(end);

        if (start.after(end)) {
            JOptionPane.showMessageDialog(this, "La fecha inicial no puede ser posterior a la final.");
            return;
        }

        loadSales(startStr, endStr);
        loadPurchases(startStr, endStr);
        lblBalance.setText(String.format("Balance (Ventas - Compras): $%.2f", (totalSales - totalPurchases)));
    }

    private void loadSales(String start, String end) {
        modelSales.setRowCount(0);
        totalSales = 0;
        String sql = "SELECT s.date, p.name, s.quantity, s.unit_price, s.total, s.seller " +
                "FROM sales s JOIN products p ON s.product_id=p.id WHERE date(s.date) BETWEEN ? AND ? ORDER BY s.date";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelSales.addRow(new Object[]{
                        rs.getString("date"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("total"),
                        rs.getString("seller")
                });
                totalSales += rs.getDouble("total");
            }
            lblSalesTotal.setText(String.format("Total Ventas: $%.2f", totalSales));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar ventas: " + e.getMessage());
        }
    }

    private void loadPurchases(String start, String end) {
        modelPurchases.setRowCount(0);
        totalPurchases = 0;
        String sql = "SELECT pr.date, p.name, pr.quantity, pr.unit_cost, pr.total " +
                "FROM purchases pr JOIN products p ON pr.product_id=p.id " +
                "WHERE date(pr.date) BETWEEN ? AND ? ORDER BY pr.date";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelPurchases.addRow(new Object[]{
                        rs.getString("date"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_cost"),
                        rs.getDouble("total")
                });
                totalPurchases += rs.getDouble("total");
            }
            lblPurchasesTotal.setText(String.format("Total Compras: $%.2f", totalPurchases));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar compras: " + e.getMessage());
        }
    }

    // 🔹 Exportar PDF unificado
    private void exportPDF() {
        if (modelSales.getRowCount() == 0 && modelPurchases.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.");
            return;
        }
        try {
            String fileName = "reporte_general.pdf";
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();

            doc.add(new Paragraph("CONTROL DE VENTAS - REPORTE GENERAL",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
           // 🔹 Formato limpio de fechas
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                String fechaGeneracion = sdf.format(new java.util.Date());

            // Detectar rango actual mostrado (por las fechas seleccionadas)
                java.util.Date start = (java.util.Date) startPicker.getModel().getValue();
                java.util.Date end = (java.util.Date) endPicker.getModel().getValue();
                String rangoTexto = "";
                    if (start != null && end != null) {
                        rangoTexto = "Rango del reporte: " + sdf.format(start) + " al " + sdf.format(end);
                        }
            // 🔹 Insertar logo en el PDF (si existe)
            try {
                String logoPath = "src/main/resources/logo.png"; // ruta del logo
                com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoPath);
                logo.scaleToFit(80, 80);
                logo.setAlignment(Element.ALIGN_LEFT);
                doc.add(logo);
            } catch (Exception ex) {
                System.out.println("⚠️ No se pudo cargar el logo en el PDF: " + ex.getMessage());
                }

            // Encabezado limpio
                doc.add(new Paragraph("📄 REPORTE GENERAL DE MOVIMIENTOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                doc.add(new Paragraph("Fecha de generación: " + fechaGeneracion));
                if (!rangoTexto.isEmpty()) doc.add(new Paragraph(rangoTexto));
                doc.add(new Paragraph(" "));

            // 🔸 Totales generales
            doc.add(new Paragraph(String.format("Total Ventas: $%.2f", totalSales)));
            doc.add(new Paragraph(String.format("Total Compras: $%.2f", totalPurchases)));
            doc.add(new Paragraph(String.format("Balance (Ventas - Compras): $%.2f", (totalSales - totalPurchases))));
            doc.add(new Paragraph(" "));

            // 🔹 Tabla de ventas
            if (modelSales.getRowCount() > 0) {
                doc.add(new Paragraph("Ventas registradas:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                PdfPTable tbl1 = new PdfPTable(modelSales.getColumnCount());
                for (int i = 0; i < modelSales.getColumnCount(); i++)
                    tbl1.addCell(modelSales.getColumnName(i));
                for (int i = 0; i < modelSales.getRowCount(); i++)
                    for (int j = 0; j < modelSales.getColumnCount(); j++)
                        tbl1.addCell(modelSales.getValueAt(i,j).toString());
                doc.add(tbl1);
                doc.add(new Paragraph(" "));
            }

            // 🔹 Tabla de compras
            if (modelPurchases.getRowCount() > 0) {
                doc.add(new Paragraph("Compras registradas:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                PdfPTable tbl2 = new PdfPTable(modelPurchases.getColumnCount());
                for (int i = 0; i < modelPurchases.getColumnCount(); i++)
                    tbl2.addCell(modelPurchases.getColumnName(i));
                for (int i = 0; i < modelPurchases.getRowCount(); i++)
                    for (int j = 0; j < modelPurchases.getColumnCount(); j++)
                        tbl2.addCell(modelPurchases.getValueAt(i,j).toString());
                doc.add(tbl2);
            }

            doc.close();
            JOptionPane.showMessageDialog(this, "Reporte PDF generado: " + fileName);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al exportar PDF: " + e.getMessage());
        }
    }

    public void reload() {
        // Sin datos iniciales hasta que se busque rango
    }
}