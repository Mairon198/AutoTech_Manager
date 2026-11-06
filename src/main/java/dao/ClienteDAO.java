package dao;

import excepciones.PersistenciaException;
import modelo.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteDAO extends CrudDAO<Cliente, Integer> {

    Optional<Cliente> buscarPorIdentidad(String identidad) throws PersistenciaException;

    List<Cliente> listarActivos() throws PersistenciaException;

    List<Cliente> buscarPorCriterio(String filtro) throws PersistenciaException;

    boolean existeIdentidad(String identidad, Integer excluirId) throws PersistenciaException;
}
