package servicio;

import dao.PagoDAO;
import dao.impl.PagoDAOImpl;
import excepciones.AutoTechException;
import excepciones.PersistenciaException;
import excepciones.ReglaNegocioException;
import excepciones.ValidacionException;
import modelo.OrdenResumen;
import modelo.TablaResultado;
import modelo.enums.EstadoOrden;

/** Emisión de facturas de órdenes finalizadas. */
public class FacturacionService {

    private final PagoDAO dao = new PagoDAOImpl();

    public int generarFactura(OrdenResumen orden) throws AutoTechException {
        if (orden == null) throw new ValidacionException("Seleccione una orden.");
        if (orden.getEstado() != EstadoOrden.FINALIZADA && orden.getEstado() != EstadoOrden.ENTREGADA) {
            throw new ReglaNegocioException("Solo se facturan órdenes finalizadas (la orden está en " + orden.getEstado() + ").");
        }
        if (orden.getNumeroFactura() != null) {
            throw new ReglaNegocioException("La orden ya tiene la factura " + orden.getNumeroFactura() + ".");
        }
        try {
            return dao.generarFactura(orden.getOrdenId());
        } catch (PersistenciaException e) {
            throw ErroresNegocio.traducir(e);
        }
    }

    public TablaResultado facturas() throws PersistenciaException {
        return dao.listarFacturas();
    }
}
