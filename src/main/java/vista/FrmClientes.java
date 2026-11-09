package vista;

import modelo.Cliente;
import servicio.ClienteService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmClientes extends JFrame {

    private final ClienteService clienteService;

    private JTextField txtId;
    private JTextField txtIdentidad;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtDireccion;
    private JComboBox<String> cmbTipoCliente;
    private JCheckBox chkActivo;

    private JTextField txtBuscar;
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;

    public FrmClientes() {
        this.clienteService = new ClienteService();
        initComponents();
        cargarClientes("");
    }

    private void initComponents() {
        setTitle("AutoTech Manager — Gestión de Clientes");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(EstiloUI.AZUL_OSCURO);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("MÓDULO 1: ADMINISTRACIÓN DE CLIENTES");
        lblTitulo.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Registro, actualización, consulta y desactivación de propietarios");
        lblSub.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblSub.setForeground(new Color(0xD1D5DB));
        pnlHeader.add(lblSub, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 10),
                BorderFactory.createTitledBorder("Datos del Cliente")
        ));
        pnlForm.setPreferredSize(new Dimension(360, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = EstiloUI.campoCompacto(new JTextField());
        txtId.setEditable(false);
        txtId.setEnabled(false);

        txtIdentidad = EstiloUI.campoCompacto(new JTextField());
        txtNombre = EstiloUI.campoCompacto(new JTextField());
        txtApellido = EstiloUI.campoCompacto(new JTextField());
        txtTelefono = EstiloUI.campoCompacto(new JTextField());
        txtCorreo = EstiloUI.campoCompacto(new JTextField());
        txtDireccion = EstiloUI.campoCompacto(new JTextField());

        cmbTipoCliente = new JComboBox<>(new String[]{"PARTICULAR", "CORPORATIVO"});
        chkActivo = new JCheckBox("Cliente Activo", true);

        int row = 0;
        agregarFilaForm(pnlForm, gbc, row++, "ID (Auto):", txtId);
        agregarFilaForm(pnlForm, gbc, row++, "Identificación / RTN *:", txtIdentidad);
        agregarFilaForm(pnlForm, gbc, row++, "Nombre *:", txtNombre);
        agregarFilaForm(pnlForm, gbc, row++, "Apellido:", txtApellido);
        agregarFilaForm(pnlForm, gbc, row++, "Teléfono:", txtTelefono);
        agregarFilaForm(pnlForm, gbc, row++, "Correo electrónico:", txtCorreo);
        agregarFilaForm(pnlForm, gbc, row++, "Dirección:", txtDireccion);
        agregarFilaForm(pnlForm, gbc, row++, "Tipo de Cliente:", cmbTipoCliente);
        agregarFilaForm(pnlForm, gbc, row++, "", chkActivo);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        JButton btnNuevo = EstiloUI.botonSecundario("Nuevo / Limpiar");
        JButton btnGuardar = EstiloUI.botonPrimario("Guardar");
        JButton btnDesactivar = EstiloUI.botonPeligro("Desactivar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarCliente());
        btnDesactivar.addActionListener(e -> desactivarCliente());

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnDesactivar);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        pnlForm.add(pnlBotones, gbc);

        add(pnlForm, BorderLayout.WEST);

        JPanel pnlCentral = new JPanel(new BorderLayout(8, 8));
        pnlCentral.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 15));

        JPanel pnlBuscar = new JPanel(new BorderLayout(5, 5));
        JLabel lblBuscar = new JLabel("Buscar por Nombre / Identidad / Teléfono:");
        lblBuscar.setFont(EstiloUI.fuente(Font.BOLD, 12f));
        txtBuscar = EstiloUI.campoCompacto(new JTextField());
        JButton btnBuscar = EstiloUI.botonPrimario("Buscar");
        JButton btnRefrescar = EstiloUI.botonSecundario("Ver Todos");

        btnBuscar.addActionListener(e -> cargarClientes(txtBuscar.getText()));
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarClientes("");
        });
        txtBuscar.addActionListener(e -> cargarClientes(txtBuscar.getText()));

        JPanel pnlBuscarAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlBuscarAcciones.add(btnBuscar);
        pnlBuscarAcciones.add(btnRefrescar);

        pnlBuscar.add(lblBuscar, BorderLayout.NORTH);
        pnlBuscar.add(txtBuscar, BorderLayout.CENTER);
        pnlBuscar.add(pnlBuscarAcciones, BorderLayout.EAST);

        pnlCentral.add(pnlBuscar, BorderLayout.NORTH);

        String[] columnas = {"ID", "Identificación", "Nombre Completo", "Teléfono", "Correo", "Tipo", "Activo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaClientes = new JTable(modeloTabla);
        EstiloUI.tabla(tablaClientes);

        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaClientes.getSelectedRow() >= 0) {
                seleccionarFila(tablaClientes.getSelectedRow());
            }
        });

        JScrollPane scroll = new JScrollPane(tablaClientes);
        pnlCentral.add(scroll, BorderLayout.CENTER);

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

    private void cargarClientes(String filtro) {
        try {
            modeloTabla.setRowCount(0);
            List<Cliente> lista = (filtro == null || filtro.isBlank())
                    ? clienteService.listarTodos()
                    : clienteService.buscar(filtro);

            for (Cliente c : lista) {
                modeloTabla.addRow(new Object[]{
                        c.getId(),
                        c.getIdentidad(),
                        c.getNombreCompleto(),
                        c.getTelefono() != null ? c.getTelefono() : "",
                        c.getCorreo() != null ? c.getCorreo() : "",
                        c.getTipoCliente() != null ? c.getTipoCliente() : "PARTICULAR",
                        c.isActivo() ? "SÍ" : "NO"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFila(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        try {
            clienteService.buscarPorId(id).ifPresent(c -> {
                txtId.setText(String.valueOf(c.getId()));
                txtIdentidad.setText(c.getIdentidad());
                txtNombre.setText(c.getNombre());
                txtApellido.setText(c.getApellido());
                txtTelefono.setText(c.getTelefono());
                txtCorreo.setText(c.getCorreo());
                txtDireccion.setText(c.getDireccion());
                if (c.getTipoCliente() != null) {
                    cmbTipoCliente.setSelectedItem(c.getTipoCliente());
                }
                chkActivo.setSelected(c.isActivo());
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar cliente: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtIdentidad.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        cmbTipoCliente.setSelectedIndex(0);
        chkActivo.setSelected(true);
        tablaClientes.clearSelection();
    }

    private void guardarCliente() {
        try {
            String idStr = txtId.getText().trim();
            Cliente c = new Cliente();
            c.setIdentidad(txtIdentidad.getText().trim());
            c.setNombre(txtNombre.getText().trim());
            c.setApellido(txtApellido.getText().trim());
            c.setTelefono(txtTelefono.getText().trim());
            c.setCorreo(txtCorreo.getText().trim());
            c.setDireccion(txtDireccion.getText().trim());
            c.setTipoCliente((String) cmbTipoCliente.getSelectedItem());
            c.setActivo(chkActivo.isSelected());

            if (idStr.isEmpty()) {
                clienteService.registrarCliente(c);
                JOptionPane.showMessageDialog(this, "Cliente registrado exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                c.setId(Integer.parseInt(idStr));
                clienteService.actualizarCliente(c);
                JOptionPane.showMessageDialog(this, "Cliente actualizado exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            limpiarFormulario();
            cargarClientes("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación / Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void desactivarCliente() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla para desactivar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de desactivar al cliente seleccionado? (Borrado lógico)",
                "Confirmar Desactivación", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                clienteService.desactivarCliente(id);
                JOptionPane.showMessageDialog(this, "Cliente desactivado correctamente.");
                limpiarFormulario();
                cargarClientes("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al desactivar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmClientes().setVisible(true));
    }
}
