package com.autotech.modelo.enums;

/** Tipo de combustible del vehículo. */
public enum TipoCombustible {
    GASOLINA("Gasolina"),
    DIESEL("Diésel"),
    HIBRIDO("Híbrido"),
    ELECTRICO("Eléctrico"),
    GLP("Gas LP");

    private final String descripcion;

    TipoCombustible(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
