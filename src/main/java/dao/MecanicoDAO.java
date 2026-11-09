package dao;

import excepciones.PersistenciaException;
import modelo.Especialidad;
import modelo.Mecanico;

import java.util.List;
import java.util.Optional;

public interface MecanicoDAO extends CrudDAO<Mecanico, Integer> {

    Optional<Mecanico> buscarPorCodigo(String codigoEmpleado) throws PersistenciaException;

    Optional<Mecanico> buscarPorIdentidad(String identidad) throws PersistenciaException;

    List<Mecanico> listarDisponibles() throws PersistenciaException;

    List<Mecanico> listarActivos() throws PersistenciaException;

    boolean cambiarDisponibilidad(int mecanicoId, boolean disponible) throws PersistenciaException;

    List<Especialidad> listarEspecialidadesMecanico(int mecanicoId) throws PersistenciaException;

    void guardarEspecialidades(int mecanicoId, List<Integer> especialidadIds) throws PersistenciaException;

    List<Especialidad> listarTodasEspecialidades() throws PersistenciaException;

    boolean existeCodigo(String codigoEmpleado, Integer excluirId) throws PersistenciaException;

    boolean existeIdentidad(String identidad, Integer excluirId) throws PersistenciaException;
}
