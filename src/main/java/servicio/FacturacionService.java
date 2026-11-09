package servicio;

import excepciones.ReglaNegocioException;
import modelo.Factura;
import modelo.OrdenTrabajo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FacturacionService {
    public Factura generar(OrdenTrabajo orden) throws ReglaNegocioException {
        if (orden == null) throw new ReglaNegocioException("Seleccione una orden.");
        if (orden.getTotal() == null || orden.getTotal().signum() <= 0) throw new ReglaNegocioException("La orden no tiene un total válido.");
        Factura f = new Factura();
        f.setOrden(orden);
        f.setFecha(LocalDateTime.now());
        f.setSubtotal(orden.getTotalServicios().add(orden.getTotalRepuestos()));
        f.setDescuento(BigDecimal.ZERO);
        f.setImpuesto(BigDecimal.ZERO);
        f.setTotal(orden.getTotal());
        return f;
    }
}
