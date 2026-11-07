package com.autotech.modelo;

import com.autotech.modelo.enums.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoInventario extends EntidadBase {

    private Repuesto repuesto;
    private TipoMovimiento tipo;
    private int cantidad;
    private int stockAnterior;
    private int stockResultante;
    private BigDecimal costoUnitario = BigDecimal.ZERO;
    private LocalDateTime fecha = LocalDateTime.now();
    private String referencia;
    private Integer idOrden;
    private String usuario;

    public MovimientoInventario() {
    }

    public MovimientoInventario(Repuesto repuesto, TipoMovimiento tipo, int cantidad,
                                int stockAnterior, int stockResultante, BigDecimal costoUnitario,
                                String referencia, Integer idOrden, String usuario) {
        this.repuesto = repuesto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockResultante = stockResultante;
        this.costoUnitario = costoUnitario != null ? costoUnitario : BigDecimal.ZERO;
        this.referencia = referencia;
        this.idOrden = idOrden;
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
    }

    public Repuesto getRepuesto() {
        return repuesto;
    }

    public void setRepuesto(Repuesto repuesto) {
        this.repuesto = repuesto;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(int stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public int getStockResultante() {
        return stockResultante;
    }

    public void setStockResultante(int stockResultante) {
        this.stockResultante = stockResultante;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Integer getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Integer idOrden) {
        this.idOrden = idOrden;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
