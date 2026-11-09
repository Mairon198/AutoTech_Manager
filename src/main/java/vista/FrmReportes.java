package vista;

import dao.ReporteDAO;
import dao.impl.ReporteDAOImpl;
import modelo.TablaResultado;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FrmReportes extends JFrame {

    private final ReporteDAO reporteDAO;

    private JLabel lblKpiClientes;
    private JLabel lblKpiVehiculos;
    private JLabel lblKpiMecanicos;
    private JLabel lblKpiStockBajo;
    private JLabel lblKpiOrdenesProceso;
    private JLabel lblKpiFacturado;
    private JLabel lblKpiCobrado;

    private JTable tablaOrdenesResumen;
    private JTable tablaStockBajo;
    private JTable tablaSaldosFacturas;
    private JTable tablaProductividad;
    private JTable tablaServicios;

    public FrmReportes() {
        this.reporteDAO = new ReporteDAOImpl();
        initComponents();
        refrescarTodosLosReportes();
    }

    private void initComponents() {
        setTitle("AutoTech Manager — Reportes Administrativos y Vistas SQL");
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = EstiloUI.panelEncabezado();
        pnlHeader.setBackground(EstiloUI.AZUL_OSCURO);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("REPORTES ADMINISTRATIVOS Y TABLERO DE CONTROL");
        lblTitulo.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        JButton btnRefrescarTodo = EstiloUI.botonPrimario("Actualizar Reportes");
        btnRefrescarTodo.addActionListener(e -> refrescarTodosLosReportes());
        pnlHeader.add(btnRefrescarTodo, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel(new BorderLayout(10, 10));
        pnlBody.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel pnlKpis = new JPanel(new GridLayout(1, 7, 8, 8));
        lblKpiClientes = crearTarjetaKpi(pnlKpis, "Clientes Activos");
        lblKpiVehiculos = crearTarjetaKpi(pnlKpis, "Vehículos");
        lblKpiMecanicos = crearTarjetaKpi(pnlKpis, "Mecánicos Libres");
        lblKpiStockBajo = crearTarjetaKpi(pnlKpis, "Alertas Stock");
        lblKpiOrdenesProceso = crearTarjetaKpi(pnlKpis, "Órdenes Activas");
        lblKpiFacturado = crearTarjetaKpi(pnlKpis, "Total Facturado");
        lblKpiCobrado = crearTarjetaKpi(pnlKpis, "Total Cobrado");

        pnlBody.add(pnlKpis, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstiloUI.fuente(Font.BOLD, 13f));

        tablaOrdenesResumen = new JTable();
        EstiloUI.tabla(tablaOrdenesResumen);
        tabs.addTab("Órdenes", crearPanelVista(tablaOrdenesResumen, "OrdenesResumen"));

        tablaStockBajo = new JTable();
        EstiloUI.tabla(tablaStockBajo);
        tabs.addTab("Stock bajo", crearPanelVista(tablaStockBajo, "StockBajo"));

        tablaSaldosFacturas = new JTable();
        EstiloUI.tabla(tablaSaldosFacturas);
        tabs.addTab("Saldos y facturas", crearPanelVista(tablaSaldosFacturas, "SaldosFacturas"));

        tablaProductividad = new JTable();
        EstiloUI.tabla(tablaProductividad);
        tabs.addTab("Productividad de mecánicos", crearPanelVista(tablaProductividad, "ProductividadMecanicos"));

        tablaServicios = new JTable();
        EstiloUI.tabla(tablaServicios);
        tabs.addTab("Servicios más solicitados", crearPanelVista(tablaServicios, "ServiciosMasSolicitados"));

        pnlBody.add(tabs, BorderLayout.CENTER);
        add(pnlBody, BorderLayout.CENTER);
    }

    private JLabel crearTarjetaKpi(JPanel contenedor, String titulo) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloUI.BORDE, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setFont(EstiloUI.fuente(Font.BOLD, 11f));
        lblTit.setForeground(EstiloUI.TEXTO_SUAVE);

        JLabel lblVal = new JLabel("0", SwingConstants.CENTER);
        lblVal.setFont(EstiloUI.fuente(Font.BOLD, 16f));
        lblVal.setForeground(EstiloUI.PRIMARIO);

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        contenedor.add(card);
        return lblVal;
    }

    private JPanel crearPanelVista(JTable tabla, String nombreReporte) {
        JPanel pnl = new JPanel(new BorderLayout(6, 6));
        pnl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        pnl.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return pnl;
    }

    private void refrescarTodosLosReportes() {
        try {

            TablaResultado stats = reporteDAO.consultarEstadisticasTaller();
            if (!stats.getFilas().isEmpty()) {
                Object[] f = stats.getFilas().get(0);
                lblKpiClientes.setText(String.valueOf(f[0]));
                lblKpiVehiculos.setText(String.valueOf(f[1]));
                lblKpiMecanicos.setText(String.valueOf(f[2]));
                lblKpiStockBajo.setText(String.valueOf(f[3]));
                lblKpiOrdenesProceso.setText(String.valueOf(f[4]));
                lblKpiFacturado.setText("Lps. " + f[5]);
                lblKpiCobrado.setText("Lps. " + f[6]);
            }

            llenarTabla(tablaOrdenesResumen, reporteDAO.consultarOrdenesResumen());
            llenarTabla(tablaStockBajo, reporteDAO.consultarStockBajo());
            llenarTabla(tablaSaldosFacturas, reporteDAO.consultarSaldosFacturas());
            llenarTabla(tablaProductividad, reporteDAO.consultarProductividadMecanicos());
            llenarTabla(tablaServicios, reporteDAO.consultarServiciosMasSolicitados());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar vistas de reportes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void llenarTabla(JTable tabla, TablaResultado res) {
        DefaultTableModel model = new DefaultTableModel(res.getColumnas().stream().map(FrmReportes::encabezado).toArray(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] fila : res.getFilas()) {
            Object[] legible = new Object[fila.length];
            for (int i = 0; i < fila.length; i++) {
                legible[i] = FrmBaseOrden.formatear(fila[i]);
            }
            model.addRow(legible);
        }
        tabla.setModel(model);
    }

    private static final java.util.Map<String, String> TILDES = java.util.Map.ofEntries(
            java.util.Map.entry("NUMERO", "NÚMERO"), java.util.Map.entry("MECANICO", "MECÁNICO"),
            java.util.Map.entry("VEHICULO", "VEHÍCULO"), java.util.Map.entry("CATEGORIA", "CATEGORÍA"),
            java.util.Map.entry("MINIMO", "MÍNIMO"), java.util.Map.entry("TELEFONO", "TELÉFONO"),
            java.util.Map.entry("EMISION", "EMISIÓN"), java.util.Map.entry("CODIGO", "CÓDIGO"),
            java.util.Map.entry("DIAS", "DÍAS"), java.util.Map.entry("ULTIMO", "ÚLTIMO"),
            java.util.Map.entry("DESCRIPCION", "DESCRIPCIÓN"), java.util.Map.entry("ESPECIALIDAD", "ESPECIALIDAD"));

    /** Encabezado con tildes: "NUMERO ORDEN" pasa a "NÚMERO ORDEN". */
    private static String encabezado(Object columna) {
        StringBuilder sb = new StringBuilder();
        for (String p : String.valueOf(columna).split(" ")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(TILDES.getOrDefault(p, p));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmReportes().setVisible(true));
    }
}
