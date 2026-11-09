package modelo;

import modelo.enums.NivelComplejidad;
import java.math.BigDecimal;

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

    public boolean generaGarantia() {
        return true;
    }
}
