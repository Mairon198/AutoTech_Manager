package com.autotech.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Repuesto utilizado en una orden. El registro se hace junto con el descuento de inventario en una transacción. */
public class ConsumoRepuesto extends EntidadBase {

    private OrdenTrabajo orden;
    private Repuesto repuesto;
    private int cantidad;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private LocalDateTime fecha;
    private boolean registradoEnInventario;

    public ConsumoRepuesto() {
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

    public Repuesto getRepuesto() {
        return repuesto;
    }

    public void setRepuesto(Repuesto repuesto) {
        this.repuesto = repuesto;
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isRegistradoEnInventario() {
        return registradoEnInventario;
    }

    public void setRegistradoEnInventario(boolean registradoEnInventario) {
        this.registradoEnInventario = registradoEnInventario;
    }
}
