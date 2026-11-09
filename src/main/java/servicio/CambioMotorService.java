package servicio;

import dao.CambioMotorDAO;
import dao.impl.CambioMotorDAOImpl;
import excepciones.AutoTechException;
import excepciones.MotorIncompatibleException;
import excepciones.PersistenciaException;
import excepciones.ReglaNegocioException;
import excepciones.ValidacionException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import modelo.CambioMotor;
import modelo.Motor;
import modelo.OrdenTrabajo;
import modelo.OrdenResumen;
import modelo.TablaResultado;
import modelo.enums.DestinoMotorRetirado;
import modelo.enums.EstadoMotor;
import modelo.enums.EstadoOrden;

/** Reparación mayor: sustitución de motor con trazabilidad (Módulo 7, caso Toyota Hilux 2015). */
public class CambioMotorService {

    /** Los 11 pasos del caso obligatorio, tal como los pide el enunciado. */
    public static final List<String> PASOS = List.of(
            "Registrar diagnóstico y evidencias técnicas",
            "Registrar número de serie del motor anterior",
            "Seleccionar un motor disponible en inventario",
            "Validar compatibilidad con el vehículo",
            "Preparar presupuesto de mano de obra y repuestos",
            "Registrar autorización del cliente",
            "Asignar mecánicos responsables",
            "Registrar desmontaje, instalación y pruebas",
            "Descontar los repuestos consumidos",
            "Registrar serie del motor nuevo, destino del retirado y pruebas",
            "Generar garantía y finalizar tras aprobar control de calidad");

    private final CambioMotorDAO dao = new CambioMotorDAOImpl();

    public List<Motor> motores(int vehiculoId) throws PersistenciaException {
        return dao.listarMotores(vehiculoId);
    }

    public Set<Integer> compatibles(int vehiculoId) throws PersistenciaException {
        return new HashSet<>(dao.compatibles(vehiculoId));
    }

    public String serieActual(int vehiculoId) throws PersistenciaException {
        return dao.serieMotorActual(vehiculoId).orElse("");
    }

    public TablaResultado cambioRegistrado(int ordenId) throws PersistenciaException {
        return dao.consultarCambio(ordenId);
    }

    public boolean[] avance(int ordenId) throws PersistenciaException {
        return dao.avancePasos(ordenId);
    }

    /**
     * Registra desmontaje, instalación y pruebas. Antes de ir a la base se bloquean los motores
     * incompatibles, no disponibles o con datos incompletos, informando la causa; la base vuelve
     * a validar todo dentro de sp_RegistrarCambioMotor.
     */
    public int registrar(OrdenResumen orden, Motor motor, String serieAnterior, DestinoMotorRetirado destino,
                         LocalDateTime desmontaje, LocalDateTime instalacion, String resultadoPruebas,
                         boolean pruebasAprobadas, String observaciones, String usuario) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione la orden.");
        if (orden.getEstado() != EstadoOrden.EN_REPARACION) {
            throw new ReglaNegocioException("El cambio de motor se registra con la orden en reparación (está en "
                    + orden.getEstado() + ").");
        }
        if (motor == null) throw new MotorIncompatibleException("Seleccione el motor nuevo.");
        if (motor.getEstado() != EstadoMotor.DISPONIBLE) {
            throw new MotorIncompatibleException("El motor " + motor.getNumeroSerie() + " no está disponible (estado: "
                    + motor.getEstado() + ").");
        }
        if (!compatibles(orden.getVehiculoId()).contains(motor.getId())) {
            throw new MotorIncompatibleException("El motor " + motor + " no es compatible con " + orden.getVehiculo() + ".");
        }

        OrdenTrabajo o = new OrdenTrabajo();
        o.setId(orden.getOrdenId());
        CambioMotor c = new CambioMotor();
        c.setOrden(o);
        c.setMotorNuevo(motor);
        c.setSerieMotorAnterior(serieAnterior == null ? null : serieAnterior.trim());
        c.setDestinoMotorRetirado(destino);
        c.setFechaDesmontaje(desmontaje);
        c.setFechaInstalacion(instalacion);
        c.setResultadoPruebas(resultadoPruebas == null ? null : resultadoPruebas.trim());
        c.setPruebasAprobadas(pruebasAprobadas);
        c.setAutorizadoCliente(true);
        c.setObservaciones(observaciones);
        if (!c.datosCompletos()) {
            throw new ValidacionException("Faltan datos obligatorios: serie anterior, destino, fechas y resultado de pruebas.");
        }
        if (instalacion.isBefore(desmontaje)) {
            throw new ValidacionException("La instalación no puede ser anterior al desmontaje.");
        }
        if (motor.getNumeroSerie().equalsIgnoreCase(c.getSerieMotorAnterior())) {
            throw new ValidacionException("La serie del motor anterior no puede ser la del motor nuevo.");
        }
        try {
            return dao.registrar(c, usuario);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }
}
