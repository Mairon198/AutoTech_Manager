package excepciones;

public class DuplicadoException extends AutoTechException {

    public DuplicadoException(String mensaje) {
        super(mensaje);
    }

    public DuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
