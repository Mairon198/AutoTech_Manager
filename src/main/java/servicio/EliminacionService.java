package servicio;

import dao.EliminacionDAO;
import dao.impl.EliminacionDAOImpl;
import excepciones.AutoTechException;
import excepciones.PersistenciaException;
import excepciones.ReglaNegocioException;
import excepciones.ValidacionException;
import java.util.List;
import java.util.Map;
import modelo.OrdenResumen;
import modelo.enums.RegistroEliminable;
import util.Sesion;

/**
 * Eliminación de registros. Solo se borra lo que no tiene historial: si un cliente ya
 * tiene vehículos u órdenes, o un repuesto ya tiene movimientos, se informa la causa y
 * se sugiere desactivarlo, para no perder la trazabilidad que pide el sistema.
 */
public class EliminacionService {

    private static final Map<String, String> NOMBRES = Map.ofEntries(
            Map.entry("Vehiculos", "vehículos registrados"),
            Map.entry("OrdenesTrabajo", "órdenes de trabajo"),
            Map.entry("DetalleOrden", "órdenes de trabajo que lo usan"),
            Map.entry("DetallePresupuesto", "presupuestos que lo incluyen"),
            Map.entry("AsignacionMecanicos", "órdenes asignadas"),
            Map.entry("ActividadesTrabajo", "actividades registradas"),
            Map.entry("Diagnosticos", "diagnósticos registrados"),
            Map.entry("ControlesCalidad", "controles de calidad registrados"),
            Map.entry("RepuestosOrden", "órdenes que lo usan"),
            Map.entry("MovimientosInventario", "movimientos de inventario (kárdex)"),
            Map.entry("Repuestos", "repuestos que suministra"),
            Map.entry("Motores", "motores registrados"),
            Map.entry("CambiosMotor", "cambios de motor registrados"));

    private final EliminacionDAO dao = new EliminacionDAOImpl();

    public void eliminar(RegistroEliminable registro, int id) throws AutoTechException {
        if (id <= 0) throw new ValidacionException("Seleccione un registro.");
        String bloqueo = dao.eliminar(registro, id, Sesion.nombreUsuario());
        if (bloqueo != null) {
            throw new ReglaNegocioException("No se puede eliminar " + registro.getDescripcion() + " porque tiene "
                    + NOMBRES.getOrDefault(bloqueo, "registros relacionados (" + bloqueo + ")")
                    + ".\nPara conservar el historial, use \"Desactivar\".");
        }
    }

    public void eliminarOrden(OrdenResumen orden, String motivo) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        List<String> impedimentos = dao.impedimentosOrden(orden.getOrdenId());
        if (!impedimentos.isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar la orden " + orden.getNumero() + " porque "
                    + String.join(", ", impedimentos) + ".\nSi el trabajo no se hará, cámbiela a \"Cancelada\".");
        }
        try {
            dao.eliminarOrden(orden.getOrdenId(), orden.getNumero(), motivo, Sesion.nombreUsuario());
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }
}
