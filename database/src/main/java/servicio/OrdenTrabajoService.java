package servicio;

import excepciones.ReglaNegocioException;
import excepciones.AutoTechException;
import excepciones.TransicionEstadoInvalidaException;
import modelo.OrdenTrabajo;
import modelo.enums.EstadoOrden;

public class OrdenTrabajoService {
    public void cambiarEstado(OrdenTrabajo orden, EstadoOrden nuevo) throws AutoTechException {
        if (orden == null || nuevo == null) throw new ReglaNegocioException("La orden y el estado son obligatorios.");
        EstadoOrden actual = orden.getEstado();
        if (actual == null || !actual.puedeCambiarA(nuevo))
            throw new TransicionEstadoInvalidaException("No se permite cambiar de " + actual + " a " + nuevo + ".");
        if (nuevo == EstadoOrden.FINALIZADA) validarFinalizacion(orden);
        orden.setEstado(nuevo);
    }
    public void validarFinalizacion(OrdenTrabajo orden) throws ReglaNegocioException {
        if (orden.tieneActividadesPendientes()) throw new ReglaNegocioException("Hay actividades pendientes.");
        if (orden.tieneConsumosSinRegistrar()) throw new ReglaNegocioException("Hay repuestos sin registrar en inventario.");
        if (orden.requiereControlCalidad() && !orden.controlCalidadAprobado())
            throw new ReglaNegocioException("El control de calidad debe estar aprobado.");
    }
}
