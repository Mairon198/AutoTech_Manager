package hilos;

import javax.swing.SwingWorker;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import java.util.List;
import java.util.concurrent.CancellationException;

public class ProcesadorOrdenWorker extends SwingWorker<Void, Integer> {
    private final JProgressBar barra;
    private final JLabel estado;
    private final Runnable tarea;
    public ProcesadorOrdenWorker(JProgressBar barra, JLabel estado, Runnable tarea) {
        this.barra = barra; this.estado = estado; this.tarea = tarea;
    }
    @Override protected Void doInBackground() throws Exception {
        setProgress(0);
        for (int i = 1; i <= 10; i++) {
            if (isCancelled()) throw new CancellationException("Proceso cancelado");
            Thread.sleep(120);
            publish(i * 10);
        }
        tarea.run();
        return null;
    }
    @Override protected void process(List<Integer> chunks) {
        int value = chunks.get(chunks.size()-1);
        barra.setValue(value);
        estado.setText("Procesando orden: " + value + "%");
    }
    @Override protected void done() {
        try { get(); estado.setText("Orden procesada correctamente."); }
        catch (Exception ex) { estado.setText("Error: " + ex.getMessage()); }
        barra.setValue(100);
    }
}
