package servicio;

import dao.RepuestoDAO;
import dao.ServicioDAO;
import dao.impl.RepuestoDAOImpl;
import dao.impl.ServicioDAOImpl;
import excepciones.DuplicadoException;
import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Repuesto;
import modelo.Servicio;
import util.Validador;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CatalogoService {

    private final ServicioDAO servicioDAO;
    private final RepuestoDAO repuestoDAO;

    public CatalogoService() {
        this(new ServicioDAOImpl(), new RepuestoDAOImpl());
    }

    public CatalogoService(ServicioDAO servicioDAO, RepuestoDAO repuestoDAO) {
        this.servicioDAO = servicioDAO;
        this.repuestoDAO = repuestoDAO;
    }

    public Servicio registrarServicio(Servicio s) throws ValidacionException, DuplicadoException, PersistenciaException {
        validarServicio(s);

        if (servicioDAO.existeCodigo(s.getCodigo(), null)) {
            throw new DuplicadoException("Ya existe un servicio con el código: " + s.getCodigo().toUpperCase().trim());
        }

        int id = servicioDAO.insertar(s);
        s.setId(id);
        return s;
    }

    public void actualizarServicio(Servicio s) throws ValidacionException, DuplicadoException, PersistenciaException {
        if (s.getId() <= 0) {
            throw new ValidacionException("El servicio debe tener un ID válido para actualizar.");
        }
        validarServicio(s);

        if (servicioDAO.existeCodigo(s.getCodigo(), s.getId())) {
            throw new DuplicadoException("El código de servicio '" + s.getCodigo().toUpperCase().trim() + "' ya está registrado.");
        }

        servicioDAO.actualizar(s);
    }

    public void asociarRepuesto(int servicioId, int repuestoId, int cantidadSugerida)
            throws ValidacionException, PersistenciaException {
        if (cantidadSugerida <= 0) {
            throw new ValidacionException("La cantidad sugerida debe ser mayor que cero.");
        }
        servicioDAO.asociarRepuesto(servicioId, repuestoId, cantidadSugerida);
    }

    public void desasociarRepuestos(int servicioId) throws PersistenciaException {
        servicioDAO.desasociarRepuestos(servicioId);
    }

    public void desactivarServicio(int servicioId) throws PersistenciaException {
        servicioDAO.desactivar(servicioId);
    }

    public Optional<Servicio> buscarPorId(int id) throws PersistenciaException {
        return servicioDAO.buscarPorId(id);
    }

    public Optional<Servicio> buscarPorCodigo(String codigo) throws PersistenciaException {
        return servicioDAO.buscarPorCodigo(codigo);
    }

    public List<Servicio> listarTodos() throws PersistenciaException {
        return servicioDAO.listar();
    }

    public List<Servicio> listarActivos() throws PersistenciaException {
        return servicioDAO.listarActivos();
    }

    public List<Servicio> listarPorNivel(int nivel) throws PersistenciaException {
        return servicioDAO.listarPorNivel(nivel);
    }

    public List<Repuesto> listarRepuestosAsociados(int servicioId) throws PersistenciaException {
        return servicioDAO.listarRepuestosAsociados(servicioId);
    }

    public BigDecimal calcularCostoTotal(Servicio s) {
        if (s == null) return BigDecimal.ZERO;
        return s.calcularCosto();
    }

    private void validarServicio(Servicio s) throws ValidacionException {
        if (s == null) {
            throw new ValidacionException("El servicio no puede ser nulo.");
        }
        Validador.requerido(s.getCodigo(), "Código");
        Validador.requerido(s.getNombre(), "Nombre del servicio");
        Validador.positivo(s.getCostoManoObra(), "Costo base de mano de obra");
        if (s.getTiempoEstimadoHoras() <= 0) {
            throw new ValidacionException("El tiempo estimado en horas debe ser mayor a cero.");
        }
        if (s.getNivel() == null) {
            throw new ValidacionException("Debe clasificar el servicio en un nivel de complejidad válido.");
        }
    }
}
