package servicio;

import dao.MecanicoDAO;
import dao.impl.MecanicoDAOImpl;
import excepciones.DuplicadoException;
import excepciones.MecanicoNoDisponibleException;
import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Especialidad;
import modelo.Mecanico;
import util.Validador;

import java.util.List;
import java.util.Optional;

public class MecanicoService {

    private final MecanicoDAO mecanicoDAO;

    public MecanicoService() {
        this(new MecanicoDAOImpl());
    }

    public MecanicoService(MecanicoDAO mecanicoDAO) {
        this.mecanicoDAO = mecanicoDAO;
    }

    public Mecanico registrarMecanico(Mecanico m, List<Integer> especialidadIds)
            throws ValidacionException, DuplicadoException, PersistenciaException {
        validarMecanico(m);

        if (mecanicoDAO.existeCodigo(m.getCodigoEmpleado(), null)) {
            throw new DuplicadoException("Ya existe un mecánico con el código de empleado: " + m.getCodigoEmpleado());
        }

        if (mecanicoDAO.existeIdentidad(m.getIdentidad(), null)) {
            throw new DuplicadoException("Ya existe un mecánico con la identificación: " + m.getIdentidad());
        }

        int id = mecanicoDAO.insertar(m);
        m.setId(id);

        if (especialidadIds != null && !especialidadIds.isEmpty()) {
            mecanicoDAO.guardarEspecialidades(id, especialidadIds);
            m.setEspecialidades(mecanicoDAO.listarEspecialidadesMecanico(id));
        }

        return m;
    }

    public void actualizarMecanico(Mecanico m, List<Integer> especialidadIds)
            throws ValidacionException, DuplicadoException, PersistenciaException {
        if (m.getId() <= 0) {
            throw new ValidacionException("El mecánico debe tener un ID válido para actualizar.");
        }
        validarMecanico(m);

        if (mecanicoDAO.existeCodigo(m.getCodigoEmpleado(), m.getId())) {
            throw new DuplicadoException("El código de empleado '" + m.getCodigoEmpleado() + "' ya está registrado.");
        }

        if (mecanicoDAO.existeIdentidad(m.getIdentidad(), m.getId())) {
            throw new DuplicadoException("La identificación '" + m.getIdentidad() + "' ya está registrada.");
        }

        mecanicoDAO.actualizar(m);
        mecanicoDAO.guardarEspecialidades(m.getId(), especialidadIds);
    }

    public Mecanico verificarDisponibilidadParaAsignacion(int mecanicoId)
            throws MecanicoNoDisponibleException, PersistenciaException {
        Optional<Mecanico> opt = mecanicoDAO.buscarPorId(mecanicoId);
        if (opt.isEmpty()) {
            throw new MecanicoNoDisponibleException("El mecánico con ID " + mecanicoId + " no existe.");
        }
        Mecanico m = opt.get();
        if (!m.isActivo()) {
            throw new MecanicoNoDisponibleException("El mecánico " + m.getNombreCompleto() + " se encuentra inactivo en el sistema.");
        }
        if (!m.isDisponible()) {
            throw new MecanicoNoDisponibleException("El mecánico " + m.getNombreCompleto() + " no está disponible actualmente (ocupado o en receso).");
        }
        return m;
    }

    public void cambiarDisponibilidad(int mecanicoId, boolean disponible) throws PersistenciaException {
        mecanicoDAO.cambiarDisponibilidad(mecanicoId, disponible);
    }

    public void desactivarMecanico(int mecanicoId) throws PersistenciaException {
        mecanicoDAO.desactivar(mecanicoId);
    }

    public Optional<Mecanico> buscarPorId(int id) throws PersistenciaException {
        return mecanicoDAO.buscarPorId(id);
    }

    public Optional<Mecanico> buscarPorCodigo(String codigo) throws PersistenciaException {
        return mecanicoDAO.buscarPorCodigo(codigo);
    }

    public List<Mecanico> listarTodos() throws PersistenciaException {
        return mecanicoDAO.listar();
    }

    public List<Mecanico> listarActivos() throws PersistenciaException {
        return mecanicoDAO.listarActivos();
    }

    public List<Mecanico> listarDisponibles() throws PersistenciaException {
        return mecanicoDAO.listarDisponibles();
    }

    public List<Especialidad> listarTodasEspecialidades() throws PersistenciaException {
        return mecanicoDAO.listarTodasEspecialidades();
    }

    private void validarMecanico(Mecanico m) throws ValidacionException {
        if (m == null) {
            throw new ValidacionException("El mecánico no puede ser nulo.");
        }
        Validador.requerido(m.getCodigoEmpleado(), "Código de Empleado");
        Validador.requerido(m.getIdentidad(), "Identificación");
        Validador.requerido(m.getNombre(), "Nombre");
        Validador.positivo(m.getTarifaHora(), "Tarifa por hora");
        if (m.getCorreo() != null && !m.getCorreo().isBlank()) {
            Validador.correo(m.getCorreo());
        }
    }
}
