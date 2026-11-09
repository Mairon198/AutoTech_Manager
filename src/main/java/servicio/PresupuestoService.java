package servicio;

import dao.PresupuestoDAO;
import dao.impl.PresupuestoDAOImpl;
import excepciones.AutoTechException;
import excepciones.PersistenciaException;
import excepciones.ReglaNegocioException;
import excepciones.ValidacionException;
import java.math.BigDecimal;
import java.util.List;
import modelo.ConfiguracionImpuesto;
import modelo.DetallePresupuesto;
import modelo.Mecanico;
import modelo.OrdenResumen;
import modelo.Presupuesto;
import modelo.TablaResultado;
import modelo.enums.EstadoOrden;
import modelo.enums.EstadoPresupuesto;
import util.Validador;

/** Diagnóstico técnico y presupuesto con aprobación del cliente (Módulo 6). */
public class PresupuestoService {

    private final PresupuestoDAO dao = new PresupuestoDAOImpl();

    // ------------------------------------------------------------ diagnóstico

    public int registrarDiagnostico(OrdenResumen orden, Mecanico mecanico, String descripcion, String causaRaiz,
                                    String recomendacion, boolean requiereCambioMotor) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        if (orden.estaCerrada()) throw new ReglaNegocioException("La orden está cerrada.");
        if (mecanico == null) throw new ValidacionException("Seleccione el mecánico que diagnostica.");
        Validador.requerido(descripcion, "Descripción del diagnóstico");
        try {
            return dao.registrarDiagnostico(orden.getOrdenId(), mecanico.getId(), descripcion.trim(), causaRaiz,
                    recomendacion, requiereCambioMotor);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    public void agregarEvidencia(OrdenResumen orden, String tipo, String descripcion, String valorMedido) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        Validador.requerido(tipo, "Tipo de evidencia");
        Validador.requerido(descripcion, "Descripción de la evidencia");
        int diagnosticoId = dao.ultimoDiagnostico(orden.getOrdenId())
                .orElseThrow(() -> new ReglaNegocioException("Primero registre el diagnóstico de la orden."));
        try {
            dao.agregarEvidencia(diagnosticoId, tipo.trim(), descripcion.trim(), valorMedido);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    public TablaResultado diagnosticos(int ordenId) throws PersistenciaException { return dao.listarDiagnosticos(ordenId); }
    public TablaResultado evidencias(int ordenId) throws PersistenciaException { return dao.listarEvidencias(ordenId); }

    // ------------------------------------------------------------ presupuesto

    public List<ConfiguracionImpuesto> impuestos() throws PersistenciaException {
        return dao.listarImpuestos();
    }

    /** Arma el presupuesto desglosado: mano de obra (servicios y horas) + repuestos asociados. */
    public Presupuesto preparar(OrdenResumen orden, ConfiguracionImpuesto impuesto, BigDecimal descuento,
                                String autorizadoPor) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        Presupuesto p = new Presupuesto();
        for (DetallePresupuesto d : dao.lineasSugeridas(orden.getOrdenId())) {
            p.agregarDetalle(d);
        }
        p.setImpuesto(impuesto);
        p.setDescuento(descuento == null ? BigDecimal.ZERO : descuento);
        p.setAutorizadoDescuentoPor(autorizadoPor);
        validar(p);
        return p;
    }

    /** Reglas del presupuesto: líneas válidas y descuento solo si alguien lo autorizó. */
    public void validar(Presupuesto p) throws ReglaNegocioException {
        if (p == null || p.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("La orden no tiene servicios.\nAgréguelos en Órdenes de trabajo → pestaña \"2. Servicios\" y vuelva a generar la propuesta.");
        }
        if (p.getDescuento() == null || p.getDescuento().signum() < 0 || p.getDescuento().compareTo(p.getSubtotal()) > 0) {
            throw new ReglaNegocioException("El descuento debe estar entre cero y el subtotal.");
        }
        if (p.getDescuento().signum() > 0 && (p.getAutorizadoDescuentoPor() == null || p.getAutorizadoDescuentoPor().isBlank())) {
            throw new ReglaNegocioException("Debe indicar quién autorizó el descuento.");
        }
        for (DetallePresupuesto d : p.getDetalles()) {
            if (d.getCantidad() <= 0 || d.getPrecioUnitario() == null || d.getPrecioUnitario().signum() < 0) {
                throw new ReglaNegocioException("Revise cantidades y precios del presupuesto.");
            }
        }
    }

    public int guardar(OrdenResumen orden, Presupuesto p) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        if (orden.getEstado() != EstadoOrden.EN_DIAGNOSTICO && orden.getEstado() != EstadoOrden.PENDIENTE_APROBACION) {
            throw new ReglaNegocioException("El presupuesto se prepara con la orden en diagnóstico o pendiente de aprobación.");
        }
        validar(p);
        if (dao.ultimoPendiente(orden.getOrdenId()).isPresent()) {
            throw new ReglaNegocioException("Ya hay un presupuesto pendiente de respuesta para esta orden.");
        }
        try {
            return dao.guardar(orden.getOrdenId(), p);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    /** Registra la aprobación o el rechazo del cliente sobre el último presupuesto pendiente. */
    public EstadoPresupuesto responder(OrdenResumen orden, boolean aprobado, String observacion) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        if (!aprobado && (observacion == null || observacion.isBlank())) {
            throw new ValidacionException("Anote el motivo del rechazo.");
        }
        int id = dao.ultimoPendiente(orden.getOrdenId())
                .orElseThrow(() -> new ReglaNegocioException("No hay un presupuesto pendiente de respuesta."));
        try {
            dao.responder(id, aprobado, observacion);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
        return aprobado ? EstadoPresupuesto.APROBADO : EstadoPresupuesto.RECHAZADO;
    }

    public TablaResultado presupuestos(int ordenId) throws PersistenciaException { return dao.listarPresupuestos(ordenId); }
    public TablaResultado lineas(int presupuestoId) throws PersistenciaException { return dao.listarLineas(presupuestoId); }
}
