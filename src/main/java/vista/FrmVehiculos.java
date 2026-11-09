package vista;

import modelo.Cliente;
import modelo.Vehiculo;
import modelo.enums.TipoCombustible;
import servicio.ClienteService;
import servicio.VehiculoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmVehiculos extends JFrame {

    private final VehiculoService vehiculoService;
    private final ClienteService clienteService;

    private JTextField txtId;
    private JComboBox<ClienteComboItem> cmbCliente;
    private JTextField txtPlaca;
    private JTextField txtVin;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JSpinner spnAnio;
    private JTextField txtColor;
    private JSpinner spnKilometraje;
    private JComboBox<TipoCombustible> cmbCombustible;
    private JTextField txtSerieMotor;
    private JCheckBox chkActivo;

    private JTextField txtBuscar;
    private JComboBox<ClienteComboItem> cmbFiltroCliente;
    private JTable tablaVehiculos;
    private DefaultTableModel modeloTabla;

    public static class ClienteComboItem {
        private final int id;
        private final String nombre;
        private final String identidad;

        public ClienteComboItem(int id, String nombre, String identidad) {
            this.id = id;
            this.nombre = nombre;
            this.identidad = identidad;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return id <= 0 ? "-- Todos los clientes --" : nombre + " (" + identidad + ")";
        }
    }

    public FrmVehiculos() {
        this.vehiculoService = new VehiculoService();
        this.clienteService = new ClienteService();
        initComponents();
        cargarComboClientes();
        cargarVehiculos("");
    }

    private void initComponents() {
        setTitle("AutoTech Manager — Gestión de Vehículos");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(EstiloUI.AZUL_OSCURO);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("MÓDULO 1: ADMINISTRACIÓN DE VEHÍCULOS");
        lblTitulo.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Asociación a clientes, validación de placas hondureñas y VIN de 17 caracteres");
        lblSub.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblSub.setForeground(new Color(0xD1D5DB));
        pnlHeader.add(lblSub, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 10),
                BorderFactory.createTitledBorder("Ficha Técnica del Vehículo")
        ));
        pnlForm.setPreferredSize(new Dimension(390, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = EstiloUI.campoCompacto(new JTextField());
        txtId.setEditable(false);
        txtId.setEnabled(false);

        cmbCliente = new JComboBox<>();
        txtPlaca = EstiloUI.campoCompacto(new JTextField());
        txtVin = EstiloUI.campoCompacto(new JTextField());
        txtMarca = EstiloUI.campoCompacto(new JTextField());
        txtModelo = EstiloUI.campoCompacto(new JTextField());

        spnAnio = new JSpinner(new SpinnerNumberModel(2020, 1950, 2030, 1));
        txtColor = EstiloUI.campoCompacto(new JTextField());
        spnKilometraje = new JSpinner(new SpinnerNumberModel(50000, 0, 9999999, 1000));
        cmbCombustible = new JComboBox<>(TipoCombustible.values());
        txtSerieMotor = EstiloUI.campoCompacto(new JTextField());
        chkActivo = new JCheckBox("Vehículo Activo", true);

        int row = 0;
        agregarFilaForm(pnlForm, gbc, row++, "ID:", txtId);
        agregarFilaForm(pnlForm, gbc, row++, "Cliente Propietario *:", cmbCliente);
        agregarFilaForm(pnlForm, gbc, row++, "Placa (ej. HAA 1234) *:", txtPlaca);
        agregarFilaForm(pnlForm, gbc, row++, "VIN / Chasis (17 car.) *:", txtVin);
        agregarFilaForm(pnlForm, gbc, row++, "Marca *:", txtMarca);
        agregarFilaForm(pnlForm, gbc, row++, "Modelo *:", txtModelo);
        agregarFilaForm(pnlForm, gbc, row++, "Año de Fabricación:", spnAnio);
        agregarFilaForm(pnlForm, gbc, row++, "Color:", txtColor);
        agregarFilaForm(pnlForm, gbc, row++, "Kilometraje Actual:", spnKilometraje);
        agregarFilaForm(pnlForm, gbc, row++, "Tipo de Combustible:", cmbCombustible);
        agregarFilaForm(pnlForm, gbc, row++, "Serie Motor Actual:", txtSerieMotor);
        agregarFilaForm(pnlForm, gbc, row++, "", chkActivo);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        JButton btnNuevo = EstiloUI.botonSecundario("Nuevo");
        JButton btnGuardar = EstiloUI.botonPrimario("Guardar");
        JButton btnDesactivar = EstiloUI.botonPeligro("Desactivar");

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarVehiculo());
        btnDesactivar.addActionListener(e -> desactivarVehiculo());

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

        JPanel pnlFiltros = new JPanel(new GridLayout(2, 1, 4, 4));

        JPanel pnlFiltroCliente = new JPanel(new BorderLayout(5, 5));
        pnlFiltroCliente.add(new JLabel("Filtrar por Cliente:"), BorderLayout.WEST);
        cmbFiltroCliente = new JComboBox<>();
        cmbFiltroCliente.addActionListener(e -> filtrarPorCliente());
        pnlFiltroCliente.add(cmbFiltroCliente, BorderLayout.CENTER);

        JPanel pnlBuscar = new JPanel(new BorderLayout(5, 5));
        pnlBuscar.add(new JLabel("Buscar (Placa / VIN / Modelo):"), BorderLayout.WEST);
        txtBuscar = EstiloUI.campoCompacto(new JTextField());
        JButton btnBuscar = EstiloUI.botonPrimario("Buscar");
        JButton btnVerTodos = EstiloUI.botonSecundario("Todos");
        JButton btnHistorial = EstiloUI.botonSecundario("Ver Historial");

        btnBuscar.addActionListener(e -> cargarVehiculos(txtBuscar.getText()));
        btnVerTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cmbFiltroCliente.setSelectedIndex(0);
            cargarVehiculos("");
        });
        txtBuscar.addActionListener(e -> cargarVehiculos(txtBuscar.getText()));

        btnHistorial.addActionListener(e -> verHistorialSeleccionado());

        JPanel pnlAccionesBuscador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlAccionesBuscador.add(btnBuscar);
        pnlAccionesBuscador.add(btnVerTodos);
        pnlAccionesBuscador.add(btnHistorial);

        pnlBuscar.add(txtBuscar, BorderLayout.CENTER);
        pnlBuscar.add(pnlAccionesBuscador, BorderLayout.EAST);

        pnlFiltros.add(pnlFiltroCliente);
        pnlFiltros.add(pnlBuscar);
        pnlCentral.add(pnlFiltros, BorderLayout.NORTH);

        String[] columnas = {"ID", "Placa", "VIN", "Marca", "Modelo", "Año", "Combustible", "Km", "Cliente Propietario", "Activo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaVehiculos = new JTable(modeloTabla);
        EstiloUI.tabla(tablaVehiculos);

        tablaVehiculos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaVehiculos.getSelectedRow() >= 0) {
                seleccionarFila(tablaVehiculos.getSelectedRow());
            }
        });

        pnlCentral.add(new JScrollPane(tablaVehiculos), BorderLayout.CENTER);
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

    private void cargarComboClientes() {
        try {
            cmbCliente.removeAllItems();
            cmbFiltroCliente.removeAllItems();
            cmbFiltroCliente.addItem(new ClienteComboItem(0, "Todos los clientes", ""));

            List<Cliente> clientes = clienteService.listarActivos();
            for (Cliente c : clientes) {
                ClienteComboItem item = new ClienteComboItem(c.getId(), c.getNombreCompleto(), c.getIdentidad());
                cmbCliente.addItem(item);
                cmbFiltroCliente.addItem(item);
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar combo clientes: " + ex.getMessage());
        }
    }

    private void cargarVehiculos(String filtro) {
        try {
            modeloTabla.setRowCount(0);
            List<Vehiculo> lista = (filtro == null || filtro.isBlank())
                    ? vehiculoService.listarTodos()
                    : vehiculoService.buscar(filtro);

            for (Vehiculo v : lista) {
                String cliNombre = (v.getCliente() != null && v.getCliente().getNombreCompleto() != null)
                        ? v.getCliente().getNombreCompleto()
                        : "ID: " + (v.getCliente() != null ? v.getCliente().getId() : "N/A");

                modeloTabla.addRow(new Object[]{
                        v.getId(),
                        v.getPlaca(),
                        v.getVin(),
                        v.getMarca(),
                        v.getModelo(),
                        v.getAnio(),
                        v.getCombustible(),
                        v.getKilometraje(),
                        cliNombre,
                        v.isActivo() ? "SÍ" : "NO"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar vehículos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarPorCliente() {
        ClienteComboItem item = (ClienteComboItem) cmbFiltroCliente.getSelectedItem();
        if (item == null || item.getId() <= 0) {
            cargarVehiculos("");
            return;
        }
        try {
            modeloTabla.setRowCount(0);
            List<Vehiculo> lista = vehiculoService.listarPorCliente(item.getId());
            for (Vehiculo v : lista) {
                String cliNombre = (v.getCliente() != null && v.getCliente().getNombreCompleto() != null)
                        ? v.getCliente().getNombreCompleto()
                        : "ID: " + (v.getCliente() != null ? v.getCliente().getId() : "N/A");

                modeloTabla.addRow(new Object[]{
                        v.getId(),
                        v.getPlaca(),
                        v.getVin(),
                        v.getMarca(),
                        v.getModelo(),
                        v.getAnio(),
                        v.getCombustible(),
                        v.getKilometraje(),
                        cliNombre,
                        v.isActivo() ? "SÍ" : "NO"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar por cliente: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFila(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        try {
            vehiculoService.buscarPorId(id).ifPresent(v -> {
                txtId.setText(String.valueOf(v.getId()));
                txtPlaca.setText(v.getPlaca());
                txtVin.setText(v.getVin());
                txtMarca.setText(v.getMarca());
                txtModelo.setText(v.getModelo());
                spnAnio.setValue(v.getAnio());
                txtColor.setText(v.getColor() != null ? v.getColor() : "");
                spnKilometraje.setValue(v.getKilometraje());
                if (v.getCombustible() != null) {
                    cmbCombustible.setSelectedItem(v.getCombustible());
                }
                txtSerieMotor.setText(v.getSerieMotorActual() != null ? v.getSerieMotorActual() : "");
                chkActivo.setSelected(v.isActivo());

                if (v.getCliente() != null) {
                    for (int i = 0; i < cmbCliente.getItemCount(); i++) {
                        if (cmbCliente.getItemAt(i).getId() == v.getCliente().getId()) {
                            cmbCliente.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar vehículo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtPlaca.setText("");
        txtVin.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        spnAnio.setValue(2020);
        txtColor.setText("");
        spnKilometraje.setValue(50000);
        cmbCombustible.setSelectedIndex(0);
        txtSerieMotor.setText("");
        chkActivo.setSelected(true);
        tablaVehiculos.clearSelection();
    }

    private void guardarVehiculo() {
        try {
            ClienteComboItem cliItem = (ClienteComboItem) cmbCliente.getSelectedItem();
            if (cliItem == null || cliItem.getId() <= 0) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un cliente propietario.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Vehiculo v = new Vehiculo();
            Cliente c = new Cliente();
            c.setId(cliItem.getId());
            v.setCliente(c);

            v.setPlaca(txtPlaca.getText().trim());
            v.setVin(txtVin.getText().trim());
            v.setMarca(txtMarca.getText().trim());
            v.setModelo(txtModelo.getText().trim());
            v.setAnio((int) spnAnio.getValue());
            v.setColor(txtColor.getText().trim());
            v.setKilometraje((int) spnKilometraje.getValue());
            v.setCombustible((TipoCombustible) cmbCombustible.getSelectedItem());
            v.setSerieMotorActual(txtSerieMotor.getText().trim());
            v.setActivo(chkActivo.isSelected());

            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                vehiculoService.registrarVehiculo(v);
                JOptionPane.showMessageDialog(this, "Vehículo registrado correctamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                v.setId(Integer.parseInt(idStr));
                vehiculoService.actualizarVehiculo(v);
                JOptionPane.showMessageDialog(this, "Vehículo actualizado correctamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            limpiarFormulario();
            cargarVehiculos("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación / Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void desactivarVehiculo() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un vehículo de la tabla para desactivar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(idStr);
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de desactivar este vehículo?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                vehiculoService.desactivarVehiculo(id);
                JOptionPane.showMessageDialog(this, "Vehículo desactivado correctamente.");
                limpiarFormulario();
                cargarVehiculos("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al desactivar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void verHistorialSeleccionado() {
        String placa = txtPlaca.getText().trim();
        if (placa.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un vehículo en la tabla para ver su expediente histórico.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FrmHistorialVehiculo frm = new FrmHistorialVehiculo(placa);
        frm.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmVehiculos().setVisible(true));
    }
}
