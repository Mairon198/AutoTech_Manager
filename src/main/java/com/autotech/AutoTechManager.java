package com.autotech;

import com.autotech.conexion.ConexionDB;

/**
 * Punto de entrada de AutoTech Manager.
 * Por ahora solo prueba la conexión; luego abrirá el formulario principal.
 */
public class AutoTechManager {

    public static void main(String[] args) {
        ConexionDB.probarConexion();
    }
}
