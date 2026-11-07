package excepciones;

public class MotorIncompatibleException extends AutoTechException {

    public MotorIncompatibleException(String mensaje) {
        super(mensaje);
    }

    public MotorIncompatibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
