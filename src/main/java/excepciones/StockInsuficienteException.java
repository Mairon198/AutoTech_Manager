package excepciones;

/** No hay existencias suficientes de un repuesto. */
public class StockInsuficienteException extends AutoTechException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }

    public StockInsuficienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
