package dao;

import excepciones.PersistenciaException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico de persistencia. Cada DAO específico lo extiende.
 * @param <T>  entidad
 * @param <ID> tipo de la clave primaria
 */
public interface CrudDAO<T, ID> {

    /** Inserta y devuelve el id generado. */
    ID insertar(T entidad) throws PersistenciaException;

    boolean actualizar(T entidad) throws PersistenciaException;

    Optional<T> buscarPorId(ID id) throws PersistenciaException;

    List<T> listar() throws PersistenciaException;

    /** Borrado lógico (activo = 0). No se eliminan registros con historial. */
    boolean desactivar(ID id) throws PersistenciaException;
}
