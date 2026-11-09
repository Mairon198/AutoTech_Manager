package util;

import java.time.LocalDateTime;
import modelo.Usuario;

/** Usuario que inició sesión en la aplicación. */
public final class Sesion {

    private static Usuario actual;
    private static LocalDateTime inicio;

    private Sesion() {
    }

    public static void iniciar(Usuario u) {
        actual = u;
        inicio = LocalDateTime.now();
    }

    public static void cerrar() {
        actual = null;
        inicio = null;
    }

    public static Usuario actual() {
        return actual;
    }

    public static boolean activa() {
        return actual != null;
    }

    public static LocalDateTime inicio() {
        return inicio;
    }

    /** Nombre para registrar en operaciones (consumos, cambios de motor...). */
    public static String nombreUsuario() {
        return actual == null ? null : actual.getNombreUsuario();
    }
}
