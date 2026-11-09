package dao;

import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import modelo.OrdenResumen;
import modelo.TablaResultado;
import modelo.enums.EstadoActividad;
import modelo.enums.EstadoOrden;

/** Contrato de persistencia de las órdenes de trabajo y todo lo que cuelga de ellas. */
public interface OrdenTrabajoDAO {

    // ---- encabezado ----
    int crear(int clienteId, int vehiculoId, int kilometraje, String sintomas, String diagnosticoPreliminar,
              int responsableId) throws PersistenciaException;

    List<OrdenResumen> listar(boolean soloAbiertas) throws PersistenciaException;

    Optional<OrdenResumen> buscar(int ordenId) throws PersistenciaException;

    int kilometrajeIngreso(int ordenId) throws PersistenciaException;

    /** Cambia el estado con sp_CambiarEstadoOrden (valida flujo y reglas de finalización). */
    void cambiarEstado(int ordenId, EstadoOrden nuevo) throws PersistenciaException;

    void cambiarEstado(Connection cn, int ordenId, EstadoOrden nuevo) throws PersistenciaException;

    // ---- servicios (DetalleOrden) ----
    void agregarServicio(int ordenId, int servicioId, int cantidad, BigDecimal precioHistorico) throws PersistenciaException;

    void quitarServicio(int detalleId) throws PersistenciaException;

    TablaResultado listarServicios(int ordenId) throws PersistenciaException;

    /** Niveles de complejidad de los servicios de la orden (para la garantía). */
    List<Integer> nivelesServicios(int ordenId) throws PersistenciaException;

    // ---- repuestos ----
    /** Descuenta inventario con sp_ConsumirRepuesto (transacción + control de concurrencia). */
    void consumirRepuesto(int ordenId, int repuestoId, int cantidad, String usuario, String claveOperacion)
            throws PersistenciaException;

    TablaResultado listarRepuestos(int ordenId) throws PersistenciaException;

    // ---- mecánicos y actividades ----
    void asignarMecanico(int ordenId, int mecanicoId, boolean responsable) throws PersistenciaException;

    TablaResultado listarAsignaciones(int ordenId) throws PersistenciaException;

    boolean esResponsable(int ordenId, int mecanicoId) throws PersistenciaException;

    int contarActividades(int ordenId, int mecanicoId) throws PersistenciaException;

    /** Quita un mecánico de apoyo de la orden. Devuelve las filas eliminadas. */
    int quitarAsignacion(int ordenId, int mecanicoId) throws PersistenciaException;

    int agregarActividad(int ordenId, int mecanicoId, String descripcion) throws PersistenciaException;

    /** Quita una actividad que todavía no está completada. Devuelve las filas eliminadas. */
    int quitarActividad(int ordenId, int actividadId) throws PersistenciaException;

    void actualizarActividad(int actividadId, EstadoActividad estado, BigDecimal horas, String observaciones)
            throws PersistenciaException;

    TablaResultado listarActividades(int ordenId) throws PersistenciaException;

    // ---- control de calidad y garantía ----
    int registrarControl(int ordenId, int inspectorId, boolean aprobado, String checklist, String observaciones)
            throws PersistenciaException;

    TablaResultado listarControles(int ordenId) throws PersistenciaException;

    boolean tieneGarantia(int ordenId) throws PersistenciaException;

    int generarGarantia(Connection cn, int ordenId, String descripcion, LocalDate inicio, LocalDate fin,
                        Integer kilometrajeLimite, String condiciones) throws PersistenciaException;
}
