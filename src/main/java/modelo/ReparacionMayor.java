package modelo;

import modelo.enums.NivelComplejidad;
import java.math.BigDecimal;

/** Nivel 5: reconstrucción y sustitución de motor. Requiere diagnóstico, autorización y garantía. */
public class ReparacionMayor extends Servicio {

    @Override
    public NivelComplejidad getNivel() {
        return NivelComplejidad.MAYOR;
    }

    @Override
    protected BigDecimal getRecargoTecnico() {
        return new BigDecimal("0.20");
    }

    @Override
    public boolean requiereControlCalidad() {
        return true;
    }

    @Override
    public int getDiasGarantia() {
        return 180;
    }

    @Override
    public boolean requiereDiagnostico() {
        return true;
    }

    /** Las reparaciones mayores siempre generan un documento de garantía. */
    public boolean generaGarantia() {
        return true;
    }
}
