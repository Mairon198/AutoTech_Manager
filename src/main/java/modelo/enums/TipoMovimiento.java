package modelo.enums;

public enum TipoMovimiento {
    ENTRADA("Entrada"),
    SALIDA("Salida"),
    AJUSTE("Ajuste");

    private final String descripcion;

    TipoMovimiento(String descripcion) {
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
