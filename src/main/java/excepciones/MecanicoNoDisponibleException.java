package excepciones;

/** El mecánico está inactivo o no está disponible. */
public class MecanicoNoDisponibleException extends AutoTechException {

    public MecanicoNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public MecanicoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
