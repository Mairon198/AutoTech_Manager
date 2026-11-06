package com.autotech.excepciones;

/** Cambio de estado de orden no permitido por el flujo. */
public class TransicionEstadoInvalidaException extends AutoTechException {

    public TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje);
    }

    public TransicionEstadoInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
