package dao;

import excepciones.PersistenciaException;
import java.util.List;
import java.util.Optional;
import modelo.ConfiguracionImpuesto;
import modelo.DetallePresupuesto;
import modelo.Presupuesto;
import modelo.TablaResultado;

/** Contrato de persistencia del diagnóstico técnico y del presupuesto (Módulo 6). */
public interface PresupuestoDAO {

    int registrarDiagnostico(int ordenId, int mecanicoId, String descripcion, String causaRaiz,
                             String recomendacion, boolean requiereCambioMotor) throws PersistenciaException;

    int agregarEvidencia(int diagnosticoId, String tipo, String descripcion, String valorMedido) throws PersistenciaException;

    Optional<Integer> ultimoDiagnostico(int ordenId) throws PersistenciaException;

    TablaResultado listarDiagnosticos(int ordenId) throws PersistenciaException;

    TablaResultado listarEvidencias(int ordenId) throws PersistenciaException;

    /** Líneas propuestas a partir de los servicios de la orden y sus repuestos asociados. */
    List<DetallePresupuesto> lineasSugeridas(int ordenId) throws PersistenciaException;

    /** Guarda encabezado y líneas en una transacción. */
    int guardar(int ordenId, Presupuesto p) throws PersistenciaException;

    /** Último presupuesto de la orden: id, estado, subtotal, descuento, impuesto, total. */
    TablaResultado listarPresupuestos(int ordenId) throws PersistenciaException;

    TablaResultado listarLineas(int presupuestoId) throws PersistenciaException;

    Optional<Integer> ultimoPendiente(int ordenId) throws PersistenciaException;

    void responder(int presupuestoId, boolean aprobado, String observacion) throws PersistenciaException;

    List<ConfiguracionImpuesto> listarImpuestos() throws PersistenciaException;
}
