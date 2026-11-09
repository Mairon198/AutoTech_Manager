package dao.impl;

import dao.OrdenTrabajoDAO;
import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import modelo.OrdenResumen;
import modelo.TablaResultado;
import modelo.enums.EstadoActividad;
import modelo.enums.EstadoOrden;

public class OrdenTrabajoDAOImpl extends TallerDAOBase implements OrdenTrabajoDAO {

    private static final String SELECT_RESUMEN =
            "SELECT orden_id, numero_orden, cliente_id, cliente, vehiculo_id, placa, vehiculo, estado, "
          + "mecanico_responsable, fecha_ingreso, total_mano_obra, total_repuestos, descuento, impuesto, "
          + "total, total_pagado, saldo, numero_factura, requiere_cambio_motor FROM dbo.vw_OrdenesResumen ";

    private OrdenResumen mapear(ResultSet rs) throws SQLException {
        OrdenResumen o = new OrdenResumen();
        o.setOrdenId(rs.getInt("orden_id"));
        o.setNumero(rs.getString("numero_orden"));
        o.setClienteId(rs.getInt("cliente_id"));
        o.setCliente(rs.getString("cliente"));
        o.setVehiculoId(rs.getInt("vehiculo_id"));
        o.setPlaca(rs.getString("placa"));
        o.setVehiculo(rs.getString("vehiculo"));
        o.setEstado(EstadoOrden.valueOf(rs.getString("estado")));
        o.setResponsable(rs.getString("mecanico_responsable"));
        Timestamp f = rs.getTimestamp("fecha_ingreso");
        o.setFechaIngreso(f != null ? f.toLocalDateTime() : null);
        o.setTotalManoObra(rs.getBigDecimal("total_mano_obra"));
        o.setTotalRepuestos(rs.getBigDecimal("total_repuestos"));
        o.setDescuento(rs.getBigDecimal("descuento"));
        o.setImpuesto(rs.getBigDecimal("impuesto"));
        o.setTotal(rs.getBigDecimal("total"));
        o.setTotalPagado(rs.getBigDecimal("total_pagado"));
        o.setSaldo(rs.getBigDecimal("saldo"));
        o.setNumeroFactura(rs.getString("numero_factura"));
        o.setRequiereCambioMotor(rs.getBoolean("requiere_cambio_motor"));
        return o;
    }

    // ------------------------------------------------------------ encabezado

    /** Inserta la orden, asigna al responsable y actualiza el kilometraje del vehículo en UNA transacción. */
    @Override
    public int crear(int clienteId, int vehiculoId, int kilometraje, String sintomas, String diagnosticoPreliminar,
                     int responsableId) throws PersistenciaException {
        return ejecutarEnTransaccion(cn -> {
            int id = ejecutarInsertConClave(cn,
                    "INSERT INTO dbo.OrdenesTrabajo (cliente_id, vehiculo_id, kilometraje_ingreso, sintomas, diagnostico_preliminar) "
                  + "VALUES (?, ?, ?, ?, ?)",
                    clienteId, vehiculoId, kilometraje, sintomas, diagnosticoPreliminar);
            ejecutarUpdate(cn, "UPDATE dbo.Vehiculos SET kilometraje = ? WHERE vehiculo_id = ? AND kilometraje < ?",
                    kilometraje, vehiculoId, kilometraje);
            asignarMecanico(cn, id, responsableId, true);
            return id;
        });
    }

    @Override
    public List<OrdenResumen> listar(boolean soloAbiertas) throws PersistenciaException {
        String where = soloAbiertas ? "WHERE estado NOT IN ('ENTREGADA','RECHAZADA','CANCELADA') " : "";
        return consultarLista(SELECT_RESUMEN + where + "ORDER BY orden_id DESC", this::mapear);
    }

    @Override
    public Optional<OrdenResumen> buscar(int ordenId) throws PersistenciaException {
        return consultarUno(SELECT_RESUMEN + "WHERE orden_id = ?", this::mapear, ordenId);
    }

    @Override
    public int kilometrajeIngreso(int ordenId) throws PersistenciaException {
        return consultarUno("SELECT kilometraje_ingreso FROM dbo.OrdenesTrabajo WHERE orden_id = ?",
                rs -> rs.getInt(1), ordenId).orElse(0);
    }

    @Override
    public void cambiarEstado(int ordenId, EstadoOrden nuevo) throws PersistenciaException {
        try (Connection cn = obtenerConexion()) {
            cambiarEstado(cn, ordenId, nuevo);
        } catch (SQLException e) {
            throw error("Error al cambiar el estado", e);
        }
    }

    @Override
    public void cambiarEstado(Connection cn, int ordenId, EstadoOrden nuevo) throws PersistenciaException {
        try (CallableStatement cs = llamada(cn, "sp_CambiarEstadoOrden", 2)) {
            cs.setInt(1, ordenId);
            cs.setString(2, nuevo.name());
            cs.execute();
        } catch (SQLException e) {
            throw error("Error al cambiar el estado", e);
        }
    }

