package com.autotech.modelo;

import java.math.BigDecimal;

/** Impuesto configurable (ej.: ISV 15 %). */
public class ConfiguracionImpuesto extends EntidadBase {

    private String nombre;
    private BigDecimal porcentaje = BigDecimal.ZERO;

    public ConfiguracionImpuesto() {
    }

    /** Porcentaje expresado como fracción: 15 -> 0.15 */
    public BigDecimal getFactor() {
        return porcentaje.divide(new BigDecimal("100"));
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }
}
