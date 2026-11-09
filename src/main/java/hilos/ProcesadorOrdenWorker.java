package hilos;

import java.util.List;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import modelo.OrdenResumen;
import servicio.OrdenTrabajoService;

/**
 * Procesa el cierre de una orden en segundo plano (la ventana no se congela):
 * verifica la orden, calcula la garantía según el nivel de sus servicios y, en una
 * sola transacción, genera la garantía, finaliza la orden y emite la factura.
 * La barra de progreso se actualiza en el hilo de la interfaz (publish/process).
 */
public class ProcesadorOrdenWorker extends SwingWorker<String, ProcesadorOrdenWorker.Avance> {

    /** Avance que se publica a la interfaz. */
    public record Avance(int porcentaje, String mensaje) {
    }

    private final int ordenId;
    private final JProgressBar barra;
    private final JLabel estado;
    private final Consumer<String> alTerminar;
    private final OrdenTrabajoService servicio = new OrdenTrabajoService();

    public ProcesadorOrdenWorker(int ordenId, JProgressBar barra, JLabel estado, Consumer<String> alTerminar) {
        this.ordenId = ordenId;
        this.barra = barra;
        this.estado = estado;
        this.alTerminar = alTerminar;
    }

    @Override
    protected String doInBackground() throws Exception {
        publish(new Avance(10, "Verificando la orden..."));
        OrdenResumen orden = servicio.buscar(ordenId).orElseThrow(() -> new IllegalStateException("La orden no existe."));
        pausa();

        publish(new Avance(30, "Revisando actividades, repuestos y control de calidad de " + orden.getNumero() + "..."));
        pausa();

        int dias = servicio.diasGarantia(ordenId);
        publish(new Avance(50, "Calculando garantía: " + dias + " días según el nivel de los servicios..."));
        pausa();

        publish(new Avance(70, "Finalizando la orden y emitiendo la factura..."));
        servicio.cerrarOrden(ordenId);

        OrdenResumen cerrada = servicio.buscar(ordenId).orElse(orden);
        publish(new Avance(100, "Orden " + cerrada.getNumero() + " finalizada."));
        return "Orden " + cerrada.getNumero() + " finalizada.\nFactura " + cerrada.getNumeroFactura()
                + " por L " + cerrada.getTotal() + "  (saldo pendiente: L " + cerrada.getSaldo() + ").\n"
                + "Se generó la garantía de " + dias + " días.";
    }

    @Override
    protected void process(List<Avance> avances) {
        Avance ultimo = avances.get(avances.size() - 1);
        barra.setValue(ultimo.porcentaje());
        estado.setText(ultimo.mensaje());
    }

    @Override
    protected void done() {
        String resultado;
        try {
            resultado = get();
        } catch (Exception ex) {
            Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
            barra.setValue(0);
            estado.setText("No se pudo procesar: " + causa.getMessage());
            resultado = "ERROR: " + causa.getMessage();
        }
        if (alTerminar != null) {
            alTerminar.accept(resultado);
        }
    }

    private static void pausa() throws InterruptedException {
        Thread.sleep(400);   // solo para que el avance se vea en la demostración
    }
}
