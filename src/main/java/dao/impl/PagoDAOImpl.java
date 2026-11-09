package dao.impl;

import dao.PagoDAO;
import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import modelo.TablaResultado;
import modelo.enums.MetodoPago;

public class PagoDAOImpl extends TallerDAOBase implements PagoDAO {

    @Override
    public BigDecimal registrarPago(int ordenId, BigDecimal monto, MetodoPago metodo, String referencia)
            throws PersistenciaException {
        try (Connection cn = obtenerConexion(); CallableStatement cs = llamada(cn, "sp_RegistrarPago", 5)) {
            cs.setInt(1, ordenId);
            cs.setBigDecimal(2, monto);
            cs.setString(3, metodo.name());
            if (referencia == null || referencia.isBlank()) cs.setNull(4, Types.VARCHAR); else cs.setString(4, referencia.trim());
            cs.registerOutParameter(5, Types.DECIMAL, 2);
            cs.execute();
            return cs.getBigDecimal(5);
        } catch (SQLException e) {
            throw error("Error al registrar el pago", e);
        }
    }

    @Override
    public TablaResultado listarPagos(int ordenId) throws PersistenciaException {
        return tabla("SELECT pago_id AS id, fecha, metodo, monto, referencia FROM dbo.Pagos "
                   + "WHERE orden_id = ? ORDER BY fecha, pago_id", ordenId);
    }

    @Override
    public int generarFactura(int ordenId) throws PersistenciaException {
        try (Connection cn = obtenerConexion()) {
            return generarFactura(cn, ordenId);
        } catch (SQLException e) {
            throw error("Error al generar la factura", e);
        }
    }

    @Override
    public int generarFactura(Connection cn, int ordenId) throws PersistenciaException {
        try (CallableStatement cs = llamada(cn, "sp_GenerarFactura", 2)) {
            cs.setInt(1, ordenId);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            return cs.getInt(2);
        } catch (SQLException e) {
            throw error("Error al generar la factura", e);
        }
    }

    @Override
    public TablaResultado listarFacturas() throws PersistenciaException {
        return tabla("SELECT numero_factura AS factura, numero_orden AS orden, nombre_cliente AS cliente, total, "
                   + "total_pagado AS pagado, saldo_pendiente AS saldo, estado_pago, fecha_emision "
                   + "FROM dbo.vw_SaldosFacturas ORDER BY fecha_emision DESC");
    }
}
