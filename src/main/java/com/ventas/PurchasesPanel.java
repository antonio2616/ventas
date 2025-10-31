package com.ventas;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PurchasesPanel extends JPanel {

    private JComboBox<Product> cbProducts;
    private JTextField tfQuantity, tfUnitCost, tfTotal;
    private DefaultTableModel model;
    private JLabel lblTotalGeneral;

    public PurchasesPanel() {
        setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new GridLayout(2,4,10,10));
        cbProducts = new JComboBox<>();
        tfQuantity = new JTextField();
        tfUnitCost = new JTextField();
        tfTotal = new JTextField();
        tfTotal.setEditable(false);

        form.add(new JLabel("Producto:"));
        form.add(cbProducts);
        form.add(new JLabel("Cantidad:"));
        form.add(tfQuantity);
        form.add(new JLabel("Costo Unitario:"));
        form.add(tfUnitCost);
        form.add(new JLabel("Total:"));
        form.add(tfTotal);

        JButton btnAdd = new JButton("Registrar compra");
        btnAdd.addActionListener(e -> addPurchase());

        JPanel top = new JPanel(new BorderLayout(10,10));
        top.add(form, BorderLayout.CENTER);
        top.add(btnAdd, BorderLayout.EAST);

        // Tabla
        model = new DefaultTableModel(new String[]{"Fecha","Producto","Cantidad","Costo Unit.","Total"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        lblTotalGeneral = new JLabel("Total de compras: $0.00");
        lblTotalGeneral.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblTotalGeneral, BorderLayout.SOUTH);

        reloadProducts();
        reloadTable();
    }

    private void addPurchase() {
        Product p = (Product) cbProducts.getSelectedItem();
        if (p == null) return;
        try {
            int qty = Integer.parseInt(tfQuantity.getText());
            double cost = Double.parseDouble(tfUnitCost.getText());
            double total = qty * cost;
            tfTotal.setText(String.format("%.2f", total));

            PurchaseDAO.addPurchase(p.id, qty, cost);
            JOptionPane.showMessageDialog(this, "Compra registrada correctamente.");
            reloadProducts();
            reloadTable();
            tfQuantity.setText("");
            tfUnitCost.setText("");
            tfTotal.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad o costo inválido.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar compra: " + ex.getMessage());
        }
    }

    private void reloadProducts() {
        try {
            cbProducts.removeAllItems();
            for (Product p : ProductDAO.listAll()) {
                cbProducts.addItem(p);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage());
        }
    }

    private void reloadTable() {
        try {
            model.setRowCount(0);
            List<Purchase> list = PurchaseDAO.listPurchases();
            for (Purchase p : list) {
                String productName = "";
                for (Product prod : ProductDAO.listAll()) {
                    if (prod.id == p.productId) {
                        productName = prod.name;
                        break;
                    }
                }
                model.addRow(new Object[]{p.date, productName, p.quantity, p.unitCost, p.total});
            }
            lblTotalGeneral.setText(String.format("Total de compras: $%.2f", PurchaseDAO.totalPurchases()));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar compras: " + e.getMessage());
        }
    }

    public void reload() {
        reloadProducts();
        reloadTable();
    }
}