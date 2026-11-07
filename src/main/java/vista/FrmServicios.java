package vista;

import modelo.Repuesto;
import modelo.Servicio;
import modelo.ServicioFactory;
import modelo.enums.NivelComplejidad;
import servicio.CatalogoService;
import servicio.InventarioService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class FrmServicios extends JFrame {

    private final CatalogoService catalogoService;
    private final InventarioService inventarioService;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JComboBox<NivelComplejidad> cmbNivel;
    private JSpinner spnCostoBase;
    private JSpinner spnHoras;
    private JLabel lblCostoFinalCalculado;
    private JLabel lblGarantia;
    private JLabel lblRequiereQC;
    private JCheckBox chkActivo;

    private JTable tablaServicios;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> cmbFiltroNivel;

    private DefaultListModel<String> modeloListaRepuestos;
    private JList<String> lstRepuestosAsociados;

    public FrmServicios() {
        this.catalogoService = new CatalogoService();
        this.inventarioService = new InventarioService();
        initComponents();
        cargarServicios(0);
    }

    private void initComponents() {
        setTitle("AutoTech Manager — Catálogo de Servicios Automotrices");
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(EstiloUI.AZUL_OSCURO);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("MÓDULO 2: CATÁLOGO DE SERVICIOS POR NIVEL DE COMPLEJIDAD");
        lblTitulo.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Polimorfismo aplicado: recargo técnico, días de garantía y control de calidad según nivel");
        lblSub.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblSub.setForeground(new Color(0xD1D5DB));
        pnlHeader.add(lblSub, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 10),
                BorderFactory.createTitledBorder("Parámetros del Servicio")
        ));
        pnlForm.setPreferredSize(new Dimension(420, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = EstiloUI.campoCompacto(new JTextField());
        txtId.setEditable(false);
        txtId.setEnabled(false);

        txtCodigo = EstiloUI.campoCompacto(new JTextField());
        txtNombre = EstiloUI.campoCompacto(new JTextField());

        txtDescripcion = new JTextArea(2, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setFont(EstiloUI.fuente(Font.PLAIN, 12f));

        cmbNivel = new JComboBox<>(NivelComplejidad.values());
        cmbNivel.addActionListener(e -> recalcularValoresPolimorficos());

        spnCostoBase = new JSpinner(new SpinnerNumberModel(500.0, 50.0, 50000.0, 50.0));
        spnCostoBase.addChangeListener(e -> recalcularValoresPolimorficos());

        spnHoras = new JSpinner(new SpinnerNumberModel(1.5, 0.25, 100.0, 0.5));

        lblCostoFinalCalculado = new JLabel("Lps. 500.00");
        lblCostoFinalCalculado.setFont(EstiloUI.fuente(Font.BOLD, 14f));
        lblCostoFinalCalculado.setForeground(EstiloUI.PRIMARIO);

        lblGarantia = new JLabel("15 días");
        lblGarantia.setFont(EstiloUI.fuente(Font.BOLD, 12f));

        lblRequiereQC = new JLabel("No");
        lblRequiereQC.setFont(EstiloUI.fuente(Font.BOLD, 12f));

        chkActivo = new JCheckBox("Servicio Activo en Catálogo", true);

        modeloListaRepuestos = new DefaultListModel<>();
        lstRepuestosAsociados = new JList<>(modeloListaRepuestos);
        JScrollPane scrollRep = new JScrollPane(lstRepuestosAsociados);
        scrollRep.setPreferredSize(new Dimension(0, 75));

        int row = 0;
        agregarFilaForm(pnlForm, gbc, row++, "ID:", txtId);
        agregarFilaForm(pnlForm, gbc, row++, "Código (ej. SRV-001) *:", txtCodigo);
        agregarFilaForm(pnlForm, gbc, row++, "Nombre del Servicio *:", txtNombre);
        agregarFilaForm(pnlForm, gbc, row++, "Descripción:", new JScrollPane(txtDescripcion));
        agregarFilaForm(pnlForm, gbc, row++, "Nivel de Complejidad *:", cmbNivel);
        agregarFilaForm(pnlForm, gbc, row++, "Costo Base Mano Obra:", spnCostoBase);
        agregarFilaForm(pnlForm, gbc, row++, "Tiempo Estimado (Horas):", spnHoras);
        agregarFilaForm(pnlForm, gbc, row++, "Costo Final (Polimórfico):", lblCostoFinalCalculado);
        agregarFilaForm(pnlForm, gbc, row++, "Garantía Otorgada:", lblGarantia);
        agregarFilaForm(pnlForm, gbc, row++, "Control Calidad Oblig.:", lblRequiereQC);
        agregarFilaForm(pnlForm, gbc, row++, "", chkActivo);
        agregarFilaForm(pnlForm, gbc, row++, "Repuestos Sugeridos:", scrollRep);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton btnNuevo = EstiloUI.botonSecundario("Nuevo");
        JButton btnGuardar = EstiloUI.botonPrimario("Guardar");
        JButton btnAsociarRep = EstiloUI.botonSecundario("+ Repuesto");
        JButton btnDesactivar = EstiloUI.botonPeligro("Desactivar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarServicio());
        btnAsociarRep.addActionListener(e -> abrirAsociarRepuesto());
        btnDesactivar.addActionListener(e -> desactivarServicio());

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnAsociarRep);
        pnlBotones.add(btnDesactivar);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        pnlForm.add(pnlBotones, gbc);

        add(pnlForm, BorderLayout.WEST);

        JPanel pnlCentral = new JPanel(new BorderLayout(8, 8));
        pnlCentral.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 15));

        JPanel pnlFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlFiltro.add(new JLabel("Filtrar por Nivel de Complejidad:"));
        cmbFiltroNivel = new JComboBox<>(new String[]{
                "-- Todos los niveles --",
                "Nivel 1 - Mantenimiento Básico",
                "Nivel 2 - Mantenimiento Preventivo",
                "Nivel 3 - Reparación Intermedia",
                "Nivel 4 - Reparación Avanzada",
                "Nivel 5 - Reparación Mayor"
        });
        cmbFiltroNivel.addActionListener(e -> {
            int idx = cmbFiltroNivel.getSelectedIndex();
            cargarServicios(idx);
        });
        pnlFiltro.add(cmbFiltroNivel);
        pnlCentral.add(pnlFiltro, BorderLayout.NORTH);

        String[] columnas = {"ID", "Código", "Nombre", "Nivel", "Categoría", "Costo Base", "Costo Final", "Horas", "Garantía", "Activo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaServicios = new JTable(modeloTabla);
        EstiloUI.tabla(tablaServicios);

        tablaServicios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaServicios.getSelectedRow() >= 0) {
                seleccionarFila(tablaServicios.getSelectedRow());
            }
        });

        pnlCentral.add(new JScrollPane(tablaServicios), BorderLayout.CENTER);
        add(pnlCentral, BorderLayout.CENTER);

        recalcularValoresPolimorficos();
    }

    private void agregarFilaForm(JPanel pnl, GridBagConstraints gbc, int y, String etiqueta, Component cmp) {
        gbc.gridy = y;
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(EstiloUI.fuente(Font.BOLD, 12f));
        pnl.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        pnl.add(cmp, gbc);
    }

    private void recalcularValoresPolimorficos() {
        NivelComplejidad nivel = (NivelComplejidad) cmbNivel.getSelectedItem();
        if (nivel == null) return;

        Servicio dummy = ServicioFactory.crear(nivel);
        dummy.setCostoManoObra(BigDecimal.valueOf(((Number) spnCostoBase.getValue()).doubleValue()));

        BigDecimal costoFinal = dummy.calcularCosto();
        lblCostoFinalCalculado.setText("Lps. " + costoFinal.toPlainString());
        lblGarantia.setText(dummy.getDiasGarantia() + " días de respaldo");
        lblRequiereQC.setText(dummy.requiereControlCalidad() ? "SÍ (Obligatorio)" : "No");
        lblRequiereQC.setForeground(dummy.requiereControlCalidad() ? EstiloUI.ERROR : EstiloUI.PRIMARIO);
    }

    private void cargarServicios(int nivelFiltro) {
        try {
            modeloTabla.setRowCount(0);
            List<Servicio> lista = (nivelFiltro <= 0)
                    ? catalogoService.listarTodos()
                    : catalogoService.listarPorNivel(nivelFiltro);

            for (Servicio s : lista) {
                modeloTabla.addRow(new Object[]{
                        s.getId(),
                        s.getCodigo(),
                        s.getNombre(),
                        "Nivel " + s.getNivel().getNivel(),
                        s.getNivel().getCategoria(),
                        "Lps. " + s.getCostoManoObra(),
                        "Lps. " + s.calcularCosto(),
                        s.getTiempoEstimadoHoras() + " hrs",
                        s.getDiasGarantia() + " d",
                        s.isActivo() ? "SÍ" : "NO"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar catálogo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFila(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        try {
            catalogoService.buscarPorId(id).ifPresent(s -> {
                txtId.setText(String.valueOf(s.getId()));
                txtCodigo.setText(s.getCodigo());
                txtNombre.setText(s.getNombre());
                txtDescripcion.setText(s.getDescripcion() != null ? s.getDescripcion() : "");
                cmbNivel.setSelectedItem(s.getNivel());
                spnCostoBase.setValue(s.getCostoManoObra().doubleValue());
                spnHoras.setValue(s.getTiempoEstimadoHoras());
                chkActivo.setSelected(s.isActivo());

                modeloListaRepuestos.clear();
                if (s.getRepuestosAsociados() != null) {
                    for (Repuesto r : s.getRepuestosAsociados()) {
                        modeloListaRepuestos.addElement("[" + r.getCodigo() + "] " + r.getNombre() + " (Stock: " + r.getStock() + ")");
                    }
                }
                recalcularValoresPolimorficos();
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar servicio: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        cmbNivel.setSelectedIndex(0);
        spnCostoBase.setValue(500.0);
        spnHoras.setValue(1.5);
        chkActivo.setSelected(true);
        modeloListaRepuestos.clear();
        tablaServicios.clearSelection();
        recalcularValoresPolimorficos();
    }

    private void guardarServicio() {
        try {
            NivelComplejidad nivel = (NivelComplejidad) cmbNivel.getSelectedItem();
            Servicio s = ServicioFactory.crear(nivel);
            s.setCodigo(txtCodigo.getText().trim());
            s.setNombre(txtNombre.getText().trim());
            s.setDescripcion(txtDescripcion.getText().trim());
            s.setCostoManoObra(BigDecimal.valueOf(((Number) spnCostoBase.getValue()).doubleValue()));
            s.setTiempoEstimadoHoras(((Number) spnHoras.getValue()).doubleValue());
            s.setActivo(chkActivo.isSelected());

            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                catalogoService.registrarServicio(s);
                JOptionPane.showMessageDialog(this, "Servicio registrado correctamente en el catálogo.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                s.setId(Integer.parseInt(idStr));
                catalogoService.actualizarServicio(s);
                JOptionPane.showMessageDialog(this, "Servicio actualizado correctamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            limpiarFormulario();
            cargarServicios(cmbFiltroNivel.getSelectedIndex());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación / Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void abrirAsociarRepuesto() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un servicio primero para asociarle repuestos sugeridos.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int servicioId = Integer.parseInt(idStr);

        try {
            List<Repuesto> repuestos = inventarioService.listarRepuestosActivos();
            if (repuestos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay repuestos registrados en el inventario.");
                return;
            }

            JComboBox<Repuesto> cmbRep = new JComboBox<>(repuestos.toArray(new Repuesto[0]));
            JSpinner spnCant = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));

            JPanel pnl = new JPanel(new GridLayout(2, 2, 5, 5));
            pnl.add(new JLabel("Seleccione Repuesto:"));
            pnl.add(cmbRep);
            pnl.add(new JLabel("Cantidad sugerida:"));
            pnl.add(spnCant);

            int opc = JOptionPane.showConfirmDialog(this, pnl, "Asociar Repuesto al Servicio", JOptionPane.OK_CANCEL_OPTION);
            if (opc == JOptionPane.OK_OPTION) {
                Repuesto sel = (Repuesto) cmbRep.getSelectedItem();
                int cant = (int) spnCant.getValue();
                if (sel != null) {
                    catalogoService.asociarRepuesto(servicioId, sel.getId(), cant);
                    JOptionPane.showMessageDialog(this, "Repuesto asociado correctamente.");

                    catalogoService.buscarPorId(servicioId).ifPresent(s -> {
                        modeloListaRepuestos.clear();
                        for (Repuesto r : s.getRepuestosAsociados()) {
                            modeloListaRepuestos.addElement("[" + r.getCodigo() + "] " + r.getNombre());
                        }
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al asociar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desactivarServicio() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un servicio para desactivar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de desactivar este servicio del catálogo?",
                "Confirmar Desactivación", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                catalogoService.desactivarServicio(id);
                JOptionPane.showMessageDialog(this, "Servicio desactivado.");
                limpiarFormulario();
                cargarServicios(cmbFiltroNivel.getSelectedIndex());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al desactivar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmServicios().setVisible(true));
    }
}
