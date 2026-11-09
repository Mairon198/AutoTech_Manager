package modelo;

import modelo.enums.DestinoMotorRetirado;
import java.time.LocalDateTime;

public class CambioMotor extends EntidadBase {

    private OrdenTrabajo orden;
    private Vehiculo vehiculo;
    private String serieMotorAnterior;
    private Motor motorNuevo;
    private DestinoMotorRetirado destinoMotorRetirado;
    private LocalDateTime fechaDesmontaje;
    private LocalDateTime fechaInstalacion;
    private String resultadoPruebas;
    private boolean pruebasAprobadas;
    private boolean autorizadoCliente;
    private String observaciones;

    public CambioMotor() {
    }

    public boolean datosCompletos() {
        return serieMotorAnterior != null && !serieMotorAnterior.isBlank()
                && motorNuevo != null
                && destinoMotorRetirado != null
                && fechaDesmontaje != null && fechaInstalacion != null
                && resultadoPruebas != null && !resultadoPruebas.isBlank();
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getSerieMotorAnterior() {
        return serieMotorAnterior;
    }

    public void setSerieMotorAnterior(String serieMotorAnterior) {
        this.serieMotorAnterior = serieMotorAnterior;
    }

    public Motor getMotorNuevo() {
        return motorNuevo;
    }

    public void setMotorNuevo(Motor motorNuevo) {
        this.motorNuevo = motorNuevo;
    }

    public DestinoMotorRetirado getDestinoMotorRetirado() {
        return destinoMotorRetirado;
    }

    public void setDestinoMotorRetirado(DestinoMotorRetirado destinoMotorRetirado) {
        this.destinoMotorRetirado = destinoMotorRetirado;
    }

    public LocalDateTime getFechaDesmontaje() {
        return fechaDesmontaje;
    }

    public void setFechaDesmontaje(LocalDateTime fechaDesmontaje) {
        this.fechaDesmontaje = fechaDesmontaje;
    }

    public LocalDateTime getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(LocalDateTime fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }

    public String getResultadoPruebas() {
        return resultadoPruebas;
    }

    public void setResultadoPruebas(String resultadoPruebas) {
        this.resultadoPruebas = resultadoPruebas;
    }

    public boolean isPruebasAprobadas() {
        return pruebasAprobadas;
    }

    public void setPruebasAprobadas(boolean pruebasAprobadas) {
        this.pruebasAprobadas = pruebasAprobadas;
    }

    public boolean isAutorizadoCliente() {
        return autorizadoCliente;
    }

    public void setAutorizadoCliente(boolean autorizadoCliente) {
        this.autorizadoCliente = autorizadoCliente;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
