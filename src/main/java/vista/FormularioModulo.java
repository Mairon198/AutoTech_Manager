package vista;

import hilos.ProcesadorOrdenWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.*;

public class FormularioModulo extends JFrame {
    private final JPanel campos = new JPanel(new GridLayout(0, 2, 10, 8));
    private final JProgressBar progreso = new JProgressBar(0, 100);
    private final JLabel estado = new JLabel("Listo.");
    public FormularioModulo(String titulo, String[] etiquetas) {
        super("AutoTech Manager - " + titulo);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 580);
        setLocationRelativeTo(null);
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel encabezado = new JLabel(titulo);
        encabezado.setFont(encabezado.getFont().deriveFont(22f));
        raiz.add(encabezado, BorderLayout.NORTH);
        for (String etiqueta : etiquetas) {
            campos.add(new JLabel(etiqueta));
            campos.add(new JTextField());
        }
        raiz.add(new JScrollPane(campos), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(8, 8));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton guardar = new JButton("Validar / Guardar");
        JButton limpiar = new JButton("Limpiar");
        JButton procesar = new JButton("Procesar en segundo plano");
        botones.add(limpiar); botones.add(guardar); botones.add(procesar);
        limpiar.addActionListener(e -> limpiarCampos());
        guardar.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Datos capturados. Conecte la acción al DAO correspondiente para persistirlos.",
            "AutoTech Manager", JOptionPane.INFORMATION_MESSAGE));
        procesar.addActionListener(e -> {
            progreso.setValue(0);
            new ProcesadorOrdenWorker(progreso, estado, () -> {}).execute();
        });
        pie.add(botones, BorderLayout.NORTH);
        pie.add(progreso, BorderLayout.CENTER);
        pie.add(estado, BorderLayout.SOUTH);
        raiz.add(pie, BorderLayout.SOUTH);
        setContentPane(raiz);
    }
    private void limpiarCampos() {
        for (java.awt.Component c : campos.getComponents())
            if (c instanceof JTextField t) t.setText("");
        estado.setText("Campos limpiados.");
        progreso.setValue(0);
    }
}
