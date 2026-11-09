package modelo.enums;

import java.util.EnumSet;
import java.util.Set;

public enum EstadoOrden {
    RECIBIDA("Recibida"),
    EN_DIAGNOSTICO("En diagnóstico"),
    PENDIENTE_APROBACION("Pendiente de aprobación"),
    EN_REPARACION("En reparación"),
    EN_CONTROL_CALIDAD("En control de calidad"),
    FINALIZADA("Finalizada"),
    ENTREGADA("Entregada"),
    RECHAZADA("Rechazada"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoOrden(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Set<EstadoOrden> siguientesPermitidos() {
        switch (this) {
            case RECIBIDA:
                return EnumSet.of(EN_DIAGNOSTICO, CANCELADA);
            case EN_DIAGNOSTICO:
                return EnumSet.of(PENDIENTE_APROBACION, CANCELADA);
            case PENDIENTE_APROBACION:
                return EnumSet.of(EN_REPARACION, RECHAZADA, CANCELADA);
            case EN_REPARACION:
                return EnumSet.of(EN_CONTROL_CALIDAD, CANCELADA);
            case EN_CONTROL_CALIDAD:
                return EnumSet.of(FINALIZADA, EN_REPARACION);
            case FINALIZADA:
                return EnumSet.of(ENTREGADA);
            default:
                return EnumSet.noneOf(EstadoOrden.class);
        }
    }

    public boolean puedeCambiarA(EstadoOrden nuevo) {
        return siguientesPermitidos().contains(nuevo);
    }

    public boolean esFinal() {
        return this == ENTREGADA || this == RECHAZADA || this == CANCELADA;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
