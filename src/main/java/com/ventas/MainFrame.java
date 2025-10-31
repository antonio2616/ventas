package com.ventas;

import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
    private ProductsPanel productsPanel;
    private SalesPanel salesPanel;
    private ReportsPanel reportsPanel;
    private PurchasesPanel purchasesPanel;

    public MainFrame() {
        super("Control de Ventas e Inventario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // 🔹 Establecer ícono de la ventana
        try {
            Image icon = new ImageIcon(getClass().getResource("/logo.png")).getImage();
            setIconImage(icon);
        } catch (Exception e) {
            System.out.println("⚠️ No se encontró el archivo de logo.");
        }


        productsPanel = new ProductsPanel();
        salesPanel = new SalesPanel();
        reportsPanel = new ReportsPanel();
        purchasesPanel = new PurchasesPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Productos", productsPanel);
        tabs.addTab("Ventas", salesPanel);
        tabs.addTab("Reportes", reportsPanel);
        tabs.addTab("Compras", purchasesPanel);

        // 🔹 Cuando el usuario cambia de pestaña, recarga los datos visibles
        tabs.addChangeListener(e -> {
            int index = tabs.getSelectedIndex();
            switch (index) {
                case 0 -> productsPanel.reload();
                case 1 -> salesPanel.reloadProducts();
                case 2 -> reportsPanel.reload();
                case 3 -> purchasesPanel.reload();
            }
        });

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }
}