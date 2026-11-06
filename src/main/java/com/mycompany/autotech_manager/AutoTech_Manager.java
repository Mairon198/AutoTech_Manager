package com.mycompany.autotech_manager;

import conexion.ConexionDB;
import vista.FrmMenuPrincipal;

import javax.swing.SwingUtilities;

public class AutoTech_Manager {

    public static void main(String[] args) {
        ConexionDB.probarConexion();
        SwingUtilities.invokeLater(() -> new FrmMenuPrincipal().setVisible(true));
    }
}
