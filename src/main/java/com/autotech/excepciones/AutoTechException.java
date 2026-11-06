package com.autotech.excepciones;

/** Excepción base del sistema. Todas las demás heredan de ella. */
public class AutoTechException extends Exception {

    public AutoTechException(String mensaje) {
        super(mensaje);
    }

    public AutoTechException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
