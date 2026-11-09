package dao.impl;

import dao.EliminacionDAO;
import excepciones.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import modelo.enums.RegistroEliminable;

public class EliminacionDAOImpl extends TallerDAOBase implements EliminacionDAO {

    private static final int ERROR_REFERENCIA = 547;   // violación de clave foránea en SQL Server
    private static final Pattern TABLA = Pattern.compile("table \"dbo\\.(\\w+)\"|tabla \"dbo\\.(\\w+)\"");
    private static final String AUDITAR =
            "INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo, usuario) "
            + "VALUES (?, 'DELETE', ?, ?, ?, ?, ?)";

    @Override
    public String eliminar(RegistroEliminable r, int id, String usuario) throws PersistenciaException {
        try (Connection cn = obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                for (String puente : r.getPuentes()) {
                    ejecutar(cn, "DELETE FROM dbo." + puente + " WHERE " + r.getColumnaId() + " = ?", id);
                }
                int filas = ejecutar(cn, "DELETE FROM dbo." + r.getTabla() + " WHERE " + r.getColumnaId() + " = ?", id);
                if (filas == 0) {
                    cn.rollback();
                    throw new PersistenciaException("El registro ya no existe.");
                }
                auditar(cn, r.getTabla(), id, r.getColumnaId(), String.valueOf(id), "Registro eliminado", usuario);
                cn.commit();
                return null;
            } catch (SQLException e) {
                cn.rollback();
                if (e.getErrorCode() == ERROR_REFERENCIA) {
                    Matcher m = TABLA.matcher(e.getMessage());
                    return m.find() ? (m.group(1) != null ? m.group(1) : m.group(2)) : "otros registros";
                }
                throw error("No se pudo eliminar", e);
            }
        } catch (SQLException e) {
            throw error("No se pudo eliminar", e);
        }
    }

    @Override
    public List<String> impedimentosOrden(int ordenId) throws PersistenciaException {
        return consultarUno("SELECT o.estado, "
                + "(SELECT COUNT(*) FROM dbo.RepuestosOrden r WHERE r.orden_id = o.orden_id AND r.consumido = 1) AS consumos, "
                + "(SELECT COUNT(*) FROM dbo.MovimientosInventario m WHERE m.orden_id = o.orden_id) AS movimientos, "
                + "(SELECT COUNT(*) FROM dbo.Pagos p WHERE p.orden_id = o.orden_id) AS pagos, "
                + "(SELECT COUNT(*) FROM dbo.Facturas f WHERE f.orden_id = o.orden_id) AS facturas, "
                + "(SELECT COUNT(*) FROM dbo.CambiosMotor c WHERE c.orden_id = o.orden_id) AS motores, "
                + "(SELECT COUNT(*) FROM dbo.Garantias g WHERE g.orden_id = o.orden_id) AS garantias "
                + "FROM dbo.OrdenesTrabajo o WHERE o.orden_id = ? AND o.activo = 1", rs -> {
                    List<String> l = new ArrayList<>();
                    String estado = rs.getString("estado");
                    if ("FINALIZADA".equals(estado) || "ENTREGADA".equals(estado)) {
                        l.add("ya está " + ("FINALIZADA".equals(estado) ? "finalizada" : "entregada"));
                    }
                    if (rs.getInt("consumos") > 0 || rs.getInt("movimientos") > 0) {
                        l.add("ya descontó repuestos del inventario");
                    }
                    if (rs.getInt("pagos") > 0) {
                        l.add("tiene pagos registrados");
                    }
                    if (rs.getInt("facturas") > 0) {
                        l.add("tiene factura emitida");
                    }
                    if (rs.getInt("motores") > 0) {
                        l.add("tiene un cambio de motor registrado");
                    }
                    if (rs.getInt("garantias") > 0) {
                        l.add("tiene garantía emitida");
                    }
                    return l;
                }, ordenId).orElse(List.of("la orden ya no existe"));
    }

    @Override
    public void eliminarOrden(int ordenId, String numero, String motivo, String usuario) throws PersistenciaException {
        ejecutarEnTransaccion(cn -> {
            int filas = ejecutarUpdate(cn, "UPDATE dbo.OrdenesTrabajo SET activo = 0 WHERE orden_id = ? AND activo = 1", ordenId);
            if (filas == 0) {
                throw new PersistenciaException("La orden ya no existe.");
            }
            try {
                auditar(cn, "OrdenesTrabajo", ordenId, "activo", "1",
                        recortar("Orden " + numero + " eliminada" + (motivo == null || motivo.isBlank() ? "" : ": " + motivo.trim()), 200),
                        usuario);
            } catch (SQLException e) {
                throw error("No se pudo registrar la auditoría", e);
            }
        });
    }

    private static int ejecutar(Connection cn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private static void auditar(Connection cn, String tabla, int id, String campo, String anterior, String detalle,
                                String usuario)
            throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(AUDITAR)) {
            ps.setString(1, tabla);
            ps.setInt(2, id);
            ps.setString(3, campo);
            ps.setString(4, anterior);
            ps.setString(5, recortar(detalle, 200));
            ps.setString(6, recortar(usuario == null || usuario.isBlank() ? "sistema" : usuario, 60));
            ps.executeUpdate();
        }
    }

    private static String recortar(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
