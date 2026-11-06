package modelo;

import modelo.enums.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entrada, salida o ajuste de un repuesto. Toda salida por orden queda ligada a ella. */
public class MovimientoInventario extends EntidadBase {

    private Repuesto repuesto;
    private TipoMovimiento tipo;
    private int cantidad;
    private BigDecimal costoUnitario = BigDecimal.ZERO;
    private LocalDateTime fecha;
    private String referencia;
    private Integer idOrden;
    private String usuario;

    public MovimientoInventario() {
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
