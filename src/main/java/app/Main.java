package app;

import com.formdev.flatlaf.FlatIntelliJLaf; // O FlatDarkLaf si prefieres modo oscuro por defecto
import views.FrmLogin;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. Inicializar FlatLaf antes de crear componentes gráficos
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
            // Si prefieres modo oscuro: UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // 2. Ejecutar la interfaz en el hilo de eventos de Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            FrmLogin ventana = new FrmLogin();
            ventana.setVisible(true);
        });
    }
}