package com.mycompany.autotech_manager;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import vista.FrmLogin;

/**
 * Punto de entrada de AutoTech Manager: abre el inicio de sesión y, si es correcto, el menú principal.
 */
public class AutoTech_Manager {

    public static void main(String[] args) {
        try {
            // Apariencia nativa del sistema operativo (Windows en las computadoras del equipo)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar la apariencia del sistema: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}
