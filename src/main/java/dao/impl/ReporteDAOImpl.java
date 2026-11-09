package dao.impl;

import dao.ReporteDAO;
import excepciones.PersistenciaException;
import modelo.TablaResultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAOImpl extends BaseDAO implements ReporteDAO {

    private TablaResultado ejecutarConsultaTabla(String sql, Object... params) throws PersistenciaException {
        try (Connection cn = obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            setParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                List<String> columnas = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    String label = meta.getColumnLabel(i);

                    columnas.add(label.replace('_', ' ').toUpperCase());
                }

                List<Object[]> filas = new ArrayList<>();
                while (rs.next()) {
                    Object[] fila = new Object[colCount];
                    for (int i = 1; i <= colCount; i++) {
                        fila[i - 1] = rs.getObject(i);
                    }
                    filas.add(fila);
                }
                return new TablaResultado(columnas, filas);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al ejecutar reporte: " + e.getMessage(), e);
        }
    }

    @Override
    public TablaResultado consultarOrdenesResumen() throws PersistenciaException {
        String sql = "SELECT orden_id, numero_orden, cliente, placa, vehiculo, "
                   + "mecanico_responsable, estado, requiere_cambio_motor, "
                   + "total_mano_obra, total_repuestos, importe_total_base, fecha_ingreso "
                   + "FROM dbo.vw_OrdenesResumen ORDER BY fecha_ingreso DESC";
        return ejecutarConsultaTabla(sql);
    }

    @Override
    public TablaResultado consultarStockBajo() throws PersistenciaException {
        String sql = "SELECT repuesto_id, codigo, nombre, categoria, marca, stock_actual, "
                   + "stock_minimo, unidades_faltantes, costo_compra, proveedor, telefono_proveedor "
                   + "FROM dbo.vw_StockBajo ORDER BY unidades_faltantes DESC";
        return ejecutarConsultaTabla(sql);
    }

    @Override
    public TablaResultado consultarHistorialVehiculo(String filtro) throws PersistenciaException {
        if (filtro == null || filtro.isBlank()) {
            String sql = "SELECT vehiculo_id, placa, vehiculo, numero_orden, fecha_ingreso, "
                       + "kilometraje_ingreso, estado, mecanico_responsable, servicios_realizados, "
                       + "repuestos_utilizados, costo_total_orden, serie_motor_anterior, serie_motor_nuevo "
                       + "FROM dbo.vw_HistorialVehiculo ORDER BY fecha_ingreso DESC";
            return ejecutarConsultaTabla(sql);
        } else {
            String like = "%" + filtro.trim() + "%";
            String sql = "SELECT vehiculo_id, placa, vehiculo, numero_orden, fecha_ingreso, "
                       + "kilometraje_ingreso, estado, mecanico_responsable, servicios_realizados, "
                       + "repuestos_utilizados, costo_total_orden, serie_motor_anterior, serie_motor_nuevo "
                       + "FROM dbo.vw_HistorialVehiculo "
                       + "WHERE placa LIKE ? OR vehiculo LIKE ? OR numero_orden LIKE ? "
                       + "ORDER BY fecha_ingreso DESC";
            return ejecutarConsultaTabla(sql, like, like, like);
        }
    }

    @Override
    public TablaResultado consultarSaldosFacturas() throws PersistenciaException {
        String sql = "SELECT factura_id, numero_factura, numero_orden, nombre_cliente, "
                   + "subtotal, descuento, impuesto, total, total_pagado, saldo_pendiente, "
                   + "estado_pago, fecha_emision "
                   + "FROM dbo.vw_SaldosFacturas ORDER BY fecha_emision DESC";
        return ejecutarConsultaTabla(sql);
    }

    @Override
    public TablaResultado consultarEstadisticasTaller() throws PersistenciaException {
        String sql = "SELECT "
                   + "(SELECT COUNT(*) FROM dbo.Clientes WHERE activo = 1) AS total_clientes, "
                   + "(SELECT COUNT(*) FROM dbo.Vehiculos WHERE activo = 1) AS total_vehiculos, "
                   + "(SELECT COUNT(*) FROM dbo.Mecanicos WHERE activo = 1 AND disponible = 1) AS mecanicos_disponibles, "
                   + "(SELECT COUNT(*) FROM dbo.Repuestos WHERE activo = 1 AND stock_actual <= stock_minimo) AS repuestos_stock_bajo, "
                   + "(SELECT COUNT(*) FROM dbo.OrdenesTrabajo WHERE activo = 1 AND estado NOT IN ('FINALIZADA','ENTREGADA','CANCELADA','RECHAZADA')) AS ordenes_en_proceso, "
                   + "(SELECT ISNULL(SUM(total), 0) FROM dbo.Facturas) AS total_facturado, "
                   + "(SELECT ISNULL(SUM(monto), 0) FROM dbo.Pagos) AS total_cobrado";
        return ejecutarConsultaTabla(sql);
    }

    @Override
    public TablaResultado consultarProductividadMecanicos() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.vw_ProductividadMecanicos ORDER BY horas_trabajadas DESC";
        return ejecutarConsultaTabla(sql);
    }

    @Override
    public TablaResultado consultarServiciosMasSolicitados() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.vw_ServiciosMasSolicitados ORDER BY veces_solicitado DESC";
        return ejecutarConsultaTabla(sql);
    }
}
