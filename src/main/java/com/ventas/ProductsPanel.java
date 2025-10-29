package com.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class ProductsPanel extends JPanel {

    private JTable table;
    private JTextField tfCode, tfName, tfPrice, tfStock;
    private DefaultTableModel model;

    public ProductsPanel() {
        setLayout(new BorderLayout(10, 10));

        // 🔹 Formulario superior
        JPanel form = new JPanel(new GridLayout(2, 5, 8, 8));
        tfCode = new JTextField();
        tfName = new JTextField();
        tfPrice = new JTextField();
        tfStock = new JTextField();

        form.add(new JLabel("Código:"));
        form.add(new JLabel("Nombre:"));
        form.add(new JLabel("Precio:"));
        form.add(new JLabel("Existencias:"));
        form.add(new JLabel(" "));

        form.add(tfCode);
        form.add(tfName);
        form.add(tfPrice);
        form.add(tfStock);

        // 🔹 Botones inferiores
        JButton btnGuardar = new JButton("Guardar/Actualizar");
        btnGuardar.addActionListener(this::onSave);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(this::onDelete);

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> clear());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnLimpiar);
        actions.add(btnEliminar);
        actions.add(btnGuardar);

        // 🔹 Tabla de productos
        model = new DefaultTableModel(new String[]{"ID", "Código", "Nombre", "Precio", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillFromSelection());

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        reload(); // carga los productos existentes
    }

    // 🔹 Guardar o actualizar
    private void onSave(ActionEvent e) {
        try {
            int id = getSelectedId();
            String code = tfCode.getText().trim();
            String name = tfName.getText().trim();
            double price = Double.parseDouble(tfPrice.getText().trim());
            int stock = Integer.parseInt(tfStock.getText().trim());

            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Código y nombre son obligatorios");
                return;
            }

            Product p = new Product(id, code, name, price, stock);
            ProductDAO.upsert(p);
            reload();
            clear();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Precio o stock inválido");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // 🔹 Eliminar producto
    private void onDelete(ActionEvent e) {
        int id = getSelectedId();
        if (id == 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "¿Eliminar producto?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                ProductDAO.delete(id);
                reload();
                clear();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void fillFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        tfCode.setText(model.getValueAt(row, 1).toString());
        tfName.setText(model.getValueAt(row, 2).toString());
        tfPrice.setText(model.getValueAt(row, 3).toString());
        tfStock.setText(model.getValueAt(row, 4).toString());
    }

    private int getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) return 0;
        return Integer.parseInt(model.getValueAt(row, 0).toString());
    }

    public void reload() {
        try {
            List<Product> list = ProductDAO.listAll();
            model.setRowCount(0);
            for (Product p : list) {
                model.addRow(new Object[]{p.id, p.code, p.name, p.price, p.stock});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void clear() {
        tfCode.setText("");
        tfName.setText("");
        tfPrice.setText("");
        tfStock.setText("");
        table.clearSelection();
    }
}
