package dao.impl;

import dao.CambioMotorDAO;
import excepciones.PersistenciaException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import modelo.CambioMotor;
import modelo.Motor;
import modelo.TablaResultado;
import modelo.enums.EstadoMotor;
import modelo.enums.TipoCombustible;

public class CambioMotorDAOImpl extends TallerDAOBase implements CambioMotorDAO {

    @Override
    public List<Motor> listarMotores(int vehiculoId) throws PersistenciaException {
        return consultarLista(
                "SELECT motor_id, numero_serie, marca, modelo_motor, cilindrada, combustible, estado, es_nuevo, costo "
              + "FROM dbo.Motores WHERE activo = 1 ORDER BY dbo.fn_MotorCompatible(motor_id, ?) DESC, estado, numero_serie",
                rs -> {
                    Motor m = new Motor();
                    m.setId(rs.getInt("motor_id"));
                    m.setNumeroSerie(rs.getString("numero_serie"));
                    m.setMarca(rs.getString("marca"));
                    m.setModeloMotor(rs.getString("modelo_motor"));
                    m.setCilindrada(rs.getString("cilindrada"));
                    m.setCombustible(TipoCombustible.valueOf(rs.getString("combustible")));
                    m.setEstado(EstadoMotor.valueOf(rs.getString("estado")));
                    m.setEsNuevo(rs.getBoolean("es_nuevo"));
                    m.setCosto(rs.getBigDecimal("costo"));
                    return m;
                }, vehiculoId);
    }

    @Override
    public List<Integer> compatibles(int vehiculoId) throws PersistenciaException {
        return consultarLista("SELECT motor_id FROM dbo.Motores WHERE dbo.fn_MotorCompatible(motor_id, ?) = 1",
                rs -> rs.getInt(1), vehiculoId);
    }

    @Override
    public Optional<String> serieMotorActual(int vehiculoId) throws PersistenciaException {
        return consultarUno("SELECT serie_motor_actual FROM dbo.Vehiculos WHERE vehiculo_id = ?",
                rs -> rs.getString(1), vehiculoId);
    }

    @Override
    public int registrar(CambioMotor c, String usuario) throws PersistenciaException {
        try (Connection cn = obtenerConexion(); CallableStatement cs = llamada(cn, "sp_RegistrarCambioMotor", 11)) {
            cs.setInt(1, c.getOrden().getId());
            cs.setString(2, c.getSerieMotorAnterior());
            cs.setInt(3, c.getMotorNuevo().getId());
            cs.setString(4, c.getDestinoMotorRetirado().name());
            cs.setTimestamp(5, Timestamp.valueOf(c.getFechaDesmontaje()));
            cs.setTimestamp(6, Timestamp.valueOf(c.getFechaInstalacion()));
            cs.setString(7, c.getResultadoPruebas());
            cs.setBoolean(8, c.isPruebasAprobadas());
            if (c.getObservaciones() == null || c.getObservaciones().isBlank()) cs.setNull(9, Types.VARCHAR);
            else cs.setString(9, c.getObservaciones());
            cs.setString(10, usuario);
            cs.registerOutParameter(11, Types.INTEGER);
            cs.execute();
            return cs.getInt(11);
        } catch (SQLException e) {
            throw error("Error al registrar el cambio de motor", e);
        }
    }

    @Override
    public TablaResultado consultarCambio(int ordenId) throws PersistenciaException {
        return tabla("SELECT c.cambio_motor_id AS id, c.serie_motor_anterior, m.numero_serie AS serie_motor_nuevo, "
                   + "m.modelo_motor, c.destino_motor_retirado AS destino_retirado, c.fecha_desmontaje, c.fecha_instalacion, "
                   + "c.resultado_pruebas, CASE WHEN c.pruebas_aprobadas = 1 THEN 'Sí' ELSE 'No' END AS pruebas_ok "
                   + "FROM dbo.CambiosMotor c INNER JOIN dbo.Motores m ON m.motor_id = c.motor_nuevo_id "
                   + "WHERE c.orden_id = ?", ordenId);
    }

    @Override
    public boolean[] avancePasos(int ordenId) throws PersistenciaException {
        String sql = "SELECT "
            + " (SELECT COUNT(*) FROM dbo.Diagnosticos d WHERE d.orden_id = o.orden_id) AS p1a,"
            + " (SELECT COUNT(*) FROM dbo.EvidenciasTecnicas e INNER JOIN dbo.Diagnosticos d ON d.diagnostico_id = e.diagnostico_id WHERE d.orden_id = o.orden_id) AS p1b,"
            + " (SELECT COUNT(*) FROM dbo.CambiosMotor c WHERE c.orden_id = o.orden_id) AS cambio,"
            + " (SELECT COUNT(*) FROM dbo.Presupuestos p WHERE p.orden_id = o.orden_id) AS presupuesto,"
            + " (SELECT COUNT(*) FROM dbo.Presupuestos p WHERE p.orden_id = o.orden_id AND p.estado = 'APROBADO') AS aprobado,"
            + " (SELECT COUNT(*) FROM dbo.AsignacionMecanicos a WHERE a.orden_id = o.orden_id AND a.es_responsable = 1) AS responsable,"
            + " (SELECT COUNT(*) FROM dbo.RepuestosOrden r WHERE r.orden_id = o.orden_id AND r.consumido = 1) AS repuestos,"
            + " (SELECT COUNT(*) FROM dbo.Garantias g WHERE g.orden_id = o.orden_id) AS garantia,"
            + " (SELECT TOP 1 CAST(aprobado AS INT) FROM dbo.ControlesCalidad q WHERE q.orden_id = o.orden_id ORDER BY q.fecha DESC, q.control_id DESC) AS calidad,"
            + " o.estado "
            + "FROM dbo.OrdenesTrabajo o WHERE o.orden_id = ?";
        return consultarUno(sql, rs -> {
            boolean cambio = rs.getInt("cambio") > 0;
            boolean cerrada = "FINALIZADA".equals(rs.getString("estado")) || "ENTREGADA".equals(rs.getString("estado"));
            return new boolean[] {
                rs.getInt("p1a") > 0 && rs.getInt("p1b") > 0,   // 1. diagnóstico y evidencias
                cambio,                                          // 2. serie del motor anterior
                cambio,                                          // 3. motor disponible seleccionado
                cambio,                                          // 4. compatibilidad validada
                rs.getInt("presupuesto") > 0,                    // 5. presupuesto
                rs.getInt("aprobado") > 0,                       // 6. autorización del cliente
                rs.getInt("responsable") > 0,                    // 7. mecánicos responsables
                cambio,                                          // 8. desmontaje, instalación y pruebas
                rs.getInt("repuestos") > 0,                      // 9. repuestos descontados
                cambio,                                          // 10. serie nueva, destino y pruebas
                rs.getInt("garantia") > 0 && rs.getInt("calidad") == 1 && cerrada   // 11. garantía + calidad + finalizada
            };
        }, ordenId).orElse(new boolean[11]);
    }
}
