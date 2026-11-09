package servicio;

import dao.ClienteDAO;
import dao.impl.ClienteDAOImpl;
import excepciones.DuplicadoException;
import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Cliente;
import util.Validador;

import java.util.List;
import java.util.Optional;

public class ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteService() {
        this(new ClienteDAOImpl());
    }

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public Cliente registrarCliente(Cliente c) throws ValidacionException, DuplicadoException, PersistenciaException {
        validarCliente(c);

        if (clienteDAO.existeIdentidad(c.getIdentidad(), null)) {
            throw new DuplicadoException("Ya existe un cliente con el número de identidad / RTN: " + c.getIdentidad());
        }

        int id = clienteDAO.insertar(c);
        c.setId(id);
        return c;
    }

    public void actualizarCliente(Cliente c) throws ValidacionException, DuplicadoException, PersistenciaException {
        if (c.getId() <= 0) {
            throw new ValidacionException("El cliente debe tener un ID válido para actualizar.");
        }
        validarCliente(c);

        if (clienteDAO.existeIdentidad(c.getIdentidad(), c.getId())) {
            throw new DuplicadoException("La identidad / RTN '" + c.getIdentidad() + "' ya pertenece a otro cliente.");
        }

        clienteDAO.actualizar(c);
    }

    public void desactivarCliente(int clienteId) throws PersistenciaException {
        clienteDAO.desactivar(clienteId);
    }

    public Optional<Cliente> buscarPorId(int id) throws PersistenciaException {
        return clienteDAO.buscarPorId(id);
    }

    public Optional<Cliente> buscarPorIdentidad(String identidad) throws PersistenciaException {
        return clienteDAO.buscarPorIdentidad(identidad);
    }

    public List<Cliente> listarTodos() throws PersistenciaException {
        return clienteDAO.listar();
    }

    public List<Cliente> listarActivos() throws PersistenciaException {
        return clienteDAO.listarActivos();
    }

    public List<Cliente> buscar(String filtro) throws PersistenciaException {
        return clienteDAO.buscarPorCriterio(filtro);
    }

    private void validarCliente(Cliente c) throws ValidacionException {
        if (c == null) {
            throw new ValidacionException("El cliente no puede ser nulo.");
        }
        Validador.requerido(c.getIdentidad(), "Identificación");
        Validador.requerido(c.getNombre(), "Nombre");
        if (c.getCorreo() != null && !c.getCorreo().isBlank()) {
            Validador.correo(c.getCorreo());
        }
    }
}
