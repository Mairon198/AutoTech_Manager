package modelo;

import modelo.enums.EstadoOrden;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenTrabajo extends EntidadBase {

    private String numero;
    private Cliente cliente;
    private Vehiculo vehiculo;
    private LocalDateTime fechaIngreso;
    private int kilometrajeIngreso;
    private String sintomas;
    private String diagnosticoPreliminar;
    private EstadoOrden estado = EstadoOrden.RECIBIDA;
    private LocalDateTime fechaEntrega;
    private List<DetalleOrden> detalles = new ArrayList<>();
    private List<AsignacionMecanico> asignaciones = new ArrayList<>();
    private List<ActividadTrabajo> actividades = new ArrayList<>();
    private List<ConsumoRepuesto> consumos = new ArrayList<>();
    private List<ControlCalidad> controles = new ArrayList<>();
    private List<Pago> pagos = new ArrayList<>();
    private Diagnostico diagnostico;
    private Presupuesto presupuesto;
    private BigDecimal total = BigDecimal.ZERO;

    public OrdenTrabajo() {
    }

    public void agregarDetalle(DetalleOrden d) {
        d.setOrden(this);
        detalles.add(d);
    }

    public void agregarActividad(ActividadTrabajo a) {
        a.setOrden(this);
        actividades.add(a);
    }

    public void agregarConsumo(ConsumoRepuesto c) {
        c.setOrden(this);
        consumos.add(c);
    }

    public Mecanico getResponsable() {
        for (AsignacionMecanico a : asignaciones) {
            if (a.isResponsable()) {
                return a.getMecanico();
            }
        }
        return null;
    }

    public BigDecimal getTotalServicios() {
        BigDecimal t = BigDecimal.ZERO;
        for (DetalleOrden d : detalles) {
            t = t.add(d.getSubtotal());
        }
        return t;
    }

    public BigDecimal getTotalRepuestos() {
        BigDecimal t = BigDecimal.ZERO;
        for (ConsumoRepuesto c : consumos) {
            t = t.add(c.getSubtotal());
        }
        return t;
    }

    public BigDecimal getTotalPagado() {
        BigDecimal t = BigDecimal.ZERO;
        for (Pago p : pagos) {
            t = t.add(p.getMonto());
        }
        return t;
    }

    public BigDecimal getSaldo() {
        return total.subtract(getTotalPagado());
    }

    public boolean tieneActividadesPendientes() {
        for (ActividadTrabajo a : actividades) {
            if (!a.estaCompletada()) {
                return true;
            }
        }
        return false;
    }

    public boolean tieneConsumosSinRegistrar() {
        for (ConsumoRepuesto c : consumos) {
            if (!c.isRegistradoEnInventario()) {
                return true;
            }
        }
        return false;
    }

    public boolean requiereControlCalidad() {
        for (DetalleOrden d : detalles) {
            if (d.getServicio().requiereControlCalidad()) {
                return true;
            }
        }
        return false;
    }

    public boolean controlCalidadAprobado() {
        if (controles.isEmpty()) {
            return false;
        }
        return controles.get(controles.size() - 1).isAprobado();
    }

    @Override
    public String toString() {
        return numero + " - " + (vehiculo != null ? vehiculo.getPlaca() : "") + " [" + estado + "]";
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public int getKilometrajeIngreso() {
        return kilometrajeIngreso;
    }

    public void setKilometrajeIngreso(int kilometrajeIngreso) {
        this.kilometrajeIngreso = kilometrajeIngreso;
    }

    public String getSintomas() {
        return sintomas;
    }

    public void setSintomas(String sintomas) {
        this.sintomas = sintomas;
    }

    public String getDiagnosticoPreliminar() {
        return diagnosticoPreliminar;
    }

    public void setDiagnosticoPreliminar(String diagnosticoPreliminar) {
        this.diagnosticoPreliminar = diagnosticoPreliminar;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public List<DetalleOrden> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleOrden> detalles) {
        this.detalles = detalles;
    }

    public List<AsignacionMecanico> getAsignaciones() {
        return asignaciones;
    }

    public void setAsignaciones(List<AsignacionMecanico> asignaciones) {
        this.asignaciones = asignaciones;
    }

    public List<ActividadTrabajo> getActividades() {
        return actividades;
    }

    public void setActividades(List<ActividadTrabajo> actividades) {
        this.actividades = actividades;
    }

    public List<ConsumoRepuesto> getConsumos() {
        return consumos;
    }

    public void setConsumos(List<ConsumoRepuesto> consumos) {
        this.consumos = consumos;
    }

    public List<ControlCalidad> getControles() {
        return controles;
    }

    public void setControles(List<ControlCalidad> controles) {
        this.controles = controles;
    }

    public List<Pago> getPagos() {
        return pagos;
    }

    public void setPagos(List<Pago> pagos) {
        this.pagos = pagos;
    }

    public Diagnostico getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(Diagnostico diagnostico) {
        this.diagnostico = diagnostico;
    }

    public Presupuesto getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(Presupuesto presupuesto) {
        this.presupuesto = presupuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
