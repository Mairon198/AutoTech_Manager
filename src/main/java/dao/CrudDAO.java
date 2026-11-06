package dao;

import excepciones.PersistenciaException;
import java.util.List;
import java.util.Optional;

public interface CrudDAO<T, ID> {

    ID insertar(T entidad) throws PersistenciaException;

    boolean actualizar(T entidad) throws PersistenciaException;

    Optional<T> buscarPorId(ID id) throws PersistenciaException;

    List<T> listar() throws PersistenciaException;

    boolean desactivar(ID id) throws PersistenciaException;
}
