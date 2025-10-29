package com.ventas;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ProductsPanel productsPanel;
    private SalesPanel salesPanel;

    public MainFrame() {
        super("Control de Ventas e Inventario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        productsPanel = new ProductsPanel();
        salesPanel = new SalesPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Productos", productsPanel);
        tabs.addTab("Ventas", salesPanel);

        // 🔹 Cuando el usuario cambia de pestaña, recarga los datos visibles
        tabs.addChangeListener(e -> {
            int index = tabs.getSelectedIndex();
            if (index == 0) {
                productsPanel.reload();  // refresca lista de productos
            } else if (index == 1) {
                salesPanel.reloadProducts(); // refresca lista en ventas
            }
        });

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }
}