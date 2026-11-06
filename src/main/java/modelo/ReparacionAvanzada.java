package modelo;

import modelo.enums.NivelComplejidad;
import java.math.BigDecimal;

/** Nivel 4: transmisión, culata, sistema eléctrico. Requiere diagnóstico y control de calidad. */
public class ReparacionAvanzada extends Servicio {

    @Override
    public NivelComplejidad getNivel() {
        return NivelComplejidad.AVANZADO;
    }

    @Override
    protected BigDecimal getRecargoTecnico() {
        return new BigDecimal("0.15");
    }

    @Override
    public boolean requiereControlCalidad() {
        return true;
    }

    @Override
    public int getDiasGarantia() {
        return 120;
    }

    @Override
    public boolean requiereDiagnostico() {
        return true;
    }
}
