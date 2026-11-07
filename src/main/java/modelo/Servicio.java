package modelo;

import modelo.enums.NivelComplejidad;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public abstract class Servicio extends EntidadBase {

    protected String codigo;
    protected String nombre;
    protected String descripcion;
    protected BigDecimal costoManoObra = BigDecimal.ZERO;
    protected double tiempoEstimadoHoras;
    protected List<Repuesto> repuestosAsociados = new ArrayList<>();

    public abstract NivelComplejidad getNivel();

    protected abstract BigDecimal getRecargoTecnico();

    public abstract boolean requiereControlCalidad();

    public abstract int getDiasGarantia();

    public boolean requiereDiagnostico() {
        return false;
    }

    public BigDecimal calcularCosto() {
        BigDecimal recargo = costoManoObra.multiply(getRecargoTecnico());
        return costoManoObra.add(recargo).setScale(2, RoundingMode.HALF_UP);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public BigDecimal getCostoManoObra() {
        return costoManoObra;
    }

    public void setCostoManoObra(BigDecimal costoManoObra) {
        this.costoManoObra = costoManoObra;
    }

    public double getTiempoEstimadoHoras() {
        return tiempoEstimadoHoras;
    }

    public void setTiempoEstimadoHoras(double tiempoEstimadoHoras) {
        this.tiempoEstimadoHoras = tiempoEstimadoHoras;
    }

    public List<Repuesto> getRepuestosAsociados() {
        return repuestosAsociados;
    }

    public void setRepuestosAsociados(List<Repuesto> repuestosAsociados) {
        this.repuestosAsociados = repuestosAsociados;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
