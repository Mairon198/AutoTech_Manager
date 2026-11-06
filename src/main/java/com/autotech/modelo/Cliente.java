package com.autotech.modelo;

import java.util.ArrayList;
import java.util.List;

/** Propietario de uno o varios vehículos. */
public class Cliente extends Persona {

    private List<Vehiculo> vehiculos = new ArrayList<>();

    public Cliente() {
    }

    public Cliente(String identidad, String nombre, String apellido, String telefono, String correo, String direccion) {
        this.identidad = identidad;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
    }

    @Override
    public String getTipoPersona() {
        return "Cliente";
    }

    public void agregarVehiculo(Vehiculo v) {
        v.setCliente(this);
        vehiculos.add(v);
    }

    public List<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public void setVehiculos(List<Vehiculo> vehiculos) {
        this.vehiculos = vehiculos;
    }
}
