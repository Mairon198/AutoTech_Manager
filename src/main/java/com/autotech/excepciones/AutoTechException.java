package com.autotech.excepciones;

public class AutoTechException extends Exception {

    public AutoTechException(String mensaje) {
        super(mensaje);
    }

    public AutoTechException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
