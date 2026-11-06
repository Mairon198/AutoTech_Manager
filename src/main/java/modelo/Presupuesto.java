package modelo;

import modelo.enums.EstadoPresupuesto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Cotización desglosada por servicios, horas, repuestos, descuento autorizado e impuesto. */
public class Presupuesto extends EntidadBase {

    private OrdenTrabajo orden;
    private LocalDateTime fecha;
    private List<DetallePresupuesto> detalles = new ArrayList<>();
    private BigDecimal descuento = BigDecimal.ZERO;
    private String autorizadoDescuentoPor;
    private ConfiguracionImpuesto impuesto;
    private EstadoPresupuesto estado = EstadoPresupuesto.PENDIENTE;
    private LocalDateTime fechaRespuesta;
    private String observacionCliente;

    public Presupuesto() {
    }

    public void agregarDetalle(DetallePresupuesto d) {
        d.setPresupuesto(this);
        detalles.add(d);
    }

    public BigDecimal getSubtotalManoObra() {
        return sumar(DetallePresupuesto.MANO_OBRA);
    }

    public BigDecimal getSubtotalRepuestos() {
        return sumar(DetallePresupuesto.REPUESTO);
    }

    public BigDecimal getSubtotal() {
        return getSubtotalManoObra().add(getSubtotalRepuestos());
    }

    public double getTotalHoras() {
        double h = 0;
        for (DetallePresupuesto d : detalles) {
            h += d.getHoras();
        }
        return h;
    }

    /** Impuesto sobre (subtotal - descuento). */
    public BigDecimal getMontoImpuesto() {
        if (impuesto == null) {
            return BigDecimal.ZERO;
        }
        return getSubtotal().subtract(descuento).multiply(impuesto.getFactor()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return getSubtotal().subtract(descuento).add(getMontoImpuesto()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumar(String tipo) {
        BigDecimal s = BigDecimal.ZERO;
        for (DetallePresupuesto d : detalles) {
            if (tipo.equals(d.getTipoLinea())) {
                s = s.add(d.getSubtotal());
            }
        }
        return s;
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<DetallePresupuesto> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePresupuesto> detalles) {
        this.detalles = detalles;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public String getAutorizadoDescuentoPor() {
        return autorizadoDescuentoPor;
    }

    public void setAutorizadoDescuentoPor(String autorizadoDescuentoPor) {
        this.autorizadoDescuentoPor = autorizadoDescuentoPor;
    }

    public ConfiguracionImpuesto getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(ConfiguracionImpuesto impuesto) {
        this.impuesto = impuesto;
    }

    public EstadoPresupuesto getEstado() {
        return estado;
    }

    public void setEstado(EstadoPresupuesto estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public String getObservacionCliente() {
        return observacionCliente;
    }

    public void setObservacionCliente(String observacionCliente) {
        this.observacionCliente = observacionCliente;
    }
}
