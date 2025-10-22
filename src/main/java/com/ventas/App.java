package com.ventas;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Aplica el tema del sistema (Windows)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // ✅ Inicializa base de datos
            Database.init();

            // ✅ Abre la ventana principal con pestañas
            JFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
