package com.ventas;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ProductsPanel productsPanel;
    private SalesPanel salesPanel;
    private ReportsPanel reportsPanel;

    public MainFrame() {
        super("Control de Ventas e Inventario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        productsPanel = new ProductsPanel();
        salesPanel = new SalesPanel();
        reportsPanel = new ReportsPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Productos", productsPanel);
        tabs.addTab("Ventas", salesPanel);
        tabs.addTab("Reportes", reportsPanel);

        // 🔹 Cuando el usuario cambia de pestaña, recarga los datos visibles
        tabs.addChangeListener(e -> {
            int index = tabs.getSelectedIndex();
            switch (index) {
                case 0 -> productsPanel.reload();
                case 1 -> salesPanel.reloadProducts();
                case 2 -> reportsPanel.reload();
            }
        });

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }
}