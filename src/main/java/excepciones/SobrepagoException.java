package excepciones;

public class SobrepagoException extends AutoTechException {

    public SobrepagoException(String mensaje) {
        super(mensaje);
    }

    public SobrepagoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
