package servicio;

import dao.OrdenTrabajoDAO;
import dao.PagoDAO;
import dao.impl.OrdenTrabajoDAOImpl;
import dao.impl.PagoDAOImpl;
import excepciones.AutoTechException;
import excepciones.MecanicoNoDisponibleException;
import excepciones.PersistenciaException;
import excepciones.ReglaNegocioException;
import excepciones.TransicionEstadoInvalidaException;
import excepciones.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import modelo.Cliente;
import modelo.Mecanico;
import modelo.OrdenResumen;
import modelo.Repuesto;
import modelo.Servicio;
import modelo.ServicioFactory;
import modelo.TablaResultado;
import modelo.Vehiculo;
import modelo.enums.EstadoActividad;
import modelo.enums.EstadoOrden;
import util.Validador;

/** Reglas de negocio de las órdenes de trabajo (Módulos 3 y 4). */
public class OrdenTrabajoService {

    private final OrdenTrabajoDAO ordenDAO;
    private final OrdenTrabajoDAOImpl transacciones;   // para operaciones que comparten una conexión
    private final PagoDAO pagoDAO;

    public OrdenTrabajoService() {
        OrdenTrabajoDAOImpl impl = new OrdenTrabajoDAOImpl();
        this.ordenDAO = impl;
        this.transacciones = impl;
        this.pagoDAO = new PagoDAOImpl();
    }

    // ------------------------------------------------------------ consultas

    public List<OrdenResumen> listar(boolean soloAbiertas) throws PersistenciaException {
        return ordenDAO.listar(soloAbiertas);
    }

    public Optional<OrdenResumen> buscar(int ordenId) throws PersistenciaException {
        return ordenDAO.buscar(ordenId);
    }

    public TablaResultado servicios(int ordenId) throws PersistenciaException { return ordenDAO.listarServicios(ordenId); }
    public TablaResultado repuestos(int ordenId) throws PersistenciaException { return ordenDAO.listarRepuestos(ordenId); }
    public TablaResultado asignaciones(int ordenId) throws PersistenciaException { return ordenDAO.listarAsignaciones(ordenId); }
    public TablaResultado actividades(int ordenId) throws PersistenciaException { return ordenDAO.listarActividades(ordenId); }
    public TablaResultado controles(int ordenId) throws PersistenciaException { return ordenDAO.listarControles(ordenId); }

    // ------------------------------------------------------------ ingreso

