package servicio;

import dao.VehiculoDAO;
import dao.impl.VehiculoDAOImpl;
import excepciones.DuplicadoException;
import excepciones.PersistenciaException;
import excepciones.ValidacionException;
import modelo.Vehiculo;
import util.Validador;

import java.util.List;
import java.util.Optional;

public class VehiculoService {

    private final VehiculoDAO vehiculoDAO;

    public VehiculoService() {
        this(new VehiculoDAOImpl());
    }

    public VehiculoService(VehiculoDAO vehiculoDAO) {
        this.vehiculoDAO = vehiculoDAO;
    }

    public Vehiculo registrarVehiculo(Vehiculo v) throws ValidacionException, DuplicadoException, PersistenciaException {
        validarVehiculo(v);

        if (vehiculoDAO.existePlaca(v.getPlaca(), null)) {
            throw new DuplicadoException("Ya existe un vehículo con la placa: " + v.getPlaca().toUpperCase().trim());
        }

        if (vehiculoDAO.existeVin(v.getVin(), null)) {
            throw new DuplicadoException("Ya existe un vehículo con el VIN: " + v.getVin().toUpperCase().trim());
        }

        int id = vehiculoDAO.insertar(v);
        v.setId(id);
        return v;
    }

    public void actualizarVehiculo(Vehiculo v) throws ValidacionException, DuplicadoException, PersistenciaException {
        if (v.getId() <= 0) {
            throw new ValidacionException("El vehículo debe tener un ID válido para actualizar.");
        }
        validarVehiculo(v);

        if (vehiculoDAO.existePlaca(v.getPlaca(), v.getId())) {
            throw new DuplicadoException("La placa '" + v.getPlaca().toUpperCase().trim() + "' ya pertenece a otro vehículo.");
        }

        if (vehiculoDAO.existeVin(v.getVin(), v.getId())) {
            throw new DuplicadoException("El VIN '" + v.getVin().toUpperCase().trim() + "' ya pertenece a otro vehículo.");
        }

        vehiculoDAO.actualizar(v);
    }

    public void desactivarVehiculo(int vehiculoId) throws PersistenciaException {
        vehiculoDAO.desactivar(vehiculoId);
    }

    public Optional<Vehiculo> buscarPorId(int id) throws PersistenciaException {
        return vehiculoDAO.buscarPorId(id);
    }

    public Optional<Vehiculo> buscarPorPlaca(String placa) throws PersistenciaException {
        return vehiculoDAO.buscarPorPlaca(placa);
    }

    public List<Vehiculo> listarTodos() throws PersistenciaException {
        return vehiculoDAO.listar();
    }

    public List<Vehiculo> listarActivos() throws PersistenciaException {
        return vehiculoDAO.listarActivos();
    }

    public List<Vehiculo> listarPorCliente(int clienteId) throws PersistenciaException {
        return vehiculoDAO.listarPorCliente(clienteId);
    }

    public List<Vehiculo> buscar(String filtro) throws PersistenciaException {
        return vehiculoDAO.buscarPorCriterio(filtro);
    }

    private void validarVehiculo(Vehiculo v) throws ValidacionException {
        if (v == null) {
            throw new ValidacionException("El vehículo no puede ser nulo.");
        }
        if (v.getCliente() == null || v.getCliente().getId() <= 0) {
            throw new ValidacionException("Debe seleccionar un cliente propietario para el vehículo.");
        }
        Validador.requerido(v.getPlaca(), "Placa");
        v.setPlaca(Validador.normalizarPlaca(v.getPlaca()));   // "abc1234" -> "ABC 1234"
        Validador.placa(v.getPlaca());
        v.setVin(Validador.normalizarVin(v.getVin()));
        Validador.requerido(v.getMarca(), "Marca");
        Validador.requerido(v.getModelo(), "Modelo");
        Validador.vin(v.getVin());
        Validador.anioVehiculo(v.getAnio());
        Validador.noNegativo(v.getKilometraje(), "Kilometraje");
    }
}
