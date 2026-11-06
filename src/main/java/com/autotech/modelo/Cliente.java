package com.autotech.modelo;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Persona {

    private List<Vehiculo> vehiculos = new ArrayList<>();
    private String tipoCliente = "PARTICULAR";

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

    public Cliente(String identidad, String nombre, String apellido, String telefono, String correo, String direccion, String tipoCliente) {
        this(identidad, nombre, apellido, telefono, correo, direccion);
        this.tipoCliente = (tipoCliente != null && !tipoCliente.isBlank()) ? tipoCliente : "PARTICULAR";
    }

    @Override
    public String getTipoPersona() {
        return "Cliente";
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
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
