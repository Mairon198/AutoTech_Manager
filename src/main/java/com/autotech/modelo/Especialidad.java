package com.autotech.modelo;

/** Ej.: Motores, Transmisiones, Sistema eléctrico, Frenos y suspensión. */
public class Especialidad extends EntidadBase {

    private String nombre;
    private String descripcion;

    public Especialidad() {
    }

    public Especialidad(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
