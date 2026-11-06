package com.autotech.modelo.enums;

/** Categorías del inventario (módulo 5). */
public enum CategoriaRepuesto {
    ACEITE("Aceite"),
    FILTRO("Filtro"),
    BUJIA("Bujía"),
    BATERIA("Batería"),
    FRENOS("Frenos"),
    RADIADOR("Radiador"),
    TRANSMISION("Transmisión"),
    MOTOR("Motor"),
    ELECTRICO("Eléctrico"),
    SUSPENSION("Suspensión"),
    OTRO("Otro");

    private final String descripcion;

    CategoriaRepuesto(String descripcion) {
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
