package com.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class SalesPanel extends JPanel {

    private JComboBox<Product> cbProduct;
    private JTextField tfQty, tfSeller, tfUnitPrice, tfTotal;
    private DefaultTableModel model;

    public SalesPanel() {
        setLayout(new BorderLayout(10,10));

        // 🔹 Formulario
        JPanel form = new JPanel(new GridLayout(2,5,8,8));
        cbProduct = new JComboBox<>();
        tfQty = new JTextField();
        tfSeller = new JTextField();
        tfUnitPrice = new JTextField();
        tfTotal = new JTextField();

        // 🔹 Escucha cambios en la cantidad para actualizar el total
        tfQty.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        private void update() { 
            updateTotal(); 
        }
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
             update(); 
             }
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
             update(); 
             }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { 
            update(); 
            }
        });

        tfUnitPrice.setEditable(false);
        tfTotal.setEditable(false);

        form.add(new JLabel("Producto:"));
        form.add(new JLabel("Cantidad:"));
        form.add(new JLabel("Vendedor:"));
        form.add(new JLabel("P. Unitario:"));
        form.add(new JLabel("Total:"));

        form.add(cbProduct);
        form.add(tfQty);
        form.add(tfSeller);
        form.add(tfUnitPrice);
        form.add(tfTotal);

        // 🔹 Botones
        JButton btnVender = new JButton("Registrar Venta");
        btnVender.addActionListener(this::onSell);

        JButton btnRefrescar = new JButton("Refrescar Productos");
        btnRefrescar.addActionListener(e -> reloadProducts());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnRefrescar);
        actions.add(btnVender);

        // 🔹 Tabla de ventas
        model = new DefaultTableModel(new String[]{"ID","Fecha","Producto","Cant.","Unit.","Total","Vendedor"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table = new JTable(model);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        cbProduct.addActionListener(e -> updatePrice());
        reloadProducts();
        reloadSales();
    }

    private void updatePrice() {
        Product p = (Product) cbProduct.getSelectedItem();
        if (p != null) {
            tfUnitPrice.setText(String.valueOf(p.price));
            updateTotal(); // recalcula también el total
            }
    }

    private void onSell(ActionEvent e) {
        Product p = (Product) cbProduct.getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto");
            return;
        }

        try {
            int qty = Integer.parseInt(tfQty.getText().trim());
            String seller = tfSeller.getText().trim();

            boolean ok = SaleDAO.createSale(p.id, qty, seller);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Venta registrada correctamente");
                // 🔹 Refrescar productos y ventas inmediatamente
                reloadProducts();
                reloadSales();

                // 🔹 Volver a seleccionar el mismo producto actualizado
                cbProduct.setSelectedItem(p);
                updatePrice();

                // 🔹 Limpiar campos
                tfQty.setText("");
                tfSeller.setText("");
                tfTotal.setText("");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public void reloadProducts() {
    Product selected = (Product) cbProduct.getSelectedItem(); // recordar selección
    String lastCode = selected != null ? selected.code : null;

    cbProduct.removeAllItems();
    try {
        for (Product p : ProductDAO.listAll()) {
            cbProduct.addItem(p);
            // 🔹 si era el producto anterior, lo volvemos a seleccionar
            if (lastCode != null && p.code.equals(lastCode)) {
                cbProduct.setSelectedItem(p);
            }
        }
        updatePrice();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

    private void reloadSales() {
        try {
            model.setRowCount(0);
            List<Sale> sales = SaleDAO.listRecent(100);
            for (Sale s : sales) {
                model.addRow(new Object[]{s.id, s.date, s.productName, s.quantity, s.unitPrice, s.total, s.seller});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateTotal() {
        Product p = (Product) cbProduct.getSelectedItem();
        if (p != null) {
            try {
                int q = Integer.parseInt(tfQty.getText().trim());
                tfTotal.setText(String.valueOf(p.price * q));
                } catch (Exception ex) {
                    tfTotal.setText("");
                    }
                }
        }
}
