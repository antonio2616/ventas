package com.ventas;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Control de Ventas e Inventario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        // Pestañas principales
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Productos", new ProductsPanel());

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }
}