    // ------------------------------------------------------------ servicios

    @Override
    public void agregarServicio(int ordenId, int servicioId, int cantidad, BigDecimal precioHistorico)
            throws PersistenciaException {
        try {
            ejecutarUpdate("INSERT INTO dbo.DetalleOrden (orden_id, servicio_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)",
                    ordenId, servicioId, cantidad, precioHistorico);
        } catch (PersistenciaException e) {
            if (e.getMessage() != null && e.getMessage().contains("UQ_DetalleOrden")) {
                throw new PersistenciaException("Ese servicio ya está en la orden.");
            }
            throw e;
        }
    }

    @Override
    public void quitarServicio(int detalleId) throws PersistenciaException {
        ejecutarUpdate("DELETE FROM dbo.DetalleOrden WHERE detalle_id = ?", detalleId);
    }

    @Override
    public TablaResultado listarServicios(int ordenId) throws PersistenciaException {
        return tabla("SELECT d.detalle_id AS id, s.codigo, s.nombre AS servicio, s.nivel_complejidad AS nivel, "
                   + "d.cantidad, d.precio_unitario AS precio, d.cantidad * d.precio_unitario AS subtotal "
                   + "FROM dbo.DetalleOrden d INNER JOIN dbo.Servicios s ON s.servicio_id = d.servicio_id "
                   + "WHERE d.orden_id = ? ORDER BY d.detalle_id", ordenId);
    }

    @Override
    public List<Integer> nivelesServicios(int ordenId) throws PersistenciaException {
        return consultarLista("SELECT s.nivel_complejidad FROM dbo.DetalleOrden d "
                + "INNER JOIN dbo.Servicios s ON s.servicio_id = d.servicio_id WHERE d.orden_id = ?",
                rs -> rs.getInt(1), ordenId);
    }

    // ------------------------------------------------------------ repuestos

    @Override
    public void consumirRepuesto(int ordenId, int repuestoId, int cantidad, String usuario, String claveOperacion)
            throws PersistenciaException {
        try (Connection cn = obtenerConexion(); CallableStatement cs = llamada(cn, "sp_ConsumirRepuesto", 6)) {
            cs.setInt(1, ordenId);
            cs.setInt(2, repuestoId);
            cs.setInt(3, cantidad);
            cs.setString(4, usuario);
            cs.setString(5, claveOperacion);              // UNIQUEIDENTIFIER: evita doble descuento
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute();
        } catch (SQLException e) {
            throw error("Error al consumir el repuesto", e);
        }
    }

    @Override
    public TablaResultado listarRepuestos(int ordenId) throws PersistenciaException {
        return tabla("SELECT r.repuesto_orden_id AS id, p.codigo, r.nombre_historico AS repuesto, r.cantidad, "
                   + "r.precio_unitario_historico AS precio, r.cantidad * r.precio_unitario_historico AS subtotal, "
                   + "CASE WHEN r.consumido = 1 THEN 'Sí' ELSE 'No' END AS descontado, r.fecha "
                   + "FROM dbo.RepuestosOrden r INNER JOIN dbo.Repuestos p ON p.repuesto_id = r.repuesto_id "
                   + "WHERE r.orden_id = ? ORDER BY r.repuesto_orden_id", ordenId);
    }

    // ------------------------------------------------------------ mecánicos y actividades

    @Override
    public void asignarMecanico(int ordenId, int mecanicoId, boolean responsable) throws PersistenciaException {
        try (Connection cn = obtenerConexion()) {
            asignarMecanico(cn, ordenId, mecanicoId, responsable);
        } catch (SQLException e) {
            throw error("Error al asignar el mecánico", e);
        }
    }

    private void asignarMecanico(Connection cn, int ordenId, int mecanicoId, boolean responsable) throws PersistenciaException {
        try (CallableStatement cs = llamada(cn, "sp_AsignarMecanico", 3)) {
            cs.setInt(1, ordenId);
            cs.setInt(2, mecanicoId);
            cs.setBoolean(3, responsable);
            cs.execute();
        } catch (SQLException e) {
            throw error("Error al asignar el mecánico", e);
        }
    }

    @Override
    public TablaResultado listarAsignaciones(int ordenId) throws PersistenciaException {
        return tabla("SELECT m.mecanico_id AS id, m.codigo_empleado AS codigo, m.nombre AS mecanico, "
                   + "m.nivel_certificacion AS nivel, CASE WHEN a.es_responsable = 1 THEN 'Responsable' ELSE 'Apoyo' END AS rol, "
                   + "a.fecha_asignacion AS asignado "
                   + "FROM dbo.AsignacionMecanicos a INNER JOIN dbo.Mecanicos m ON m.mecanico_id = a.mecanico_id "
                   + "WHERE a.orden_id = ? ORDER BY a.es_responsable DESC, m.nombre", ordenId);
    }

