package com.autotech.excepciones;

/** El motor seleccionado no es compatible, no está disponible o le faltan datos. */
public class MotorIncompatibleException extends AutoTechException {

    public MotorIncompatibleException(String mensaje) {
        super(mensaje);
    }

    public MotorIncompatibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
