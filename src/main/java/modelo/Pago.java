package modelo;

import modelo.enums.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago extends EntidadBase {

    private OrdenTrabajo orden;
    private BigDecimal monto = BigDecimal.ZERO;
    private MetodoPago metodo;
    private LocalDateTime fecha;
    private String referencia;

    public Pago() {
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
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
}
