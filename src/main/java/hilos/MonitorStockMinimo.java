package hilos;

import dao.RepuestoDAO;
import dao.impl.RepuestoDAOImpl;
import modelo.Repuesto;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonitorStockMinimo implements Runnable {

    public interface AlertaStockListener {
        void onAlertaStockCritico(List<Repuesto> repuestosCriticos);
        void onRevisionCompletada(int repuestosTotalesBajoMinimo);
    }

    private final RepuestoDAO repuestoDAO;
    private final int intervaloSegundos;
    private final AtomicBoolean ejecutando = new AtomicBoolean(false);
    private Thread workerThread;
    private final List<AlertaStockListener> listeners = new CopyOnWriteArrayList<>();

    public MonitorStockMinimo() {
        this(new RepuestoDAOImpl(), 5);
    }

    public MonitorStockMinimo(int intervaloSegundos) {
        this(new RepuestoDAOImpl(), intervaloSegundos);
    }

    public MonitorStockMinimo(RepuestoDAO repuestoDAO, int intervaloSegundos) {
        this.repuestoDAO = repuestoDAO;
        this.intervaloSegundos = Math.max(2, intervaloSegundos);
    }

    public void agregarListener(AlertaStockListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removerListener(AlertaStockListener listener) {
        listeners.remove(listener);
    }

    public synchronized void iniciar() {
        if (ejecutando.get()) {
            return;
        }
        ejecutando.set(true);
        workerThread = new Thread(this, "MonitorStockMinimo-Thread");
        workerThread.setDaemon(true);
        workerThread.start();
        System.out.println("[MonitorStockMinimo] Hilo iniciado. Intervalo: " + intervaloSegundos + "s");
    }

    public synchronized void detener() {
        if (!ejecutando.get()) {
            return;
        }
        ejecutando.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
        System.out.println("[MonitorStockMinimo] Hilo detenido.");
    }

    public boolean estaEjecutando() {
        return ejecutando.get();
    }

    @Override
    public void run() {
        while (ejecutando.get()) {
            try {
                revisarStock();
                Thread.sleep(intervaloSegundos * 1000L);
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[MonitorStockMinimo] Error al revisar stock: " + e.getMessage());
            }
        }
    }

    public List<Repuesto> revisarStock() {
        try {
            List<Repuesto> bajoMinimo = repuestoDAO.listarBajoStockMinimo();
            int cantidad = bajoMinimo.size();

            for (AlertaStockListener l : listeners) {
                try {
                    l.onRevisionCompletada(cantidad);
                    if (!bajoMinimo.isEmpty()) {
                        l.onAlertaStockCritico(bajoMinimo);
                    }
                } catch (Exception ex) {
                    System.err.println("[MonitorStockMinimo] Error en listener: " + ex.getMessage());
                }
            }
            return bajoMinimo;
        } catch (Exception e) {
            System.err.println("[MonitorStockMinimo] Error en consulta: " + e.getMessage());
            return List.of();
        }
    }
}
