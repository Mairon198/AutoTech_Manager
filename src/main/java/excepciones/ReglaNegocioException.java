package excepciones;

/** Se intentó una operación que viola una regla del taller. */
public class ReglaNegocioException extends AutoTechException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
