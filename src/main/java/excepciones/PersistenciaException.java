package excepciones;

/** Errores de acceso a la base de datos (JDBC / SQL Server). */
public class PersistenciaException extends AutoTechException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
