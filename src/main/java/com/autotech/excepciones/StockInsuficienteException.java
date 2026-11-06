package com.autotech.excepciones;

public class StockInsuficienteException extends AutoTechException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }

    public StockInsuficienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
