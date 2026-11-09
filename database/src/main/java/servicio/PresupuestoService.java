package servicio;

import excepciones.ReglaNegocioException;
import modelo.Presupuesto;
import modelo.enums.EstadoPresupuesto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PresupuestoService {
    public void validar(Presupuesto p) throws ReglaNegocioException {
        if (p == null || p.getDetalles().isEmpty()) throw new ReglaNegocioException("Agregue al menos un detalle al presupuesto.");
        if (p.getDescuento() == null || p.getDescuento().signum() < 0 || p.getDescuento().compareTo(p.getSubtotal()) > 0)
            throw new ReglaNegocioException("El descuento debe estar entre cero y el subtotal.");
        if (p.getDescuento().signum() > 0 && (p.getAutorizadoDescuentoPor() == null || p.getAutorizadoDescuentoPor().isBlank()))
            throw new ReglaNegocioException("Debe indicar quién autorizó el descuento.");
        for (var d : p.getDetalles()) {
            if (d.getCantidad() <= 0 || d.getPrecioUnitario() == null || d.getPrecioUnitario().signum() < 0)
                throw new ReglaNegocioException("Revise cantidades y precios del presupuesto.");
        }
    }
    public BigDecimal calcularSubtotal(Presupuesto p) { return p.getSubtotal(); }
    public BigDecimal calcularImpuesto(Presupuesto p) { return p.getMontoImpuesto(); }
    public BigDecimal calcularTotal(Presupuesto p) { return p.getTotal(); }
    public void responder(Presupuesto p, boolean aprobado, String observacion) throws ReglaNegocioException {
        validar(p);
        if (p.getEstado() != EstadoPresupuesto.PENDIENTE) throw new ReglaNegocioException("El presupuesto ya tiene respuesta.");
        p.setEstado(aprobado ? EstadoPresupuesto.APROBADO : EstadoPresupuesto.RECHAZADO);
        p.setFechaRespuesta(LocalDateTime.now());
        p.setObservacionCliente(observacion);
    }
}
