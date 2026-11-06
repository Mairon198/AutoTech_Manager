package com.autotech.modelo;

import com.autotech.modelo.enums.NivelComplejidad;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio del catálogo. Clase abstracta: cada nivel de complejidad es una
 * subclase con sus propias reglas (polimorfismo).
 */
public abstract class Servicio extends EntidadBase {

    protected String codigo;
    protected String nombre;
    protected String descripcion;
    protected BigDecimal costoManoObra = BigDecimal.ZERO;
    protected double tiempoEstimadoHoras;
    protected List<Repuesto> repuestosAsociados = new ArrayList<>();

    /** Nivel de complejidad de este tipo de servicio. */
    public abstract NivelComplejidad getNivel();

    /** Porcentaje que se suma a la mano de obra por equipo o riesgo técnico (0.10 = 10 %). */
    protected abstract BigDecimal getRecargoTecnico();

    /** Si la orden debe pasar por control de calidad obligatorio antes de finalizar. */
    public abstract boolean requiereControlCalidad();

    /** Días de garantía que se dan al cliente por este servicio. */
    public abstract int getDiasGarantia();

    /** Si necesita diagnóstico técnico registrado antes de presupuestar. */
    public boolean requiereDiagnostico() {
        return false;
    }

    /** Costo de mano de obra final = costo base + recargo técnico del nivel. */
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
