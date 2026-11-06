package com.autotech.modelo;

import com.autotech.modelo.enums.EstadoActividad;
import java.time.LocalDateTime;

public class ActividadTrabajo extends EntidadBase {

    private OrdenTrabajo orden;
    private Mecanico mecanico;
    private String descripcion;
    private double horas;
    private EstadoActividad estado = EstadoActividad.PENDIENTE;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private String observaciones;

    public ActividadTrabajo() {
    }

    public boolean estaCompletada() {
        return estado == EstadoActividad.COMPLETADA;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getHoras() {
        return horas;
    }

    public void setHoras(double horas) {
        this.horas = horas;
    }

    public EstadoActividad getEstado() {
        return estado;
    }

    public void setEstado(EstadoActividad estado) {
        this.estado = estado;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
