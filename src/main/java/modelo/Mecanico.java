package modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Mecanico extends Persona {

    private String codigoEmpleado;
    private String nivelCertificacion = "SENIOR";
    private BigDecimal tarifaHora = BigDecimal.ZERO;
    private boolean disponible = true;
    private List<Especialidad> especialidades = new ArrayList<>();

    public Mecanico() {
    }

    public Mecanico(String codigoEmpleado, String identidad, String nombre, String apellido,
                    String telefono, String correo, String nivelCertificacion, BigDecimal tarifaHora) {
        this.codigoEmpleado = codigoEmpleado;
        this.identidad = identidad;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
        this.nivelCertificacion = (nivelCertificacion != null && !nivelCertificacion.isBlank()) ? nivelCertificacion : "SENIOR";
        this.tarifaHora = tarifaHora != null ? tarifaHora : BigDecimal.ZERO;
    }

    @Override
    public String getTipoPersona() {
        return "Mecánico";
    }

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

    public String getNivelCertificacion() {
        return nivelCertificacion;
    }

    public void setNivelCertificacion(String nivelCertificacion) {
        this.nivelCertificacion = nivelCertificacion;
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
