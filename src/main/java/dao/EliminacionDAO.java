package dao;

import excepciones.PersistenciaException;
import java.util.List;
import modelo.enums.RegistroEliminable;

/** Contrato para eliminar registros dejando constancia en la auditoría. */
public interface EliminacionDAO {

    /**
     * Borra el registro (y sus tablas puente) en una transacción. Si otra tabla lo usa,
     * no se borra nada y devuelve la tabla que lo impide.
     */
    String eliminar(RegistroEliminable registro, int id, String usuario) throws PersistenciaException;

    /** Motivos por los que una orden no se puede eliminar (vacío si se puede). */
    List<String> impedimentosOrden(int ordenId) throws PersistenciaException;

    /** Elimina la orden del sistema (deja de aparecer en listas, vistas y reportes). */
    void eliminarOrden(int ordenId, String numero, String motivo, String usuario) throws PersistenciaException;
}
