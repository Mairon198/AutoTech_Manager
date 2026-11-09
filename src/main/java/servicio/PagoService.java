package servicio;

import dao.PagoDAO;
import dao.impl.PagoDAOImpl;
import excepciones.AutoTechException;
import excepciones.PersistenciaException;
import excepciones.SobrepagoException;
import excepciones.ValidacionException;
import java.math.BigDecimal;
import modelo.OrdenResumen;
import modelo.TablaResultado;
import modelo.enums.MetodoPago;

/** Pagos parciales o totales con control de saldo (Módulo 8). */
public class PagoService {

    private final PagoDAO dao = new PagoDAOImpl();

    /** Registra el pago y devuelve el saldo que queda. Impide sobrepagos (también lo valida la base). */
    public BigDecimal registrarPago(OrdenResumen orden, BigDecimal monto, MetodoPago metodo, String referencia)
            throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        if (metodo == null) throw new ValidacionException("Seleccione el método de pago.");
        if (monto == null || monto.signum() <= 0) throw new ValidacionException("El monto debe ser mayor que cero.");
        if (orden.getSaldo().signum() <= 0) throw new SobrepagoException("La orden no tiene saldo pendiente.");
        if (monto.compareTo(orden.getSaldo()) > 0) {
            throw new SobrepagoException("Sobrepago: el monto (L " + monto + ") supera el saldo pendiente (L " + orden.getSaldo() + ").");
        }
        try {
            return dao.registrarPago(orden.getOrdenId(), monto, metodo, referencia);
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    public TablaResultado pagos(int ordenId) throws PersistenciaException {
        return dao.listarPagos(ordenId);
    }
}
