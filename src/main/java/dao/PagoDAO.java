package dao;

import excepciones.PersistenciaException;
import java.math.BigDecimal;
import java.sql.Connection;
import modelo.TablaResultado;
import modelo.enums.MetodoPago;

/** Contrato de persistencia de pagos y facturas (Módulo 8). */
public interface PagoDAO {

    /** Registra el pago con sp_RegistrarPago (impide sobrepagos) y devuelve el saldo nuevo. */
    BigDecimal registrarPago(int ordenId, BigDecimal monto, MetodoPago metodo, String referencia) throws PersistenciaException;

    TablaResultado listarPagos(int ordenId) throws PersistenciaException;

    /** Genera la factura con sp_GenerarFactura y devuelve su id. */
    int generarFactura(int ordenId) throws PersistenciaException;

    int generarFactura(Connection cn, int ordenId) throws PersistenciaException;

    TablaResultado listarFacturas() throws PersistenciaException;
}
