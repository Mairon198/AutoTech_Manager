package com.autotech.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Personal técnico del taller. Puede tener varias especialidades. */
public class Mecanico extends Persona {

    private String codigoEmpleado;
    private BigDecimal tarifaHora = BigDecimal.ZERO;
    private boolean disponible = true;
    private List<Especialidad> especialidades = new ArrayList<>();

    @Override
    public String getTipoPersona() {
        return "Mecánico";
    }

    /** Solo se puede asignar a una orden si está activo y disponible. */
    public boolean puedeSerAsignado() {
        return activo && disponible;
    }

    public boolean tieneEspecialidad(String nombre) {
        for (Especialidad e : especialidades) {
            if (e.getNombre().equalsIgnoreCase(nombre)) {
                return true;
            }
        }
        return false;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }

    public BigDecimal getTarifaHora() {
        return tarifaHora;
    }

    public void setTarifaHora(BigDecimal tarifaHora) {
        this.tarifaHora = tarifaHora;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public List<Especialidad> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<Especialidad> especialidades) {
        this.especialidades = especialidades;
    }
}
