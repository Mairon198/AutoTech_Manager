package modelo;

import java.math.BigDecimal;

/** Servicio incluido en una orden. Guarda el precio del momento (histórico) para que los cambios de catálogo no alteren órdenes ya finalizadas. */
public class DetalleOrden extends EntidadBase {

    private OrdenTrabajo orden;
    private Servicio servicio;
    private int cantidad = 1;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private String observacion;

    public DetalleOrden() {
    }

    /** Crea el detalle copiando el costo actual del servicio como precio histórico. */
    public static DetalleOrden desde(Servicio s, int cantidad) {
        DetalleOrden d = new DetalleOrden();
        d.servicio = s;
        d.cantidad = cantidad;
        d.precioUnitario = s.calcularCosto();
        return d;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
