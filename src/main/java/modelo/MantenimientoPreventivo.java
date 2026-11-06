package modelo;

import modelo.enums.NivelComplejidad;
import java.math.BigDecimal;

public class MantenimientoPreventivo extends Servicio {

    @Override
    public NivelComplejidad getNivel() {
        return NivelComplejidad.PREVENTIVO;
    }

    @Override
    protected BigDecimal getRecargoTecnico() {
        return new BigDecimal("0.05");
    }

    @Override
    public boolean requiereControlCalidad() {
        return false;
    }

    @Override
    public int getDiasGarantia() {
        return 60;
    }
}
