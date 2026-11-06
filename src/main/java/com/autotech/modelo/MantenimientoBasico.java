package com.autotech.modelo;

import com.autotech.modelo.enums.NivelComplejidad;
import java.math.BigDecimal;

/** Nivel 1: aceite, filtros, bujías, luces. Sin recargo ni control de calidad obligatorio. */
public class MantenimientoBasico extends Servicio {

    @Override
    public NivelComplejidad getNivel() {
        return NivelComplejidad.BASICO;
    }

    @Override
    protected BigDecimal getRecargoTecnico() {
        return new BigDecimal("0.00");
    }

    @Override
    public boolean requiereControlCalidad() {
        return false;
    }

    @Override
    public int getDiasGarantia() {
        return 30;
    }
}
