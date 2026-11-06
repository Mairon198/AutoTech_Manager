package modelo;

import modelo.enums.NivelComplejidad;

/** Crea la subclase correcta de Servicio según el nivel guardado en la BD. */
public final class ServicioFactory {

    private ServicioFactory() {
    }

    public static Servicio crear(int nivel) {
        return crear(NivelComplejidad.desdeNivel(nivel));
    }

    public static Servicio crear(NivelComplejidad nivel) {
        switch (nivel) {
            case BASICO:
                return new MantenimientoBasico();
            case PREVENTIVO:
                return new MantenimientoPreventivo();
            case INTERMEDIO:
                return new ReparacionIntermedia();
            case AVANZADO:
                return new ReparacionAvanzada();
            case MAYOR:
                return new ReparacionMayor();
            default:
                throw new IllegalArgumentException("Nivel no soportado: " + nivel);
        }
    }
}
