package excepciones;

public class TransicionEstadoInvalidaException extends AutoTechException {

    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje);
    }

    public TransicionEstadoInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
