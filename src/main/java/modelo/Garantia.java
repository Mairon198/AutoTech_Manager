package modelo;

import java.time.LocalDate;

/** Garantía emitida al finalizar una orden. */
public class Garantia extends EntidadBase {

    private OrdenTrabajo orden;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int kilometrajeLimite;
    private String condiciones;

    public Garantia() {
    }

    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getKilometrajeLimite() {
        return kilometrajeLimite;
    }

    public void setKilometrajeLimite(int kilometrajeLimite) {
        this.kilometrajeLimite = kilometrajeLimite;
    }

    public String getCondiciones() {
        return condiciones;
    }

    public void setCondiciones(String condiciones) {
        this.condiciones = condiciones;
    }
}
