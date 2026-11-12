package vista;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import reportes.GeneradorReportes;

/**
 *
 * @author Mairon
 */
public class FrmImpresionReportes extends JFrame {

    private JComboBox<String> cmbReportes;
    private JButton btnVer;
    private JButton btnImprimir;

    public FrmImpresionReportes() {
        setTitle("Imprimir reportes");
        setSize(420, 230);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel lblReporte = new JLabel("Seleccione el reporte:");
        lblReporte.setBounds(30, 25, 150, 25);
        panel.add(lblReporte);

        cmbReportes = new JComboBox<>(new String[]{"Clientes", "Vehiculos", "Mecanicos", "Servicios", "Repuestos", "Proveedores", "OrdenesTrabajo"});
        cmbReportes.setBounds(180, 25, 190, 25);
        panel.add(cmbReportes);

        btnVer = new JButton("Ver reporte");
        btnVer.setBounds(30, 75, 160, 32);
        btnVer.addActionListener(e -> verReporte());
        panel.add(btnVer);

        btnImprimir = new JButton("Imprimir");
        btnImprimir.setBounds(210, 75, 160, 32);
        btnImprimir.addActionListener(e -> imprimirReporte());
        panel.add(btnImprimir);

        add(panel);
        EstiloUI.primario(btnVer);
        EstiloUI.secundario(btnImprimir);
    }

    private void verReporte() {
        String nombre = cmbReportes.getSelectedItem().toString();
        GeneradorReportes reporte = new GeneradorReportes();
        reporte.mostrarReporte(nombre);
    }

    private void imprimirReporte() {
        String nombre = cmbReportes.getSelectedItem().toString();
        GeneradorReportes reporte = new GeneradorReportes();
        reporte.imprimirReporte(nombre);
    }
}
