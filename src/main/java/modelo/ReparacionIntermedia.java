package modelo;

import modelo.enums.NivelComplejidad;
import java.math.BigDecimal;

/** Nivel 3: radiador, alternador, suspensión, embrague. Control de calidad obligatorio. */
public class ReparacionIntermedia extends Servicio {

    @Override
    public NivelComplejidad getNivel() {
        return NivelComplejidad.INTERMEDIO;
    }

    @Override
    protected BigDecimal getRecargoTecnico() {
        return new BigDecimal("0.10");
    }

    @Override
    public boolean requiereControlCalidad() {
        return true;
    }

    @Override
    public int getDiasGarantia() {
        return 90;
    }

    @Override
    public boolean requiereDiagnostico() {
        return true;
    }
}
