package servicio;

import excepciones.ReglaNegocioException;
import excepciones.AutoTechException;
import excepciones.SobrepagoException;
import modelo.OrdenTrabajo;
import modelo.Pago;
import java.math.BigDecimal;

public class PagoService {
    public BigDecimal saldoPendiente(OrdenTrabajo orden) { return orden.getSaldo(); }
    public void validarPago(OrdenTrabajo orden, Pago pago) throws AutoTechException {
        if (orden == null || pago == null || pago.getMonto() == null || pago.getMetodo() == null)
            throw new ReglaNegocioException("Complete los datos del pago.");
        if (pago.getMonto().signum() <= 0) throw new ReglaNegocioException("El monto debe ser mayor que cero.");
        if (pago.getMonto().compareTo(orden.getSaldo()) > 0)
            throw new SobrepagoException("El pago supera el saldo pendiente: " + orden.getSaldo());
    }
    public void registrarEnMemoria(OrdenTrabajo orden, Pago pago) throws AutoTechException {
        validarPago(orden, pago);
        pago.setOrden(orden);
        orden.getPagos().add(pago);
    }
}
