package dao.impl;

import dao.PresupuestoDAO;
import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import modelo.ConfiguracionImpuesto;
import modelo.DetallePresupuesto;
import modelo.Presupuesto;
import modelo.Repuesto;
import modelo.Servicio;
import modelo.ServicioFactory;
import modelo.TablaResultado;

public class PresupuestoDAOImpl extends TallerDAOBase implements PresupuestoDAO {

    private static String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // ------------------------------------------------------------ diagnóstico

    @Override
    public int registrarDiagnostico(int ordenId, int mecanicoId, String descripcion, String causaRaiz,
                                    String recomendacion, boolean requiereCambioMotor) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Diagnosticos (orden_id, mecanico_id, descripcion, causa_raiz, recomendacion, requiere_cambio_motor) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ordenId);
            ps.setInt(2, mecanicoId);
            ps.setString(3, descripcion);
            String c = vacioANull(causaRaiz), r = vacioANull(recomendacion);
            if (c == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, c);
            if (r == null) ps.setNull(5, Types.VARCHAR); else ps.setString(5, r);
            ps.setBoolean(6, requiereCambioMotor);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw error("Error al registrar el diagnóstico", e);
        }
    }

    @Override
    public int agregarEvidencia(int diagnosticoId, String tipo, String descripcion, String valorMedido)
            throws PersistenciaException {
        String sql = "INSERT INTO dbo.EvidenciasTecnicas (diagnostico_id, tipo, descripcion, valor_medido) VALUES (?, ?, ?, ?)";
        try (Connection cn = obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, diagnosticoId);
            ps.setString(2, tipo);
            ps.setString(3, descripcion);
            String v = vacioANull(valorMedido);
            if (v == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, v);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw error("Error al registrar la evidencia", e);
        }
    }

    @Override
    public Optional<Integer> ultimoDiagnostico(int ordenId) throws PersistenciaException {
        return consultarUno("SELECT TOP 1 diagnostico_id FROM dbo.Diagnosticos WHERE orden_id = ? ORDER BY diagnostico_id DESC",
                rs -> rs.getInt(1), ordenId);
    }

    @Override
    public TablaResultado listarDiagnosticos(int ordenId) throws PersistenciaException {
        return tabla("SELECT d.diagnostico_id AS id, d.fecha, m.nombre AS mecanico, d.descripcion, d.causa_raiz, "
                   + "d.recomendacion, CASE WHEN d.requiere_cambio_motor = 1 THEN 'Sí' ELSE 'No' END AS cambio_motor "
                   + "FROM dbo.Diagnosticos d INNER JOIN dbo.Mecanicos m ON m.mecanico_id = d.mecanico_id "
                   + "WHERE d.orden_id = ? ORDER BY d.diagnostico_id DESC", ordenId);
    }

    @Override
    public TablaResultado listarEvidencias(int ordenId) throws PersistenciaException {
        return tabla("SELECT e.evidencia_id AS id, e.diagnostico_id AS diagnostico, e.tipo, e.descripcion, e.valor_medido "
                   + "FROM dbo.EvidenciasTecnicas e INNER JOIN dbo.Diagnosticos d ON d.diagnostico_id = e.diagnostico_id "
                   + "WHERE d.orden_id = ? ORDER BY e.evidencia_id", ordenId);
    }

    // ------------------------------------------------------------ presupuesto

    @Override
    public List<DetallePresupuesto> lineasSugeridas(int ordenId) throws PersistenciaException {
        // Mano de obra: un servicio por línea, con el precio histórico guardado en la orden
        List<DetallePresupuesto> lineas = consultarLista(
                "SELECT s.servicio_id, s.nivel_complejidad, s.codigo, s.nombre, s.tiempo_estimado_horas, d.cantidad, d.precio_unitario "
              + "FROM dbo.DetalleOrden d INNER JOIN dbo.Servicios s ON s.servicio_id = d.servicio_id "
              + "WHERE d.orden_id = ? ORDER BY d.detalle_id",
                rs -> {
                    Servicio s = ServicioFactory.crear(rs.getInt("nivel_complejidad"));
                    s.setId(rs.getInt("servicio_id"));
                    s.setCodigo(rs.getString("codigo"));
                    s.setNombre(rs.getString("nombre"));
                    DetallePresupuesto d = new DetallePresupuesto();
                    d.setTipoLinea(DetallePresupuesto.MANO_OBRA);
                    d.setServicio(s);
                    d.setDescripcion(s.getCodigo() + " - " + s.getNombre());
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setHoras(rs.getDouble("tiempo_estimado_horas") * rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    return d;
                }, ordenId);
        // Repuestos: los asociados a cada servicio (cantidad sugerida x cantidad del servicio)
        lineas.addAll(consultarLista(
                "SELECT r.repuesto_id, r.codigo, r.nombre, r.precio_venta, SUM(sr.cantidad_sugerida * d.cantidad) AS cantidad "
              + "FROM dbo.DetalleOrden d "
              + "INNER JOIN dbo.ServicioRepuestos sr ON sr.servicio_id = d.servicio_id "
              + "INNER JOIN dbo.Repuestos r ON r.repuesto_id = sr.repuesto_id "
              + "WHERE d.orden_id = ? AND r.activo = 1 "
              + "GROUP BY r.repuesto_id, r.codigo, r.nombre, r.precio_venta ORDER BY r.nombre",
                rs -> {
                    Repuesto r = new Repuesto();
                    r.setId(rs.getInt("repuesto_id"));
                    r.setCodigo(rs.getString("codigo"));
                    r.setNombre(rs.getString("nombre"));
                    DetallePresupuesto d = new DetallePresupuesto();
                    d.setTipoLinea(DetallePresupuesto.REPUESTO);
                    d.setRepuesto(r);
                    d.setDescripcion(r.getCodigo() + " - " + r.getNombre());
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_venta"));
                    return d;
                }, ordenId));
        return lineas;
    }

    @Override
    public int guardar(int ordenId, Presupuesto p) throws PersistenciaException {
        return ejecutarEnTransaccion(cn -> {
            int id;
            String sql = "INSERT INTO dbo.Presupuestos (orden_id, subtotal, descuento, autorizado_descuento_por, impuesto_id, "
                       + "monto_impuesto, total, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";
            try (PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, ordenId);
                ps.setBigDecimal(2, p.getSubtotal());
                ps.setBigDecimal(3, p.getDescuento());
                String aut = vacioANull(p.getAutorizadoDescuentoPor());
                if (aut == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, aut);
                if (p.getImpuesto() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, p.getImpuesto().getId());
                ps.setBigDecimal(6, p.getMontoImpuesto());
                ps.setBigDecimal(7, p.getTotal());
                ps.executeUpdate();
                try (var rs = ps.getGeneratedKeys()) {
                    rs.next();
                    id = rs.getInt(1);
                }
            }
            String sqlLinea = "INSERT INTO dbo.DetallePresupuesto (presupuesto_id, tipo_linea, servicio_id, repuesto_id, "
                            + "descripcion, horas, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = cn.prepareStatement(sqlLinea)) {
                for (DetallePresupuesto d : p.getDetalles()) {
                    ps.setInt(1, id);
                    ps.setString(2, d.getTipoLinea());
                    if (d.getServicio() != null) ps.setInt(3, d.getServicio().getId()); else ps.setNull(3, Types.INTEGER);
                    if (d.getRepuesto() != null) ps.setInt(4, d.getRepuesto().getId()); else ps.setNull(4, Types.INTEGER);
                    ps.setString(5, d.getDescripcion());
                    ps.setBigDecimal(6, BigDecimal.valueOf(d.getHoras()));
                    ps.setInt(7, d.getCantidad());
                    ps.setBigDecimal(8, d.getPrecioUnitario());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            return id;
        });
    }

    @Override
    public TablaResultado listarPresupuestos(int ordenId) throws PersistenciaException {
        return tabla("SELECT p.presupuesto_id AS id, p.fecha, p.estado, p.subtotal, p.descuento, "
                   + "i.nombre AS impuesto, p.monto_impuesto, p.total, p.fecha_respuesta, p.observacion_cliente "
                   + "FROM dbo.Presupuestos p LEFT JOIN dbo.ConfiguracionImpuestos i ON i.impuesto_id = p.impuesto_id "
                   + "WHERE p.orden_id = ? ORDER BY p.presupuesto_id DESC", ordenId);
    }

    @Override
    public TablaResultado listarLineas(int presupuestoId) throws PersistenciaException {
        return tabla("SELECT tipo_linea AS tipo, descripcion, horas, cantidad, precio_unitario AS precio, "
                   + "cantidad * precio_unitario AS subtotal FROM dbo.DetallePresupuesto "
                   + "WHERE presupuesto_id = ? ORDER BY tipo_linea, detalle_presupuesto_id", presupuestoId);
    }

    @Override
    public Optional<Integer> ultimoPendiente(int ordenId) throws PersistenciaException {
        return consultarUno("SELECT TOP 1 presupuesto_id FROM dbo.Presupuestos WHERE orden_id = ? AND estado = 'PENDIENTE' "
                + "ORDER BY presupuesto_id DESC", rs -> rs.getInt(1), ordenId);
    }

    @Override
    public void responder(int presupuestoId, boolean aprobado, String observacion) throws PersistenciaException {
        String sql = "UPDATE dbo.Presupuestos SET estado = ?, fecha_respuesta = SYSDATETIME(), observacion_cliente = ? "
                   + "WHERE presupuesto_id = ? AND estado = 'PENDIENTE'";
        try (Connection cn = obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, aprobado ? "APROBADO" : "RECHAZADO");
            String o = vacioANull(observacion);
            if (o == null) ps.setNull(2, Types.VARCHAR); else ps.setString(2, o);
            ps.setInt(3, presupuestoId);
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("El presupuesto ya fue respondido.");
            }
        } catch (SQLException e) {
            throw error("Error al registrar la respuesta del cliente", e);
        }
    }

    @Override
    public List<ConfiguracionImpuesto> listarImpuestos() throws PersistenciaException {
        return consultarLista("SELECT impuesto_id, nombre, porcentaje FROM dbo.ConfiguracionImpuestos WHERE activo = 1 "
                + "ORDER BY es_predeterminado DESC, nombre", rs -> {
                    ConfiguracionImpuesto i = new ConfiguracionImpuesto();
                    i.setId(rs.getInt("impuesto_id"));
                    i.setNombre(rs.getString("nombre"));
                    i.setPorcentaje(rs.getBigDecimal("porcentaje"));
                    return i;
                });
    }
}
