package vista;

import modelo.Especialidad;
import modelo.Mecanico;
import servicio.MecanicoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FrmMecanicos extends JFrame {

    private final MecanicoService mecanicoService;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtIdentidad;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JComboBox<String> cmbCertificacion;
    private JSpinner spnTarifa;
    private JCheckBox chkDisponible;
    private JCheckBox chkActivo;

    private DefaultListModel<EspecialidadItem> modeloListaEspecialidades;
    private JList<EspecialidadItem> lstEspecialidades;

    private JTable tablaMecanicos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;

    public static class EspecialidadItem {
        private final int id;
        private final String nombre;

        public EspecialidadItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public FrmMecanicos() {
        this.mecanicoService = new MecanicoService();
        initComponents();
        cargarEspecialidadesCatalogo();
        cargarMecanicos("");
    }

    private void initComponents() {
        setTitle("AutoTech Manager — Personal Técnico y Especialidades");
        setSize(1080, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(EstiloUI.AZUL_OSCURO);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("MÓDULO 4: GESTIÓN DE MECÁNICOS Y DISPONIBILIDAD");
        lblTitulo.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Control de personal técnico, tarifas por hora, especialidades y bloqueo de inactivos");
        lblSub.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblSub.setForeground(new Color(0xD1D5DB));
        pnlHeader.add(lblSub, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 10),
                BorderFactory.createTitledBorder("Ficha del Técnico")
        ));
        pnlForm.setPreferredSize(new Dimension(410, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = EstiloUI.campoCompacto(new JTextField());
        txtId.setEditable(false);
        txtId.setEnabled(false);

        txtCodigo = EstiloUI.campoCompacto(new JTextField());
        txtIdentidad = EstiloUI.campoCompacto(new JTextField());
        txtNombre = EstiloUI.campoCompacto(new JTextField());
        txtApellido = EstiloUI.campoCompacto(new JTextField());
        txtTelefono = EstiloUI.campoCompacto(new JTextField());
        txtCorreo = EstiloUI.campoCompacto(new JTextField());

        cmbCertificacion = new JComboBox<>(new String[]{"JUNIOR", "SENIOR", "MASTER"});
        spnTarifa = new JSpinner(new SpinnerNumberModel(350.0, 50.0, 5000.0, 50.0));

        chkDisponible = new JCheckBox("Disponible para órdenes", true);
        chkActivo = new JCheckBox("Activo en nómina", true);

        modeloListaEspecialidades = new DefaultListModel<>();
        lstEspecialidades = new JList<>(modeloListaEspecialidades);
        lstEspecialidades.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstEspecialidades.setVisibleRowCount(4);
        JScrollPane scrollEsp = new JScrollPane(lstEspecialidades);
        scrollEsp.setPreferredSize(new Dimension(0, 90));

        int row = 0;
        agregarFilaForm(pnlForm, gbc, row++, "ID:", txtId);
        agregarFilaForm(pnlForm, gbc, row++, "Código Empleado *:", txtCodigo);
        agregarFilaForm(pnlForm, gbc, row++, "Identificación / DNI *:", txtIdentidad);
        agregarFilaForm(pnlForm, gbc, row++, "Nombre *:", txtNombre);
        agregarFilaForm(pnlForm, gbc, row++, "Apellido:", txtApellido);
        agregarFilaForm(pnlForm, gbc, row++, "Teléfono:", txtTelefono);
        agregarFilaForm(pnlForm, gbc, row++, "Correo electrónico:", txtCorreo);
        agregarFilaForm(pnlForm, gbc, row++, "Nivel Certificación:", cmbCertificacion);
        agregarFilaForm(pnlForm, gbc, row++, "Tarifa Mano de Obra (Lps/Hr):", spnTarifa);

        JPanel pnlChecks = new JPanel(new GridLayout(1, 2));
        pnlChecks.add(chkDisponible);
        pnlChecks.add(chkActivo);
        agregarFilaForm(pnlForm, gbc, row++, "Estado:", pnlChecks);

        agregarFilaForm(pnlForm, gbc, row++, "Especialidades (Ctrl+Clic):", scrollEsp);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton btnNuevo = EstiloUI.botonSecundario("Nuevo");
        JButton btnGuardar = EstiloUI.botonPrimario("Guardar");
        JButton btnToggleDisp = EstiloUI.botonSecundario("Disp / Ocup");
        JButton btnDesactivar = EstiloUI.botonPeligro("Desactivar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarMecanico());
        btnToggleDisp.addActionListener(e -> toggleDisponibilidad());
        btnDesactivar.addActionListener(e -> desactivarMecanico());

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnToggleDisp);
        pnlBotones.add(btnDesactivar);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        pnlForm.add(pnlBotones, gbc);

        add(pnlForm, BorderLayout.WEST);

        JPanel pnlCentral = new JPanel(new BorderLayout(8, 8));
        pnlCentral.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 15));

        JPanel pnlBuscar = new JPanel(new BorderLayout(5, 5));
        pnlBuscar.add(new JLabel("Filtrar Mecánicos (Código / Nombre / DNI):"), BorderLayout.NORTH);
        txtBuscar = EstiloUI.campoCompacto(new JTextField());
        JButton btnBuscar = EstiloUI.botonPrimario("Buscar");
        JButton btnVerTodos = EstiloUI.botonSecundario("Ver Todos");
        JButton btnSoloDisp = EstiloUI.botonSecundario("Solo Disponibles");

        btnBuscar.addActionListener(e -> cargarMecanicos(txtBuscar.getText()));
        btnVerTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cargarMecanicos("");
        });
        btnSoloDisp.addActionListener(e -> cargarDisponibles());
        txtBuscar.addActionListener(e -> cargarMecanicos(txtBuscar.getText()));

        JPanel pnlAccionesBuscador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlAccionesBuscador.add(btnBuscar);
        pnlAccionesBuscador.add(btnVerTodos);
        pnlAccionesBuscador.add(btnSoloDisp);

        pnlBuscar.add(txtBuscar, BorderLayout.CENTER);
        pnlBuscar.add(pnlAccionesBuscador, BorderLayout.EAST);
        pnlCentral.add(pnlBuscar, BorderLayout.NORTH);

        String[] columnas = {"ID", "Código", "DNI", "Nombre Completo", "Nivel", "Tarifa/Hr", "Disponible", "Activo", "Especialidades"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaMecanicos = new JTable(modeloTabla);
        EstiloUI.tabla(tablaMecanicos);

        tablaMecanicos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaMecanicos.getSelectedRow() >= 0) {
                seleccionarFila(tablaMecanicos.getSelectedRow());
            }
        });

        pnlCentral.add(new JScrollPane(tablaMecanicos), BorderLayout.CENTER);
        add(pnlCentral, BorderLayout.CENTER);
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

    private void cargarEspecialidadesCatalogo() {
        try {
            modeloListaEspecialidades.clear();
            List<Especialidad> lista = mecanicoService.listarTodasEspecialidades();
            for (Especialidad esp : lista) {
                modeloListaEspecialidades.addElement(new EspecialidadItem(esp.getId(), esp.getNombre()));
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar especialidades: " + ex.getMessage());
        }
    }

    private void cargarMecanicos(String filtro) {
        try {
            modeloTabla.setRowCount(0);
            List<Mecanico> lista = mecanicoService.listarTodos();

            for (Mecanico m : lista) {
                if (filtro != null && !filtro.isBlank()) {
                    String f = filtro.toLowerCase();
                    boolean match = m.getCodigoEmpleado().toLowerCase().contains(f)
                            || m.getNombreCompleto().toLowerCase().contains(f)
                            || m.getIdentidad().toLowerCase().contains(f);
                    if (!match) continue;
                }

                StringBuilder sbEsp = new StringBuilder();
                if (m.getEspecialidades() != null) {
                    for (int i = 0; i < m.getEspecialidades().size(); i++) {
                        if (i > 0) sbEsp.append(", ");
                        sbEsp.append(m.getEspecialidades().get(i).getNombre());
                    }
                }

                modeloTabla.addRow(new Object[]{
                        m.getId(),
                        m.getCodigoEmpleado(),
                        m.getIdentidad(),
                        m.getNombreCompleto(),
                        m.getNivelCertificacion(),
                        "Lps. " + m.getTarifaHora(),
                        m.isDisponible() ? "SÍ (Libre)" : "NO (Ocupado)",
                        m.isActivo() ? "SÍ" : "NO",
                        sbEsp.toString()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar mecánicos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDisponibles() {
        try {
            modeloTabla.setRowCount(0);
            List<Mecanico> lista = mecanicoService.listarDisponibles();
            for (Mecanico m : lista) {
                StringBuilder sbEsp = new StringBuilder();
                if (m.getEspecialidades() != null) {
                    for (int i = 0; i < m.getEspecialidades().size(); i++) {
                        if (i > 0) sbEsp.append(", ");
                        sbEsp.append(m.getEspecialidades().get(i).getNombre());
                    }
                }
                modeloTabla.addRow(new Object[]{
                        m.getId(),
                        m.getCodigoEmpleado(),
                        m.getIdentidad(),
                        m.getNombreCompleto(),
                        m.getNivelCertificacion(),
                        "Lps. " + m.getTarifaHora(),
                        "SÍ (Libre)",
                        "SÍ",
                        sbEsp.toString()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFila(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        try {
            mecanicoService.buscarPorId(id).ifPresent(m -> {
                txtId.setText(String.valueOf(m.getId()));
                txtCodigo.setText(m.getCodigoEmpleado());
                txtIdentidad.setText(m.getIdentidad());
                txtNombre.setText(m.getNombre());
                txtApellido.setText(m.getApellido());
                txtTelefono.setText(m.getTelefono());
                txtCorreo.setText(m.getCorreo());
                cmbCertificacion.setSelectedItem(m.getNivelCertificacion());
                spnTarifa.setValue(m.getTarifaHora().doubleValue());
                chkDisponible.setSelected(m.isDisponible());
                chkActivo.setSelected(m.isActivo());

                lstEspecialidades.clearSelection();
                if (m.getEspecialidades() != null) {
                    List<Integer> indices = new ArrayList<>();
                    for (int i = 0; i < modeloListaEspecialidades.size(); i++) {
                        EspecialidadItem item = modeloListaEspecialidades.getElementAt(i);
                        for (Especialidad esp : m.getEspecialidades()) {
                            if (esp.getId() == item.getId()) {
                                indices.add(i);
                                break;
                            }
                        }
                    }
                    int[] arr = indices.stream().mapToInt(Integer::intValue).toArray();
                    lstEspecialidades.setSelectedIndices(arr);
                }
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar mecánico: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtCodigo.setText("");
        txtIdentidad.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        cmbCertificacion.setSelectedIndex(1);
        spnTarifa.setValue(350.0);
        chkDisponible.setSelected(true);
        chkActivo.setSelected(true);
        lstEspecialidades.clearSelection();
        tablaMecanicos.clearSelection();
    }

    private void guardarMecanico() {
        try {
            Mecanico m = new Mecanico();
            m.setCodigoEmpleado(txtCodigo.getText().trim());
            m.setIdentidad(txtIdentidad.getText().trim());
            m.setNombre(txtNombre.getText().trim());
            m.setApellido(txtApellido.getText().trim());
            m.setTelefono(txtTelefono.getText().trim());
            m.setCorreo(txtCorreo.getText().trim());
            m.setNivelCertificacion((String) cmbCertificacion.getSelectedItem());
            m.setTarifaHora(BigDecimal.valueOf(((Number) spnTarifa.getValue()).doubleValue()));
            m.setDisponible(chkDisponible.isSelected());
            m.setActivo(chkActivo.isSelected());

            List<Integer> espIds = new ArrayList<>();
            for (EspecialidadItem item : lstEspecialidades.getSelectedValuesList()) {
                espIds.add(item.getId());
            }

            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                mecanicoService.registrarMecanico(m, espIds);
                JOptionPane.showMessageDialog(this, "Mecánico registrado correctamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                m.setId(Integer.parseInt(idStr));
                mecanicoService.actualizarMecanico(m, espIds);
                JOptionPane.showMessageDialog(this, "Mecánico actualizado correctamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            limpiarFormulario();
            cargarMecanicos("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación / Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void toggleDisponibilidad() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un mecánico para cambiar su disponibilidad.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        boolean nuevaDisp = !chkDisponible.isSelected();
        try {
            mecanicoService.cambiarDisponibilidad(id, nuevaDisp);
            chkDisponible.setSelected(nuevaDisp);
            JOptionPane.showMessageDialog(this, "Disponibilidad cambiada a: " + (nuevaDisp ? "DISPONIBLE" : "NO DISPONIBLE / OCUPADO"));
            cargarMecanicos("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cambiar disponibilidad: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desactivarMecanico() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un mecánico para desactivar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de desactivar al mecánico? No podrá ser asignado a órdenes de trabajo.",
                "Confirmar Desactivación", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                mecanicoService.desactivarMecanico(id);
                JOptionPane.showMessageDialog(this, "Mecánico desactivado correctamente.");
                limpiarFormulario();
                cargarMecanicos("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmMecanicos().setVisible(true));
    }
}
