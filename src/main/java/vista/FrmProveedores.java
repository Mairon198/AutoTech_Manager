package vista;

import modelo.Proveedor;
import servicio.InventarioService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmProveedores extends JFrame {

    private final InventarioService inventarioService;

    private JTextField txtId;
    private JTextField txtRtn;
    private JTextField txtNombre;
    private JTextField txtContacto;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtDireccion;
    private JCheckBox chkActivo;

    private JTextField txtBuscar;
    private JTable tablaProveedores;
    private DefaultTableModel modeloTabla;

    public FrmProveedores() {
        this.inventarioService = new InventarioService();
        initComponents();
        cargarProveedores("");
    }

    private void initComponents() {
        setTitle("AutoTech Manager — Gestión de Proveedores");
        setSize(1100, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = EstiloUI.panelEncabezado();
        pnlHeader.setBackground(EstiloUI.AZUL_OSCURO);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("MÓDULO 5: ADMINISTRACIÓN DE PROVEEDORES");
        lblTitulo.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Suministradores de repuestos, lubricantes y piezas originales del taller");
        lblSub.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblSub.setForeground(EstiloUI.TEXTO_ENCABEZADO);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 10),
                BorderFactory.createTitledBorder("Datos del Proveedor")
        ));
        pnlForm.setPreferredSize(new Dimension(360, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = EstiloUI.campoCompacto(new JTextField());
        txtId.setEditable(false);
        txtId.setEnabled(false);

        txtRtn = EstiloUI.campoCompacto(new JTextField());
        txtNombre = EstiloUI.campoCompacto(new JTextField());
        txtContacto = EstiloUI.campoCompacto(new JTextField());
        txtTelefono = EstiloUI.campoCompacto(new JTextField());
        txtCorreo = EstiloUI.campoCompacto(new JTextField());
        txtDireccion = EstiloUI.campoCompacto(new JTextField());
        chkActivo = new JCheckBox("Proveedor Activo", true);

        int row = 0;
        agregarFilaForm(pnlForm, gbc, row++, "ID:", txtId);
        agregarFilaForm(pnlForm, gbc, row++, "RTN Fiscal *:", txtRtn);
        agregarFilaForm(pnlForm, gbc, row++, "Nombre / Razón Social *:", txtNombre);
        agregarFilaForm(pnlForm, gbc, row++, "Persona de Contacto:", txtContacto);
        agregarFilaForm(pnlForm, gbc, row++, "Teléfono:", txtTelefono);
        agregarFilaForm(pnlForm, gbc, row++, "Correo electrónico:", txtCorreo);
        agregarFilaForm(pnlForm, gbc, row++, "Dirección:", txtDireccion);
        agregarFilaForm(pnlForm, gbc, row++, "", chkActivo);

        JPanel pnlBotones = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton btnNuevo = EstiloUI.botonSecundario("Nuevo");
        JButton btnGuardar = EstiloUI.botonPrimario("Guardar");
        JButton btnDesactivar = EstiloUI.botonPeligro("Desactivar");
        JButton btnEliminar = EstiloUI.botonPeligro("Eliminar");
        btnEliminar.addActionListener(e -> Eliminador.eliminar(this, modelo.enums.RegistroEliminable.PROVEEDOR, txtId.getText(), () -> {
            limpiarFormulario();
            cargarProveedores("");
        }));

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarProveedor());
        btnDesactivar.addActionListener(e -> desactivarProveedor());

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnDesactivar);
        pnlBotones.add(btnEliminar);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        pnlForm.add(pnlBotones, gbc);

        add(EstiloUI.panelLateral(pnlForm, 420), BorderLayout.WEST);

        JPanel pnlCentral = new JPanel(new BorderLayout(8, 8));
        pnlCentral.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 15));

        JPanel pnlBuscar = new JPanel(new BorderLayout(5, 5));
        pnlBuscar.add(new JLabel("Buscar Proveedor (Nombre / RTN / Contacto):"), BorderLayout.NORTH);
        txtBuscar = EstiloUI.campoCompacto(new JTextField());
        JButton btnBuscar = EstiloUI.botonPrimario("Buscar");
        JButton btnVerTodos = EstiloUI.botonSecundario("Ver Todos");

        btnBuscar.addActionListener(e -> cargarProveedores(txtBuscar.getText()));
        btnVerTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cargarProveedores("");
        });
        txtBuscar.addActionListener(e -> cargarProveedores(txtBuscar.getText()));

        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlAcciones.add(btnBuscar);
        pnlAcciones.add(btnVerTodos);

        pnlBuscar.add(txtBuscar, BorderLayout.CENTER);
        pnlBuscar.add(pnlAcciones, BorderLayout.EAST);
        pnlCentral.add(pnlBuscar, BorderLayout.NORTH);

        String[] columnas = {"ID", "RTN", "Nombre / Razón Social", "Contacto", "Teléfono", "Correo", "Activo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProveedores = new JTable(modeloTabla);
        EstiloUI.tabla(tablaProveedores);

        tablaProveedores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaProveedores.getSelectedRow() >= 0) {
                seleccionarFila(tablaProveedores.getSelectedRow());
            }
        });

        pnlCentral.add(new JScrollPane(tablaProveedores), BorderLayout.CENTER);
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

    private void cargarProveedores(String filtro) {
        try {
            modeloTabla.setRowCount(0);
            List<Proveedor> lista = inventarioService.listarTodosProveedores();
            for (Proveedor p : lista) {
                if (filtro != null && !filtro.isBlank()) {
                    String f = filtro.toLowerCase();
                    boolean match = (p.getNombre() != null && p.getNombre().toLowerCase().contains(f))
                            || (p.getRtn() != null && p.getRtn().toLowerCase().contains(f))
                            || (p.getContacto() != null && p.getContacto().toLowerCase().contains(f));
                    if (!match) continue;
                }

                modeloTabla.addRow(new Object[]{
                        p.getId(),
                        p.getRtn(),
                        p.getNombre(),
                        p.getContacto() != null ? p.getContacto() : "",
                        p.getTelefono() != null ? p.getTelefono() : "",
                        p.getCorreo() != null ? p.getCorreo() : "",
                        p.isActivo() ? "SÍ" : "NO"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar proveedores: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFila(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        try {
            List<Proveedor> lista = inventarioService.listarTodosProveedores();
            for (Proveedor p : lista) {
                if (p.getId() == id) {
                    txtId.setText(String.valueOf(p.getId()));
                    txtRtn.setText(p.getRtn());
                    txtNombre.setText(p.getNombre());
                    txtContacto.setText(p.getContacto() != null ? p.getContacto() : "");
                    txtTelefono.setText(p.getTelefono() != null ? p.getTelefono() : "");
                    txtCorreo.setText(p.getCorreo() != null ? p.getCorreo() : "");
                    txtDireccion.setText(p.getDireccion() != null ? p.getDireccion() : "");
                    chkActivo.setSelected(p.isActivo());
                    break;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar proveedor: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtRtn.setText("");
        txtNombre.setText("");
        txtContacto.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        chkActivo.setSelected(true);
        tablaProveedores.clearSelection();
    }

    private void guardarProveedor() {
        try {
            Proveedor p = new Proveedor();
            p.setRtn(txtRtn.getText().trim());
            p.setNombre(txtNombre.getText().trim());
            p.setContacto(txtContacto.getText().trim());
            p.setTelefono(txtTelefono.getText().trim());
            p.setCorreo(txtCorreo.getText().trim());
            p.setDireccion(txtDireccion.getText().trim());
            p.setActivo(chkActivo.isSelected());

            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                inventarioService.registrarProveedor(p);
                JOptionPane.showMessageDialog(this, "Proveedor registrado exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                p.setId(Integer.parseInt(idStr));
                inventarioService.actualizarProveedor(p);
                JOptionPane.showMessageDialog(this, "Proveedor actualizado exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            limpiarFormulario();
            cargarProveedores("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación / Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void desactivarProveedor() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor para desactivar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de desactivar al proveedor seleccionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                inventarioService.desactivarProveedor(id);
                JOptionPane.showMessageDialog(this, "Proveedor desactivado correctamente.");
                limpiarFormulario();
                cargarProveedores("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al desactivar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmProveedores().setVisible(true));
    }
}
