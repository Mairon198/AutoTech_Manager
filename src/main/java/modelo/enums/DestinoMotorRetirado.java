package modelo.enums;

/** Qué se hace con el motor que se retira del vehículo. */
public enum DestinoMotorRetirado {
    DEVUELTO_CLIENTE("Devuelto al cliente"),
    RECONSTRUCCION("Enviado a reconstrucción"),
    CHATARRA("Desecho / chatarra"),
    ALMACEN("Almacenado en taller");

    private final String descripcion;

    DestinoMotorRetirado(String descripcion) {
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
