package com.autotech.modelo;

import com.autotech.modelo.enums.NivelComplejidad;
import java.math.BigDecimal;

/** Nivel 2: afinamiento, inyectores, frenos. Recargo del 5 % por inspección. */
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
