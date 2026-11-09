package dao.impl;

import dao.MovimientoInventarioDAO;
import excepciones.PersistenciaException;
import modelo.MovimientoInventario;
import modelo.Repuesto;
import modelo.enums.TipoMovimiento;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class MovimientoInventarioDAOImpl extends BaseDAO implements MovimientoInventarioDAO {

    private MovimientoInventario mapear(ResultSet rs) throws SQLException {
        MovimientoInventario m = new MovimientoInventario();
        m.setId(rs.getInt("movimiento_id"));

        Repuesto r = new Repuesto();
        r.setId(rs.getInt("repuesto_id"));
        try {
            r.setCodigo(rs.getString("codigo_repuesto"));
            r.setNombre(rs.getString("nombre_repuesto"));
        } catch (SQLException ignored) {
        }
        m.setRepuesto(r);

        int ordId = rs.getInt("orden_id");
        if (!rs.wasNull()) {
            m.setIdOrden(ordId);
        }

        String tipoStr = rs.getString("tipo_movimiento");
        if ("SALIDA_ORDEN".equalsIgnoreCase(tipoStr) || "SALIDA".equalsIgnoreCase(tipoStr)) {
            m.setTipo(TipoMovimiento.SALIDA);
        } else if ("AJUSTE".equalsIgnoreCase(tipoStr)) {
            m.setTipo(TipoMovimiento.AJUSTE);
        } else {
            m.setTipo(TipoMovimiento.ENTRADA);
        }

        m.setCantidad(rs.getInt("cantidad"));
        m.setStockAnterior(rs.getInt("stock_anterior"));
        m.setStockResultante(rs.getInt("stock_resultante"));
        m.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
        m.setReferencia(rs.getString("motivo"));
        m.setUsuario(rs.getString("usuario_registro"));

        Timestamp ts = rs.getTimestamp("fecha_movimiento");
        if (ts != null) {
            m.setFecha(ts.toLocalDateTime());
            m.setFechaCreacion(ts.toLocalDateTime());
        }
        return m;
    }

    private static final String SELECT_BASE =
            "SELECT m.movimiento_id, m.repuesto_id, m.orden_id, m.tipo_movimiento, m.cantidad, "
          + "m.stock_anterior, m.stock_resultante, m.costo_unitario, m.motivo, m.usuario_registro, "
          + "m.fecha_movimiento, r.codigo AS codigo_repuesto, r.nombre AS nombre_repuesto "
          + "FROM dbo.MovimientosInventario m "
          + "INNER JOIN dbo.Repuestos r ON r.repuesto_id = m.repuesto_id ";

    @Override
    public Integer insertar(MovimientoInventario entidad) throws PersistenciaException {
        String sql = "INSERT INTO dbo.MovimientosInventario (repuesto_id, orden_id, tipo_movimiento, "
                   + "cantidad, stock_anterior, stock_resultante, costo_unitario, motivo, usuario_registro, fecha_movimiento) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String tipoDb;
        if (entidad.getTipo() == TipoMovimiento.SALIDA) {
            tipoDb = "SALIDA_ORDEN";
        } else if (entidad.getTipo() == TipoMovimiento.AJUSTE) {
            tipoDb = "AJUSTE";
        } else {
            tipoDb = "ENTRADA";
        }

        int id = ejecutarInsertConClave(sql,
                entidad.getRepuesto() != null ? entidad.getRepuesto().getId() : null,
                entidad.getIdOrden() != null && entidad.getIdOrden() > 0 ? entidad.getIdOrden() : null,
                tipoDb,
                entidad.getCantidad(),
                entidad.getStockAnterior(),
                entidad.getStockResultante(),
                entidad.getCostoUnitario(),
                entidad.getReferencia(),
                entidad.getUsuario() != null ? entidad.getUsuario() : "SISTEMA",
                entidad.getFecha() != null ? entidad.getFecha() : java.time.LocalDateTime.now());
        entidad.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(MovimientoInventario entidad) throws PersistenciaException {

        return false;
    }

    @Override
    public Optional<MovimientoInventario> buscarPorId(Integer id) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE m.movimiento_id = ?";
        return consultarUno(sql, this::mapear, id);
    }

    @Override
    public List<MovimientoInventario> listar() throws PersistenciaException {
        String sql = SELECT_BASE + "ORDER BY m.movimiento_id DESC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<MovimientoInventario> listarPorRepuesto(int repuestoId) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE m.repuesto_id = ? ORDER BY m.movimiento_id DESC";
        return consultarLista(sql, this::mapear, repuestoId);
    }

    @Override
    public List<MovimientoInventario> listarPorOrden(int ordenId) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE m.orden_id = ? ORDER BY m.movimiento_id DESC";
        return consultarLista(sql, this::mapear, ordenId);
    }

    @Override
    public List<MovimientoInventario> listarRecientes(int limite) throws PersistenciaException {
        String sql = "SELECT TOP (?) m.movimiento_id, m.repuesto_id, m.orden_id, m.tipo_movimiento, m.cantidad, "
                   + "m.stock_anterior, m.stock_resultante, m.costo_unitario, m.motivo, m.usuario_registro, "
                   + "m.fecha_movimiento, r.codigo AS codigo_repuesto, r.nombre AS nombre_repuesto "
                   + "FROM dbo.MovimientosInventario m "
                   + "INNER JOIN dbo.Repuestos r ON r.repuesto_id = m.repuesto_id "
                   + "ORDER BY m.movimiento_id DESC";
        return consultarLista(sql, this::mapear, limite);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        return false;
    }
}