    @Override
    public boolean esResponsable(int ordenId, int mecanicoId) throws PersistenciaException {
        return consultarUno("SELECT es_responsable FROM dbo.AsignacionMecanicos WHERE orden_id = ? AND mecanico_id = ?",
                rs -> rs.getBoolean(1), ordenId, mecanicoId).orElse(false);
    }

    @Override
    public int contarActividades(int ordenId, int mecanicoId) throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) FROM dbo.ActividadesTrabajo WHERE orden_id = ? AND mecanico_id = ?",
                rs -> rs.getInt(1), ordenId, mecanicoId).orElse(0);
    }

    @Override
    public int quitarAsignacion(int ordenId, int mecanicoId) throws PersistenciaException {
        return ejecutarUpdate("DELETE FROM dbo.AsignacionMecanicos WHERE orden_id = ? AND mecanico_id = ? AND es_responsable = 0",
                ordenId, mecanicoId);
    }

    @Override
    public int agregarActividad(int ordenId, int mecanicoId, String descripcion) throws PersistenciaException {
        return ejecutarInsertConClave(
                "INSERT INTO dbo.ActividadesTrabajo (orden_id, mecanico_id, descripcion, estado) VALUES (?, ?, ?, 'PENDIENTE')",
                ordenId, mecanicoId, descripcion);
    }

    @Override
    public void actualizarActividad(int actividadId, EstadoActividad estado, BigDecimal horas, String observaciones)
            throws PersistenciaException {
        String sql = "UPDATE dbo.ActividadesTrabajo SET estado = ?, "
                   + "inicio = COALESCE(inicio, SYSDATETIME()), "
                   + "fin = CASE WHEN ? = 'COMPLETADA' THEN SYSDATETIME() ELSE NULL END, "
                   + "horas = COALESCE(?, horas), observaciones = COALESCE(?, observaciones) "
                   + "WHERE actividad_id = ?";
        try (Connection cn = obtenerConexion(); java.sql.PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setString(2, estado.name());
            if (horas == null) ps.setNull(3, Types.DECIMAL); else ps.setBigDecimal(3, horas);
            if (observaciones == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, observaciones);
            ps.setInt(5, actividadId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("Error al actualizar la actividad", e);
        }
    }

    @Override
    public TablaResultado listarActividades(int ordenId) throws PersistenciaException {
        return tabla("SELECT a.actividad_id AS id, a.descripcion, m.nombre AS mecanico, a.estado, a.horas, "
                   + "a.inicio, a.fin, a.observaciones "
                   + "FROM dbo.ActividadesTrabajo a INNER JOIN dbo.Mecanicos m ON m.mecanico_id = a.mecanico_id "
                   + "WHERE a.orden_id = ? ORDER BY a.actividad_id", ordenId);
    }

    // ------------------------------------------------------------ calidad y garantía

    @Override
    public int registrarControl(int ordenId, int inspectorId, boolean aprobado, String checklist, String observaciones)
            throws PersistenciaException {
        String sql = "INSERT INTO dbo.ControlesCalidad (orden_id, inspector_id, aprobado, checklist, observaciones) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = obtenerConexion();
             java.sql.PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ordenId);
            ps.setInt(2, inspectorId);
            ps.setBoolean(3, aprobado);
            ps.setString(4, checklist);
            if (observaciones == null || observaciones.isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, observaciones);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw error("Error al registrar el control de calidad", e);
        }
    }

    @Override
    public TablaResultado listarControles(int ordenId) throws PersistenciaException {
        return tabla("SELECT c.control_id AS id, c.fecha, m.nombre AS inspector, "
                   + "CASE WHEN c.aprobado = 1 THEN 'APROBADO' ELSE 'RECHAZADO' END AS resultado, c.checklist, c.observaciones "
                   + "FROM dbo.ControlesCalidad c INNER JOIN dbo.Mecanicos m ON m.mecanico_id = c.inspector_id "
                   + "WHERE c.orden_id = ? ORDER BY c.fecha DESC, c.control_id DESC", ordenId);
    }

    @Override
    public boolean tieneGarantia(int ordenId) throws PersistenciaException {
        return consultarUno("SELECT 1 FROM dbo.Garantias WHERE orden_id = ?", rs -> true, ordenId).isPresent();
    }

    @Override
    public int generarGarantia(Connection cn, int ordenId, String descripcion, LocalDate inicio, LocalDate fin,
                               Integer kilometrajeLimite, String condiciones) throws PersistenciaException {
        return ejecutarInsertConClave(cn,
                "INSERT INTO dbo.Garantias (orden_id, descripcion, fecha_inicio, fecha_fin, kilometraje_limite, condiciones) "
              + "VALUES (?, ?, ?, ?, ?, ?)",
                ordenId, descripcion, inicio, fin, kilometrajeLimite, condiciones);
    }
}
