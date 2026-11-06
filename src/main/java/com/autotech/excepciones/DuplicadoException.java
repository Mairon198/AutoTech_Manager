package com.autotech.excepciones;

/** Ya existe un registro con ese dato único (placa, VIN, identidad, código). */
public class DuplicadoException extends AutoTechException {

    public DuplicadoException(String mensaje) {
        super(mensaje);
    }

    public DuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
