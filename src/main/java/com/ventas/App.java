package com.ventas;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Aplica el tema del sistema (Windows o del entorno)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // ✅ Inicializa la base de datos
            Database.init(); // usa tu método actual initialize()

            // ✅ Muestra la ventana de inicio de sesión primero
            new LoginFrame().setVisible(true);
        });
    }
}
