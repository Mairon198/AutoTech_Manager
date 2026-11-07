package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class Configuracion {

    private static final String ARCHIVO = "config.properties";
    private static final Properties PROPS = new Properties();
    private static boolean cargado = false;

    static {
        try (FileInputStream in = new FileInputStream(ARCHIVO)) {
            PROPS.load(in);
            cargado = true;
        } catch (IOException e) {
            cargado = false;
        }
    }

    private Configuracion() {
    }

    public static boolean estaCargado() {
        return cargado;
    }

    public static String get(String clave) {
        String valor = PROPS.getProperty(clave);
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
