package vista;

import java.awt.Component;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Abre los formularios del sistema a partir del nombre de su clase.
 *
 * Así el menú principal no depende directamente de cada formulario: cuando un
 * integrante agrega, por ejemplo, vista.FrmClientes, el botón del menú lo abre
 * sin tener que modificar FrmMenuPrincipal.
 *
 * Reglas para los formularios:
 *  - deben estar en el paquete "vista";
 *  - deben tener un constructor público sin parámetros;
 *  - pueden ser JFrame (lo normal en NetBeans: "JFrame Form"), JDialog o JPanel.
 */
public final class Navegador {

    private static final String PAQUETE = "vista.";

    /** Formularios abiertos: si ya está abierto, se trae al frente en vez de abrir otro. */
    private static final Map<String, java.awt.Window> ABIERTOS = new HashMap<>();

    private Navegador() {
    }

    /** Indica si el formulario ya fue creado por algún integrante. */
    public static boolean existe(String nombreClase) {
        try {
            Class.forName(PAQUETE + nombreClase, false, Navegador.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /** Abre (o trae al frente) el formulario indicado. */
    public static void abrir(Component origen, String nombreClase, String titulo) {
        java.awt.Window abierto = ABIERTOS.get(nombreClase);
        if (abierto != null && abierto.isDisplayable()) {
            abierto.toFront();
            abierto.requestFocus();
            return;
        }
        try {
            Class<?> clase = Class.forName(PAQUETE + nombreClase);
            Object formulario = clase.getDeclaredConstructor().newInstance();
            java.awt.Window ventana = comoVentana(origen, formulario, titulo);
            ventana.setLocationRelativeTo(SwingUtilities.getWindowAncestor(origen));
            ABIERTOS.put(nombreClase, ventana);
            ventana.setVisible(true);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(origen,
                    "El módulo \"" + titulo + "\" todavía no está disponible.\n"
                    + "Falta crear la clase " + PAQUETE + nombreClase + ".",
                    "Módulo pendiente", JOptionPane.INFORMATION_MESSAGE);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            JOptionPane.showMessageDialog(origen,
                    "No se pudo abrir \"" + titulo + "\":\n" + causa.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Convierte el formulario en una ventana que, al cerrarse, no termina la aplicación. */
    private static java.awt.Window comoVentana(Component origen, Object formulario, String titulo) {
        if (formulario instanceof JFrame frame) {
            // Los "JFrame Form" de NetBeans traen EXIT_ON_CLOSE: cerrarían todo el sistema
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            if (frame.getTitle() == null || frame.getTitle().isBlank()) {
                frame.setTitle(titulo);
            }
            return frame;
        }
        if (formulario instanceof JDialog dialog) {
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            return dialog;
        }
        Frame padre = (Frame) SwingUtilities.getWindowAncestor(origen);
        JDialog dialog = new JDialog(padre, titulo, false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (formulario instanceof JInternalFrame interno) {
            dialog.setContentPane(interno.getContentPane());
        } else if (formulario instanceof JPanel panel) {
            dialog.setContentPane(panel);
        } else {
            throw new IllegalArgumentException("El formulario debe ser JFrame, JDialog o JPanel.");
        }
        dialog.pack();
        return dialog;
    }
}
