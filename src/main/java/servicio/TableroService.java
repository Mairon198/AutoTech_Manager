package servicio;

import conexion.ConexionDB;
import dao.ReporteDAO;
import dao.impl.ReporteDAOImpl;
import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import modelo.OrdenResumen;
import modelo.TablaResultado;
import modelo.enums.EstadoOrden;

/** Datos del tablero del menú principal: órdenes por estado, alertas y saldos. */
public class TableroService {

    /** Resumen del taller en un momento dado. */
    public record Tablero(String baseDatos, List<OrdenResumen> ordenes, Map<EstadoOrden, Integer> porEstado,
                          int abiertas, int stockBajo, int mecanicosDisponibles, int clientes, int vehiculos,
                          BigDecimal porCobrar, int ordenesPorCobrar) {

        public int cantidad(EstadoOrden e) {
            return porEstado.getOrDefault(e, 0);
        }

        /** Primera orden (la más reciente) que está en el estado indicado, o null. */
        public OrdenResumen primeraEn(EstadoOrden e) {
            return ordenes.stream().filter(o -> o.getEstado() == e).findFirst().orElse(null);
        }
    }

    private final OrdenTrabajoService ordenes = new OrdenTrabajoService();
    private final ReporteDAO reportes = new ReporteDAOImpl();

    public Tablero cargar() throws PersistenciaException {
        String base;
        try (Connection cn = ConexionDB.getConexion()) {
            base = cn.getCatalog();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar: " + e.getMessage(), e);
        }

        List<OrdenResumen> lista = ordenes.listar(false);
        Map<EstadoOrden, Integer> porEstado = new EnumMap<>(EstadoOrden.class);
        int abiertas = 0;
        int porCobrarCant = 0;
        BigDecimal porCobrar = BigDecimal.ZERO;
        for (OrdenResumen o : lista) {
            porEstado.merge(o.getEstado(), 1, Integer::sum);
            if (!o.estaCerrada()) {
                abiertas++;
            }
            boolean facturable = o.getEstado() == EstadoOrden.FINALIZADA || o.getEstado() == EstadoOrden.ENTREGADA;
            if (facturable && o.getSaldo() != null && o.getSaldo().signum() > 0) {
                porCobrar = porCobrar.add(o.getSaldo());
                porCobrarCant++;
            }
        }

        int clientes = 0, vehiculos = 0, mecanicos = 0, stockBajo = 0;
        TablaResultado est = reportes.consultarEstadisticasTaller();
        if (!est.getFilas().isEmpty()) {
            Object[] f = est.getFilas().get(0);
            clientes = numero(f[0]);
            vehiculos = numero(f[1]);
            mecanicos = numero(f[2]);
            stockBajo = numero(f[3]);
        }
        return new Tablero(base, lista, porEstado, abiertas, stockBajo, mecanicos, clientes, vehiculos,
                porCobrar, porCobrarCant);
    }

    private static int numero(Object v) {
        return v instanceof Number n ? n.intValue() : 0;
    }
}
