package dao;

import excepciones.PersistenciaException;
import modelo.Repuesto;
import modelo.Servicio;

import java.util.List;
import java.util.Optional;

public interface ServicioDAO extends CrudDAO<Servicio, Integer> {

    Optional<Servicio> buscarPorCodigo(String codigo) throws PersistenciaException;

    List<Servicio> listarPorNivel(int nivel) throws PersistenciaException;

    List<Servicio> listarActivos() throws PersistenciaException;

    List<Repuesto> listarRepuestosAsociados(int servicioId) throws PersistenciaException;

    void asociarRepuesto(int servicioId, int repuestoId, int cantidadSugerida) throws PersistenciaException;

    void desasociarRepuestos(int servicioId) throws PersistenciaException;

    boolean existeCodigo(String codigo, Integer excluirId) throws PersistenciaException;
}
