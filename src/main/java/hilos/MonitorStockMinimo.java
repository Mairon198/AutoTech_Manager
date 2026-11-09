package hilos;

import dao.RepuestoDAO;
import dao.impl.RepuestoDAOImpl;
import modelo.Repuesto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonitorStockMinimo implements Runnable {

    public interface AlertaStockListener {
        void onAlertaStockCritico(List<Repuesto> repuestosCriticos);
        void onRevisionCompletada(int repuestosTotalesBajoMinimo);

        /** Repuestos que estaban bajo el mínimo y ya se repusieron. */
        default void onRepuestosNormalizados(List<Repuesto> repuestos) {
        }
    }

    private final RepuestoDAO repuestoDAO;
    private final int intervaloSegundos;
    private final AtomicBoolean ejecutando = new AtomicBoolean(false);
    private Thread workerThread;
    private final List<AlertaStockListener> listeners = new CopyOnWriteArrayList<>();
    /** Lo que estaba bajo el mínimo en la revisión anterior (id -> repuesto): para avisar solo los cambios. */
    private final Map<Integer, Repuesto> anteriores = new LinkedHashMap<>();

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
        synchronized (anteriores) {
            anteriores.clear();   // la primera revisión del hilo muestra la lista completa
        }
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
                revisar(false);
                Thread.sleep(intervaloSegundos * 1000L);
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[MonitorStockMinimo] Error al revisar stock: " + e.getMessage());
            }
        }
    }

    /** Revisión manual: siempre informa la lista completa de repuestos bajo el mínimo. */
    public List<Repuesto> revisarStock() {
        return revisar(true);
    }

    /**
     * Revisa el inventario. En las revisiones periódicas solo se avisa lo que cambió desde la
     * revisión anterior (un repuesto que bajó del mínimo o cuyo stock siguió bajando, y los que
     * se repusieron), para no repetir la misma alerta cada pocos segundos.
     */
    private List<Repuesto> revisar(boolean completa) {
        try {
            List<Repuesto> bajoMinimo = repuestoDAO.listarBajoStockMinimo();
            int cantidad = bajoMinimo.size();

            List<Repuesto> cambios = new ArrayList<>();
            List<Repuesto> normalizados;
            synchronized (anteriores) {
                Map<Integer, Repuesto> actuales = new LinkedHashMap<>();
                for (Repuesto r : bajoMinimo) {
                    actuales.put(r.getId(), r);
                    Repuesto antes = anteriores.get(r.getId());
                    if (antes == null || antes.getStock() != r.getStock()) {
                        cambios.add(r);
                    }
                }
                normalizados = new ArrayList<>();
                for (Repuesto antes : anteriores.values()) {
                    if (!actuales.containsKey(antes.getId())) {
                        normalizados.add(antes);
                    }
                }
                anteriores.clear();
                anteriores.putAll(actuales);
            }
            List<Repuesto> avisar = completa ? bajoMinimo : cambios;

            for (AlertaStockListener l : listeners) {
                try {
                    l.onRevisionCompletada(cantidad);
                    if (!avisar.isEmpty()) {
                        l.onAlertaStockCritico(avisar);
                    }
                    if (!normalizados.isEmpty()) {
                        l.onRepuestosNormalizados(normalizados);
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
