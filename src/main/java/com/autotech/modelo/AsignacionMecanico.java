package com.autotech.modelo;

import java.time.LocalDateTime;

/** Mecánico asignado a una orden (tabla puente). Solo uno es el responsable principal. */
public class AsignacionMecanico extends EntidadBase {

    private OrdenTrabajo orden;
    private Mecanico mecanico;
    private boolean responsable;
    private LocalDateTime fechaAsignacion;

    public AsignacionMecanico() {
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public Mecanico getMecanico() {
        return mecanico;
    }

    public void setMecanico(Mecanico mecanico) {
        this.mecanico = mecanico;
    }

    public boolean isResponsable() {
        return responsable;
    }

    public void setResponsable(boolean responsable) {
        this.responsable = responsable;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
}
