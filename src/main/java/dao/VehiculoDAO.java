package dao;

import excepciones.PersistenciaException;
import modelo.Vehiculo;

import java.util.List;
import java.util.Optional;

public interface VehiculoDAO extends CrudDAO<Vehiculo, Integer> {

    Optional<Vehiculo> buscarPorPlaca(String placa) throws PersistenciaException;

    Optional<Vehiculo> buscarPorVin(String vin) throws PersistenciaException;

    List<Vehiculo> listarPorCliente(int clienteId) throws PersistenciaException;

    List<Vehiculo> listarActivos() throws PersistenciaException;

    List<Vehiculo> buscarPorCriterio(String filtro) throws PersistenciaException;

    boolean existePlaca(String placa, Integer excluirId) throws PersistenciaException;

    boolean existeVin(String vin, Integer excluirId) throws PersistenciaException;
}
