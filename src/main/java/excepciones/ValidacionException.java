package excepciones;

/** Datos inválidos o incompletos (campos vacíos, formatos, negativos). */
public class ValidacionException extends AutoTechException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    public ValidacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
