package modelo.enums;

public enum EstadoMotor {
    DISPONIBLE("Disponible"),
    RESERVADO("Reservado"),
    INSTALADO("Instalado"),
    RETIRADO("Retirado");

    private final String descripcion;

    EstadoMotor(String descripcion) {
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
