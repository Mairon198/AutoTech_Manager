package com.mycompany.autotech_manager;

import conexion.ConexionDB;

/**
 * Punto de entrada de AutoTech Manager.
 * Por ahora solo prueba la conexión; luego abrirá el formulario principal.
 */
public class AutoTech_Manager {

    public static void main(String[] args) {
        ConexionDB.probarConexion();
    }
}
