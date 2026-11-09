package dao;

import excepciones.PersistenciaException;
import modelo.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorDAO extends CrudDAO<Proveedor, Integer> {

    Optional<Proveedor> buscarPorRtn(String rtn) throws PersistenciaException;

    List<Proveedor> listarActivos() throws PersistenciaException;

    List<Proveedor> buscarPorCriterio(String filtro) throws PersistenciaException;

    boolean existeRtn(String rtn, Integer excluirId) throws PersistenciaException;
}
