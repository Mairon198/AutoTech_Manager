package vista;

import excepciones.AutoTechException;
import java.awt.Component;
import javax.swing.JOptionPane;
import modelo.enums.RegistroEliminable;
import servicio.EliminacionService;

/** Diálogos comunes para eliminar registros desde cualquier módulo. */
public final class Eliminador {

    private static final EliminacionService SERVICIO = new EliminacionService();

    private Eliminador() {
    }

    /**
     * Pide confirmación y elimina el registro cuyo id está en el campo ID del formulario.
     * Si tiene historial, explica por qué no se puede y sugiere desactivarlo.
     */
    public static void eliminar(Component padre, RegistroEliminable registro, String idTexto, Runnable alTerminar) {
        String txt = idTexto == null ? "" : idTexto.trim();
        if (txt.isEmpty()) {
            JOptionPane.showMessageDialog(padre, "Seleccione " + registro.getDescripcion() + " en la tabla para eliminarlo.",
                    "Eliminar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id;
        try {
            id = Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(padre,
                "¿Eliminar definitivamente " + registro.getDescripcion() + " seleccionado?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            SERVICIO.eliminar(registro, id);
            String quien = registro.getDescripcion();
            JOptionPane.showMessageDialog(padre, Character.toUpperCase(quien.charAt(0)) + quien.substring(1) + " se eliminó correctamente.",
                    "Eliminar", JOptionPane.INFORMATION_MESSAGE);
            if (alTerminar != null) {
                alTerminar.run();
            }
        } catch (AutoTechException e) {
            JOptionPane.showMessageDialog(padre, e.getMessage(), "No se puede eliminar", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(padre, "Error al eliminar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
