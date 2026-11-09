package vista;

import dao.ReporteDAO;
import dao.impl.ReporteDAOImpl;
import modelo.TablaResultado;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FrmHistorialVehiculo extends JFrame {

    private final ReporteDAO reporteDAO;

    private JTextField txtFiltroPlaca;
    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;
    private JTextArea txtDetalleOrden;

    public FrmHistorialVehiculo() {
        this("");
    }

    public FrmHistorialVehiculo(String placaInicial) {
        this.reporteDAO = new ReporteDAOImpl();
        initComponents();
        if (placaInicial != null && !placaInicial.isBlank()) {
            txtFiltroPlaca.setText(placaInicial.trim());
        }
        cargarHistorial(txtFiltroPlaca.getText());
    }

    private void initComponents() {
        setTitle("AutoTech Manager — Expediente e Historial Clínico del Vehículo");
        setSize(1080, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = EstiloUI.panelEncabezado();
        pnlHeader.setBackground(EstiloUI.AZUL_OSCURO);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("MÓDULO 8: EXPEDIENTE HISTÓRICO POR VEHÍCULO");
        lblTitulo.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Consulta separada de intervenciones, servicios realizados, repuestos consumidos y sustituciones de motor");
        lblSub.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblSub.setForeground(EstiloUI.TEXTO_ENCABEZADO);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlCentro = new JPanel(new BorderLayout(8, 8));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel pnlBuscador = new JPanel(new BorderLayout(6, 6));
        pnlBuscador.add(new JLabel("Filtrar por Placa / Marca / Modelo / # Orden:"), BorderLayout.WEST);
        txtFiltroPlaca = EstiloUI.campoCompacto(new JTextField());
        JButton btnBuscar = EstiloUI.botonPrimario("Consultar Historial");
        JButton btnTodos = EstiloUI.botonSecundario("Ver Todos los Vehículos");

        btnBuscar.addActionListener(e -> cargarHistorial(txtFiltroPlaca.getText()));
        btnTodos.addActionListener(e -> {
            txtFiltroPlaca.setText("");
            cargarHistorial("");
        });
        txtFiltroPlaca.addActionListener(e -> cargarHistorial(txtFiltroPlaca.getText()));

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlBotones.add(btnBuscar);
        pnlBotones.add(btnTodos);

        pnlBuscador.add(txtFiltroPlaca, BorderLayout.CENTER);
        pnlBuscador.add(pnlBotones, BorderLayout.EAST);
        pnlCentro.add(pnlBuscador, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaHistorial = new JTable(modeloTabla);
        EstiloUI.tabla(tablaHistorial);

        tablaHistorial.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaHistorial.getSelectedRow() >= 0) {
                mostrarDetalleFila(tablaHistorial.getSelectedRow());
            }
        });

        txtDetalleOrden = new JTextArea(8, 40);
        txtDetalleOrden.setEditable(false);
        txtDetalleOrden.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtDetalleOrden.setBackground(new Color(0xF9FAFB));
        txtDetalleOrden.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        txtDetalleOrden.setText("Seleccione una orden en la tabla superior para visualizar el desglose completo del expediente técnico.");

        JScrollPane scrollDetalle = new JScrollPane(txtDetalleOrden);
        scrollDetalle.setBorder(BorderFactory.createTitledBorder("Ficha Detallada de la Intervención Seleccionada"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tablaHistorial), scrollDetalle);
        split.setDividerLocation(340);
        split.setResizeWeight(0.65);

        pnlCentro.add(split, BorderLayout.CENTER);
        add(pnlCentro, BorderLayout.CENTER);
    }

    private void cargarHistorial(String filtro) {
        try {
            TablaResultado res = reporteDAO.consultarHistorialVehiculo(filtro);
            modeloTabla.setColumnIdentifiers(res.getColumnas().toArray());
            modeloTabla.setRowCount(0);

            for (Object[] fila : res.getFilas()) {
                modeloTabla.addRow(fila);
            }

            if (modeloTabla.getRowCount() > 0) {
                tablaHistorial.setRowSelectionInterval(0, 0);
            } else {
                txtDetalleOrden.setText("No se encontraron registros históricos para el criterio especificado.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar historial: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDetalleFila(int r) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                 DETALLE DE INTERVENCIÓN EN EXPEDIENTE DEL VEHÍCULO             \n");
        sb.append("================================================================================\n");
        for (int c = 0; c < modeloTabla.getColumnCount(); c++) {
            String colName = modeloTabla.getColumnName(c);
            Object val = modeloTabla.getValueAt(r, c);
            sb.append(String.format(" %-26s : %s\n", colName, val != null ? val.toString() : "N/A"));
        }
        sb.append("================================================================================\n");
        txtDetalleOrden.setText(sb.toString());
        txtDetalleOrden.setCaretPosition(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmHistorialVehiculo().setVisible(true));
    }
}
