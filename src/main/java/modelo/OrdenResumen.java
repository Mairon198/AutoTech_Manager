package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import modelo.enums.EstadoOrden;

/** Fila de la vista vw_OrdenesResumen: datos de la orden listos para mostrar en pantalla. */
public class OrdenResumen {

    private int ordenId;
    private String numero;
    private int clienteId;
    private String cliente;
    private int vehiculoId;
    private String placa;
    private String vehiculo;
    private EstadoOrden estado;
    private String responsable;
    private LocalDateTime fechaIngreso;
    private BigDecimal totalManoObra = BigDecimal.ZERO;
    private BigDecimal totalRepuestos = BigDecimal.ZERO;
    private BigDecimal descuento = BigDecimal.ZERO;
    private BigDecimal impuesto = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal totalPagado = BigDecimal.ZERO;
    private BigDecimal saldo = BigDecimal.ZERO;
    private String numeroFactura;
    private boolean requiereCambioMotor;

    public boolean estaCerrada() {
        return estado != null && (estado.esFinal() || estado == EstadoOrden.FINALIZADA);
    }

    @Override
    public String toString() {
        return numero + "  ·  " + placa + "  ·  " + vehiculo + "  ·  " + (estado != null ? estado.getDescripcion() : "");
    }

    public int getOrdenId() { return ordenId; }
    public void setOrdenId(int ordenId) { this.ordenId = ordenId; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public int getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(int vehiculoId) { this.vehiculoId = vehiculoId; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getVehiculo() { return vehiculo; }
    public void setVehiculo(String vehiculo) { this.vehiculo = vehiculo; }
    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public BigDecimal getTotalManoObra() { return totalManoObra; }
    public void setTotalManoObra(BigDecimal v) { this.totalManoObra = v; }
    public BigDecimal getTotalRepuestos() { return totalRepuestos; }
    public void setTotalRepuestos(BigDecimal v) { this.totalRepuestos = v; }
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal v) { this.descuento = v; }
    public BigDecimal getImpuesto() { return impuesto; }
    public void setImpuesto(BigDecimal v) { this.impuesto = v; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal v) { this.total = v; }
    public BigDecimal getTotalPagado() { return totalPagado; }
    public void setTotalPagado(BigDecimal v) { this.totalPagado = v; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal v) { this.saldo = v; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public boolean isRequiereCambioMotor() { return requiereCambioMotor; }
    public void setRequiereCambioMotor(boolean v) { this.requiereCambioMotor = v; }
}