    /** Registra el ingreso: orden única con cliente, vehículo, kilometraje, síntomas y responsable. */
    public int crearOrden(Cliente cliente, Vehiculo vehiculo, int kilometraje, String sintomas,
                          String diagnosticoPreliminar, Mecanico responsable) throws AutoTechException {
        if (cliente == null) throw new ValidacionException("Seleccione el cliente.");
        if (vehiculo == null) throw new ValidacionException("Seleccione el vehículo.");
        if (responsable == null) throw new ValidacionException("Seleccione el mecánico responsable.");
        Validador.requerido(sintomas, "Síntomas");
        Validador.noNegativo(kilometraje, "Kilometraje");
        if (kilometraje < vehiculo.getKilometraje()) {
            throw new ValidacionException("El kilometraje de ingreso (" + kilometraje
                    + ") no puede ser menor al último registrado (" + vehiculo.getKilometraje() + ").");
        }
        if (!responsable.puedeSerAsignado()) {
            throw new MecanicoNoDisponibleException("El mecánico " + responsable + " está inactivo o no disponible.");
        }
        try {
            return ordenDAO.crear(cliente.getId(), vehiculo.getId(), kilometraje, sintomas.trim(),
                    diagnosticoPreliminar == null ? "" : diagnosticoPreliminar.trim(), responsable.getId());
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    // ------------------------------------------------------------ servicios y repuestos

    /**
     * Agrega un servicio guardando su precio del momento. El precio lo calcula el propio
     * servicio según su nivel (polimorfismo: calcularCosto() de cada subclase).
     */
    public BigDecimal agregarServicio(OrdenResumen orden, Servicio servicio, int cantidad) throws AutoTechException {
        validarAbierta(orden);
        if (servicio == null) throw new ValidacionException("Seleccione un servicio.");
        Validador.positivo(cantidad, "Cantidad");
        if (orden.getEstado().ordinal() > EstadoOrden.EN_REPARACION.ordinal()) {
            throw new ReglaNegocioException("Ya no se pueden agregar servicios: la orden está en " + orden.getEstado() + ".");
        }
        BigDecimal precio = servicio.calcularCosto();
        try {
            ordenDAO.agregarServicio(orden.getOrdenId(), servicio.getId(), cantidad, precio);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
        return precio;
    }

    public void quitarServicio(OrdenResumen orden, int detalleId) throws AutoTechException {
        validarAbierta(orden);
        if (orden.getEstado().ordinal() >= EstadoOrden.EN_REPARACION.ordinal()) {
            throw new ReglaNegocioException("No se pueden quitar servicios de una orden ya aprobada.");
        }
        try {
            ordenDAO.quitarServicio(detalleId);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    /** Descuenta del inventario de forma atómica (sp_ConsumirRepuesto). Si falla, no queda nada a medias. */
    public void consumirRepuesto(OrdenResumen orden, Repuesto repuesto, int cantidad, String usuario) throws AutoTechException {
        validarAbierta(orden);
        if (repuesto == null) throw new ValidacionException("Seleccione un repuesto.");
        Validador.positivo(cantidad, "Cantidad");
        try {
            // Clave única de la operación: si se reintenta (doble clic, hilo duplicado) no se descuenta dos veces
            ordenDAO.consumirRepuesto(orden.getOrdenId(), repuesto.getId(), cantidad, usuario, UUID.randomUUID().toString());
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    // ------------------------------------------------------------ mecánicos y actividades

    public void asignarMecanico(OrdenResumen orden, Mecanico mecanico, boolean responsable) throws AutoTechException {
        validarAbierta(orden);
        if (mecanico == null) throw new ValidacionException("Seleccione un mecánico.");
        if (!mecanico.puedeSerAsignado()) {
            throw new MecanicoNoDisponibleException("El mecánico " + mecanico + " está inactivo o no disponible.");
        }
        try {
            ordenDAO.asignarMecanico(orden.getOrdenId(), mecanico.getId(), responsable);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    /**
     * Quita un mecánico de apoyo. No se puede quitar al responsable (primero se asigna otro)
     * ni a quien ya tiene actividades registradas en la orden.
     */
    public void quitarMecanico(OrdenResumen orden, int mecanicoId, String nombre) throws AutoTechException {
        validarAbierta(orden);
        if (ordenDAO.esResponsable(orden.getOrdenId(), mecanicoId)) {
            throw new ReglaNegocioException(nombre + " es el responsable de la orden.\n"
                    + "Primero asigne a otro mecánico como responsable y luego quítelo.");
        }
        int actividades = ordenDAO.contarActividades(orden.getOrdenId(), mecanicoId);
        if (actividades > 0) {
            throw new ReglaNegocioException(nombre + " tiene " + actividades
                    + (actividades == 1 ? " actividad registrada" : " actividades registradas")
                    + " en esta orden, por eso no se puede quitar.");
        }
        try {
            if (ordenDAO.quitarAsignacion(orden.getOrdenId(), mecanicoId) == 0) {
                throw new ReglaNegocioException("El mecánico ya no está asignado a la orden.");
            }
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    public int agregarActividad(OrdenResumen orden, Mecanico mecanico, String descripcion) throws AutoTechException {
        validarAbierta(orden);
        if (mecanico == null) throw new ValidacionException("Seleccione el mecánico de la actividad.");
        Validador.requerido(descripcion, "Descripción de la actividad");
        try {
            return ordenDAO.agregarActividad(orden.getOrdenId(), mecanico.getId(), descripcion.trim());
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    /**
     * Quita una actividad agregada por error. Las actividades completadas no se quitan,
     * porque ya forman parte del trabajo realizado y de las horas del mecánico.
     */
    public void quitarActividad(OrdenResumen orden, int actividadId) throws AutoTechException {
        validarAbierta(orden);
        try {
            if (ordenDAO.quitarActividad(orden.getOrdenId(), actividadId) == 0) {
                throw new ReglaNegocioException("Solo se pueden quitar actividades que todavía no estén completadas.");
            }
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    public void iniciarActividad(int actividadId) throws AutoTechException {
        try {
            ordenDAO.actualizarActividad(actividadId, EstadoActividad.EN_PROCESO, null, null);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    public void completarActividad(int actividadId, BigDecimal horas, String observaciones) throws AutoTechException {
        if (horas == null || horas.signum() <= 0) throw new ValidacionException("Indique las horas trabajadas (mayor que cero).");
        try {
            ordenDAO.actualizarActividad(actividadId, EstadoActividad.COMPLETADA, horas, observaciones);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    // ------------------------------------------------------------ calidad, estados y cierre

    public int registrarControl(OrdenResumen orden, Mecanico inspector, boolean aprobado, List<String> verificados,
                                String observaciones) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        if (orden.getEstado() != EstadoOrden.EN_CONTROL_CALIDAD) {
            throw new ReglaNegocioException("La orden debe estar 'En control de calidad' (está en " + orden.getEstado() + ").");
        }
        if (inspector == null) throw new ValidacionException("Seleccione el inspector.");
        if (!aprobado && (observaciones == null || observaciones.isBlank())) {
            throw new ValidacionException("Si el control no se aprueba, explique el motivo en observaciones.");
        }
        String checklist = String.join("; ", verificados);
        try {
            return ordenDAO.registrarControl(orden.getOrdenId(), inspector.getId(), aprobado, checklist, observaciones);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    /** Cambia de estado respetando el flujo (enum EstadoOrden) y las reglas de sp_CambiarEstadoOrden. */
    public void cambiarEstado(OrdenResumen orden, EstadoOrden nuevo) throws AutoTechException {
        if (orden == null || nuevo == null) throw new ValidacionException("Seleccione la orden y el nuevo estado.");
        if (!orden.getEstado().puedeCambiarA(nuevo)) {
            throw new TransicionEstadoInvalidaException("No se permite pasar de " + orden.getEstado() + " a " + nuevo + ".");
        }
        try {
            ordenDAO.cambiarEstado(orden.getOrdenId(), nuevo);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    /** Días de garantía: el mayor que otorgan los servicios de la orden (cada nivel define el suyo). */
    public int diasGarantia(int ordenId) throws PersistenciaException {
        int dias = 0;
        for (int nivel : ordenDAO.nivelesServicios(ordenId)) {
            dias = Math.max(dias, ServicioFactory.crear(nivel).getDiasGarantia());
        }
        return dias;
    }

    /**
     * Cierre de la orden en UNA transacción: genera la garantía (si falta), la pasa a
     * FINALIZADA (sp_CambiarEstadoOrden valida actividades, consumos y control de calidad)
     * y emite la factura (sp_GenerarFactura). Si algo falla, se revierte todo.
     */
    public int cerrarOrden(int ordenId) throws AutoTechException {
        OrdenResumen orden = buscar(ordenId).orElseThrow(() -> new ValidacionException("La orden no existe."));
        if (orden.getEstado() != EstadoOrden.EN_CONTROL_CALIDAD) {
            throw new ReglaNegocioException("Para procesar el cierre la orden debe estar 'En control de calidad' (está en "
                    + orden.getEstado() + ").");
        }
        int dias = diasGarantia(ordenId);
        if (dias == 0) throw new ReglaNegocioException("La orden no tiene servicios registrados.");
        boolean faltaGarantia = !ordenDAO.tieneGarantia(ordenId);
        int km = ordenDAO.kilometrajeIngreso(ordenId);
        try {
            return transacciones.ejecutarEnTransaccion(cn -> {
                if (faltaGarantia) {
                    LocalDate hoy = LocalDate.now();
                    ordenDAO.generarGarantia(cn, ordenId,
                            "Garantía de " + dias + " días por los servicios de la orden " + orden.getNumero(),
                            hoy, hoy.plusDays(dias), km + dias * 100,
                            "Cubre mano de obra y repuestos instalados. No cubre mal uso ni accidentes.");
                }
                ordenDAO.cambiarEstado(cn, ordenId, EstadoOrden.FINALIZADA);
                return pagoDAO.generarFactura(cn, ordenId);
            });
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e.getCause() instanceof PersistenciaException p ? p : e);
        }
    }

    private void validarAbierta(OrdenResumen orden) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        if (orden.estaCerrada()) {
            throw new ReglaNegocioException("La orden " + orden.getNumero() + " está cerrada (" + orden.getEstado() + ").");
        }
    }
}
