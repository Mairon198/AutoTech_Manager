package vista;

import hilos.DemostracionConcurrencia;
import hilos.MonitorStockMinimo;
import modelo.MovimientoInventario;
import modelo.Proveedor;
import modelo.Repuesto;
import modelo.enums.CategoriaRepuesto;
import servicio.InventarioService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FrmInventario extends JFrame {

    private final InventarioService inventarioService;
    private final MonitorStockMinimo monitorStock;
    private final DemostracionConcurrencia demoConcurrencia;

    private JTextField txtId;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JComboBox<CategoriaRepuesto> cmbCategoria;
    private JTextField txtMarca;
    private JComboBox<Proveedor> cmbProveedor;
    private JSpinner spnStock;
    private JSpinner spnStockMinimo;
    private JSpinner spnCosto;
    private JSpinner spnPrecio;
    private JTextField txtUbicacion;
    private JCheckBox chkActivo;

    private JTable tablaRepuestos;
    private DefaultTableModel modeloRepuestos;
    private JTextField txtBuscar;

    private JTable tablaKardex;
    private DefaultTableModel modeloKardex;

    private JTextArea txtConsolaHilos;
    private JLabel lblEstadoMonitor;
    private JButton btnToggleMonitor;

    public FrmInventario() {
        this.inventarioService = new InventarioService();
        this.monitorStock = new MonitorStockMinimo(5);
        this.demoConcurrencia = new DemostracionConcurrencia(this.inventarioService);
        initComponents();
        configurarListenersHilos();
        cargarComboProveedores();
        cargarRepuestos("");
        cargarKardexGeneral();
    }

    private void initComponents() {
        setTitle("AutoTech Manager — Inventario, Kardex y Control Concurrente");
        setSize(1200, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                monitorStock.detener();
            }
        });

        JPanel pnlHeader = EstiloUI.panelEncabezado();
        pnlHeader.setBackground(EstiloUI.AZUL_OSCURO);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("MÓDULO 5: GESTIÓN DE INVENTARIO, KARDEX Y CONCURRENCIA");
        lblTitulo.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        lblEstadoMonitor = new JLabel("Monitor Stock Mínimo: DETENIDO");
        lblEstadoMonitor.setFont(EstiloUI.fuente(Font.BOLD, 12f));
        lblEstadoMonitor.setForeground(new Color(0xFCD34D));
        pnlHeader.add(lblEstadoMonitor, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstiloUI.fuente(Font.BOLD, 13f));

        tabs.addTab("1. Catálogo de repuestos", crearPanelCatalogo());
        tabs.addTab("2. Kárdex de movimientos", crearPanelKardex());
        tabs.addTab("3. Hilos y concurrencia", crearPanelHilos());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel crearPanelCatalogo() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Datos del Repuesto"));
        pnlForm.setPreferredSize(new Dimension(390, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = EstiloUI.campoCompacto(new JTextField());
        txtId.setEditable(false);
        txtId.setEnabled(false);

        txtCodigo = EstiloUI.campoCompacto(new JTextField());
        txtNombre = EstiloUI.campoCompacto(new JTextField());
        cmbCategoria = new JComboBox<>(CategoriaRepuesto.values());
        txtMarca = EstiloUI.campoCompacto(new JTextField());
        cmbProveedor = new JComboBox<>();
        spnStock = new JSpinner(new SpinnerNumberModel(10, 0, 99999, 1));
        spnStockMinimo = new JSpinner(new SpinnerNumberModel(3, 0, 9999, 1));
        spnCosto = new JSpinner(new SpinnerNumberModel(150.0, 0.0, 500000.0, 25.0));
        spnPrecio = new JSpinner(new SpinnerNumberModel(250.0, 0.0, 750000.0, 25.0));
        txtUbicacion = EstiloUI.campoCompacto(new JTextField());
        chkActivo = new JCheckBox("Repuesto Activo", true);

        int row = 0;
        agregarFilaForm(pnlForm, gbc, row++, "ID:", txtId);
        agregarFilaForm(pnlForm, gbc, row++, "Código (ej. REP-001) *:", txtCodigo);
        agregarFilaForm(pnlForm, gbc, row++, "Nombre Repuesto *:", txtNombre);
        agregarFilaForm(pnlForm, gbc, row++, "Categoría:", cmbCategoria);
        agregarFilaForm(pnlForm, gbc, row++, "Marca / Fabricante:", txtMarca);
        agregarFilaForm(pnlForm, gbc, row++, "Proveedor:", cmbProveedor);
        agregarFilaForm(pnlForm, gbc, row++, "Stock Inicial / Actual:", spnStock);
        agregarFilaForm(pnlForm, gbc, row++, "Stock Mínimo Alerta:", spnStockMinimo);
        agregarFilaForm(pnlForm, gbc, row++, "Costo Compra (Lps):", spnCosto);
        agregarFilaForm(pnlForm, gbc, row++, "Precio Venta (Lps):", spnPrecio);
        agregarFilaForm(pnlForm, gbc, row++, "Ubicación Estante:", txtUbicacion);
        agregarFilaForm(pnlForm, gbc, row++, "", chkActivo);

        JPanel pnlBotonesCrud = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton btnNuevo = EstiloUI.botonSecundario("Nuevo");
        JButton btnGuardar = EstiloUI.botonPrimario("Guardar");
        JButton btnDesactivar = EstiloUI.botonPeligro("Desactivar");
        JButton btnEliminar = EstiloUI.botonPeligro("Eliminar");
        btnEliminar.addActionListener(e -> Eliminador.eliminar(this, modelo.enums.RegistroEliminable.REPUESTO, txtId.getText(), () -> {
            limpiarFormulario();
            cargarRepuestos("");
        }));

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarRepuesto());
        btnDesactivar.addActionListener(e -> desactivarRepuesto());

        pnlBotonesCrud.add(btnNuevo);
        pnlBotonesCrud.add(btnGuardar);
        pnlBotonesCrud.add(btnDesactivar);
        pnlBotonesCrud.add(btnEliminar);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        pnlForm.add(pnlBotonesCrud, gbc);

        JPanel pnlTransaccionales = new JPanel(new GridLayout(1, 3, 4, 4));
        pnlTransaccionales.setBorder(BorderFactory.createTitledBorder("Transacciones Atómicas"));
        JButton btnEntrada = EstiloUI.botonPrimario("+ Entrada");
        JButton btnSalida = EstiloUI.botonSecundario("- Salida Orden");
        JButton btnAjuste = EstiloUI.botonSecundario("Ajustar Stock");

        btnEntrada.addActionListener(e -> dialogoEntradaStock());
        btnSalida.addActionListener(e -> dialogoSalidaOrden());
        btnAjuste.addActionListener(e -> dialogoAjusteStock());

        pnlTransaccionales.add(btnEntrada);
        pnlTransaccionales.add(btnSalida);
        pnlTransaccionales.add(btnAjuste);

        gbc.gridy = row;
        pnlForm.add(pnlTransaccionales, gbc);

        pnl.add(EstiloUI.panelLateral(pnlForm, 450), BorderLayout.WEST);

        JPanel pnlCentro = new JPanel(new BorderLayout(6, 6));

        JPanel pnlTop = new JPanel(new BorderLayout(5, 5));
        txtBuscar = EstiloUI.campoCompacto(new JTextField());
        JButton btnBuscar = EstiloUI.botonPrimario("Buscar");
        JButton btnTodos = EstiloUI.botonSecundario("Todos");
        JButton btnBajoStock = EstiloUI.botonPeligro("Ver Stock Bajo");

        btnBuscar.addActionListener(e -> cargarRepuestos(txtBuscar.getText()));
        btnTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cargarRepuestos("");
        });
        btnBajoStock.addActionListener(e -> cargarSoloStockBajo());
        txtBuscar.addActionListener(e -> cargarRepuestos(txtBuscar.getText()));

        JPanel pnlBotonesTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlBotonesTop.add(btnBuscar);
        pnlBotonesTop.add(btnTodos);
        pnlBotonesTop.add(btnBajoStock);

        pnlTop.add(new JLabel("Buscar (código / nombre / marca):"), BorderLayout.NORTH);
        pnlTop.add(txtBuscar, BorderLayout.CENTER);
        pnlTop.add(pnlBotonesTop, BorderLayout.EAST);

        pnlCentro.add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"ID", "Código", "Nombre", "Categoría", "Marca", "Stock", "Mínimo", "Estado Stock", "Costo", "Precio Venta", "Proveedor"};
        modeloRepuestos = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaRepuestos = new JTable(modeloRepuestos);
        EstiloUI.tabla(tablaRepuestos);

        tablaRepuestos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaRepuestos.getSelectedRow() >= 0) {
                seleccionarRepuesto(tablaRepuestos.getSelectedRow());
            }
        });

        pnlCentro.add(new JScrollPane(tablaRepuestos), BorderLayout.CENTER);
        pnl.add(pnlCentro, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel crearPanelKardex() {
        JPanel pnl = new JPanel(new BorderLayout(8, 8));
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnRefrescarKardex = EstiloUI.botonPrimario("Refrescar Últimos Movimientos");
        btnRefrescarKardex.addActionListener(e -> cargarKardexGeneral());
        pnlTop.add(btnRefrescarKardex);
        pnl.add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"ID Mov", "Fecha", "Repuesto", "Tipo Movimiento", "Cantidad", "Stock Ant.", "Stock Res.", "Costo Unit.", "Orden #", "Motivo / Referencia", "Usuario"};
        modeloKardex = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaKardex = new JTable(modeloKardex);
        EstiloUI.tabla(tablaKardex);

        pnl.add(new JScrollPane(tablaKardex), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel crearPanelHilos() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel pnlControles = new JPanel(new GridLayout(1, 2, 15, 10));

        JPanel cardMonitor = new JPanel(new BorderLayout(8, 8));
        cardMonitor.setBorder(BorderFactory.createTitledBorder("Hilo 1: Monitor de stock mínimo"));
        JLabel lblDescMonitor = new JLabel("<html>Verifica periódicamente en segundo plano qué repuestos tienen <b>stock_actual &lt;= stock_minimo</b> y notifica alertas en tiempo real.</html>");
        cardMonitor.add(lblDescMonitor, BorderLayout.CENTER);

        JPanel pnlBtnMonitor = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnToggleMonitor = EstiloUI.botonPrimario("Iniciar MonitorStockMinimo");
        JButton btnRevisarAhora = EstiloUI.botonSecundario("Ejecutar Revisión Inmediata");

        btnToggleMonitor.addActionListener(e -> toggleMonitor());
        btnRevisarAhora.addActionListener(e -> {
            logConsola("[MonitorStockMinimo] Ejecutando barrido manual de stock...");
            monitorStock.revisarStock();
        });

        pnlBtnMonitor.add(btnToggleMonitor);
        pnlBtnMonitor.add(btnRevisarAhora);
        cardMonitor.add(pnlBtnMonitor, BorderLayout.SOUTH);

        JPanel cardDemo = new JPanel(new BorderLayout(8, 8));
        cardDemo.setBorder(BorderFactory.createTitledBorder("Hilo 2: Demostración de concurrencia"));
        JLabel lblDescDemo = new JLabel("<html>Lanza <b>2 hilos al mismo milisegundo</b> intentando descontar 4 y 3 unidades de un mismo repuesto con stock = 5. Demuestra el bloqueo transaccional <code>UPDLOCK, HOLDLOCK</code> y rollback automático.</html>");
        cardDemo.add(lblDescDemo, BorderLayout.CENTER);

        JPanel pnlBtnDemo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnEjecutarDemo = EstiloUI.botonPeligro("Ejecutar demostración de concurrencia");
        JButton btnLimpiarLog = EstiloUI.botonSecundario("Limpiar Consola");

        btnEjecutarDemo.addActionListener(e -> {
            Integer idRep = null;
            if (!txtId.getText().isBlank()) {
                idRep = Integer.parseInt(txtId.getText().trim());
            }
            demoConcurrencia.ejecutarDemostracion(idRep);
        });
        btnLimpiarLog.addActionListener(e -> txtConsolaHilos.setText(""));

        pnlBtnDemo.add(btnEjecutarDemo);
        pnlBtnDemo.add(btnLimpiarLog);
        cardDemo.add(pnlBtnDemo, BorderLayout.SOUTH);

        pnlControles.add(cardMonitor);
        pnlControles.add(cardDemo);

        pnl.add(pnlControles, BorderLayout.NORTH);

        txtConsolaHilos = new JTextArea();
        txtConsolaHilos.setEditable(false);
        txtConsolaHilos.setBackground(new Color(0x111827));
        txtConsolaHilos.setForeground(new Color(0x10B981));
        txtConsolaHilos.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtConsolaHilos.setText(">>> Consola de Eventos de Hilos y Concurrencia lista.\n");

        JScrollPane scrollConsola = new JScrollPane(txtConsolaHilos);
        scrollConsola.setBorder(BorderFactory.createTitledBorder("Bitácora de Ejecución Concurrente en Tiempo Real"));
        pnl.add(scrollConsola, BorderLayout.CENTER);

        return pnl;
    }

    private void configurarListenersHilos() {
        demoConcurrencia.setLogger(msg -> SwingUtilities.invokeLater(() -> {
            logConsola(msg);
            cargarRepuestos(txtBuscar.getText());
            cargarKardexGeneral();
        }));

        monitorStock.agregarListener(new MonitorStockMinimo.AlertaStockListener() {
            @Override
            public void onAlertaStockCritico(List<Repuesto> repuestosCriticos) {
                SwingUtilities.invokeLater(() -> {
                    StringBuilder sb = new StringBuilder();
                    int n = repuestosCriticos.size();
                    sb.append("[ALERTA MONITOR STOCK] ").append(n == 1 ? "1 repuesto en nivel crítico:\n"
                            : n + " repuestos en nivel crítico:\n");
                    for (Repuesto r : repuestosCriticos) {
                        sb.append(String.format("   - [%s] %s | Stock actual: %d (Mínimo requerido: %d)\n",
                                r.getCodigo(), r.getNombre(), r.getStock(), r.getStockMinimo()));
                    }
                    logConsola(sb.toString());
                });
            }

            @Override
            public void onRepuestosNormalizados(List<Repuesto> repuestos) {
                SwingUtilities.invokeLater(() -> {
                    StringBuilder sb = new StringBuilder("[MONITOR STOCK] Volvieron a un nivel normal:\n");
                    for (Repuesto r : repuestos) {
                        sb.append(String.format("   - [%s] %s\n", r.getCodigo(), r.getNombre()));
                    }
                    logConsola(sb.toString());
                });
            }

            @Override
            public void onRevisionCompletada(int repuestosTotalesBajoMinimo) {
                SwingUtilities.invokeLater(() -> {
                    lblEstadoMonitor.setText("Monitor ACTIVO | Bajo mínimo: " + repuestosTotalesBajoMinimo);
                });
            }
        });
    }

    private void toggleMonitor() {
        if (monitorStock.estaEjecutando()) {
            monitorStock.detener();
            btnToggleMonitor.setText("Iniciar MonitorStockMinimo");
            lblEstadoMonitor.setText("Monitor Stock Mínimo: DETENIDO");
            logConsola("[MonitorStockMinimo] Hilo detenido por el usuario.");
        } else {
            monitorStock.iniciar();
            btnToggleMonitor.setText("Detener MonitorStockMinimo");
            lblEstadoMonitor.setText("Monitor Stock Mínimo: EN EJECUCIÓN...");
            logConsola("[MonitorStockMinimo] Hilo iniciado. Revisa el inventario cada 5 segundos y avisa solo cuando algo cambia.");
        }
    }

    private void logConsola(String texto) {
        txtConsolaHilos.append(texto + "\n");
        txtConsolaHilos.setCaretPosition(txtConsolaHilos.getDocument().getLength());
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

    private void cargarComboProveedores() {
        try {
            cmbProveedor.removeAllItems();
            List<Proveedor> lista = inventarioService.listarProveedoresActivos();
            for (Proveedor p : lista) {
                cmbProveedor.addItem(p);
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar proveedores: " + ex.getMessage());
        }
    }

    private void cargarRepuestos(String filtro) {
        try {
            modeloRepuestos.setRowCount(0);
            List<Repuesto> lista = (filtro == null || filtro.isBlank())
                    ? inventarioService.listarTodosRepuestos()
                    : inventarioService.buscarRepuestos(filtro);

            for (Repuesto r : lista) {
                String estadoStock = r.estaBajoMinimo() ? "CRÍTICO / BAJO" : "NORMAL";
                String prov = (r.getProveedor() != null && r.getProveedor().getNombre() != null)
                        ? r.getProveedor().getNombre()
                        : "Sin proveedor";

                modeloRepuestos.addRow(new Object[]{
                        r.getId(),
                        r.getCodigo(),
                        r.getNombre(),
                        r.getCategoria(),
                        r.getMarca() != null ? r.getMarca() : "",
                        r.getStock(),
                        r.getStockMinimo(),
                        estadoStock,
                        "Lps. " + r.getCostoUnitario(),
                        "Lps. " + r.getPrecioVenta(),
                        prov
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar inventario: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSoloStockBajo() {
        try {
            modeloRepuestos.setRowCount(0);
            List<Repuesto> lista = inventarioService.listarBajoStockMinimo();
            for (Repuesto r : lista) {
                String prov = (r.getProveedor() != null && r.getProveedor().getNombre() != null)
                        ? r.getProveedor().getNombre()
                        : "Sin proveedor";

                modeloRepuestos.addRow(new Object[]{
                        r.getId(),
                        r.getCodigo(),
                        r.getNombre(),
                        r.getCategoria(),
                        r.getMarca() != null ? r.getMarca() : "",
                        r.getStock(),
                        r.getStockMinimo(),
                        "CRÍTICO / BAJO",
                        "Lps. " + r.getCostoUnitario(),
                        "Lps. " + r.getPrecioVenta(),
                        prov
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarKardexGeneral() {
        try {
            modeloKardex.setRowCount(0);
            List<MovimientoInventario> movs = inventarioService.listarMovimientosRecientes(100);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (MovimientoInventario m : movs) {
                String rep = (m.getRepuesto() != null)
                        ? "[" + m.getRepuesto().getCodigo() + "] " + m.getRepuesto().getNombre()
                        : "N/A";
                modeloKardex.addRow(new Object[]{
                        m.getId(),
                        m.getFecha() != null ? m.getFecha().format(fmt) : "",
                        rep,
                        m.getTipo(),
                        m.getCantidad(),
                        m.getStockAnterior(),
                        m.getStockResultante(),
                        "Lps. " + m.getCostoUnitario(),
                        m.getIdOrden() != null ? m.getIdOrden() : "-",
                        m.getReferencia() != null ? m.getReferencia() : "",
                        m.getUsuario() != null ? m.getUsuario() : ""
                });
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar Kardex: " + ex.getMessage());
        }
    }

    private void seleccionarRepuesto(int fila) {
        int id = (int) modeloRepuestos.getValueAt(fila, 0);
        try {
            inventarioService.buscarPorId(id).ifPresent(r -> {
                txtId.setText(String.valueOf(r.getId()));
                txtCodigo.setText(r.getCodigo());
                txtNombre.setText(r.getNombre());
                if (r.getCategoria() != null) {
                    cmbCategoria.setSelectedItem(r.getCategoria());
                }
                txtMarca.setText(r.getMarca() != null ? r.getMarca() : "");
                spnStock.setValue(r.getStock());
                spnStockMinimo.setValue(r.getStockMinimo());
                spnCosto.setValue(r.getCostoUnitario().doubleValue());
                spnPrecio.setValue(r.getPrecioVenta().doubleValue());
                txtUbicacion.setText(r.getUbicacion() != null ? r.getUbicacion() : "");
                chkActivo.setSelected(r.isActivo());

                if (r.getProveedor() != null) {
                    for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
                        if (cmbProveedor.getItemAt(i).getId() == r.getProveedor().getId()) {
                            cmbProveedor.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al seleccionar repuesto: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtCodigo.setText("");
        txtNombre.setText("");
        cmbCategoria.setSelectedIndex(0);
        txtMarca.setText("");
        spnStock.setValue(10);
        spnStockMinimo.setValue(3);
        spnCosto.setValue(150.0);
        spnPrecio.setValue(250.0);
        txtUbicacion.setText("");
        chkActivo.setSelected(true);
        tablaRepuestos.clearSelection();
    }

    private void guardarRepuesto() {
        try {
            Repuesto r = new Repuesto();
            r.setCodigo(txtCodigo.getText().trim());
            r.setNombre(txtNombre.getText().trim());
            r.setCategoria((CategoriaRepuesto) cmbCategoria.getSelectedItem());
            r.setMarca(txtMarca.getText().trim());
            r.setProveedor((Proveedor) cmbProveedor.getSelectedItem());
            r.setStock((int) spnStock.getValue());
            r.setStockMinimo((int) spnStockMinimo.getValue());
            r.setCostoUnitario(BigDecimal.valueOf(((Number) spnCosto.getValue()).doubleValue()));
            r.setPrecioVenta(BigDecimal.valueOf(((Number) spnPrecio.getValue()).doubleValue()));
            r.setUbicacion(txtUbicacion.getText().trim());
            r.setActivo(chkActivo.isSelected());

            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                inventarioService.registrarRepuesto(r);
                JOptionPane.showMessageDialog(this, "Repuesto registrado exitosamente.");
            } else {
                r.setId(Integer.parseInt(idStr));
                inventarioService.actualizarRepuesto(r);
                JOptionPane.showMessageDialog(this, "Repuesto actualizado exitosamente.");
            }
            limpiarFormulario();
            cargarRepuestos("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación / Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void desactivarRepuesto() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un repuesto para desactivar.");
            return;
        }
        int id = Integer.parseInt(idStr);
        int resp = JOptionPane.showConfirmDialog(this, "¿Desactivar este repuesto del inventario?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                inventarioService.desactivarRepuesto(id);
                limpiarFormulario();
                cargarRepuestos("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void dialogoEntradaStock() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un repuesto de la tabla primero.");
            return;
        }
        int repuestoId = Integer.parseInt(idStr);

        JSpinner spnCant = new JSpinner(new SpinnerNumberModel(5, 1, 10000, 1));
        JSpinner spnNuevoCosto = new JSpinner(new SpinnerNumberModel(((Number) spnCosto.getValue()).doubleValue(), 0.0, 500000.0, 10.0));
        JTextField txtMotivo = new JTextField("Compra a proveedor / Reabastecimiento");

        JPanel pnl = new JPanel(new GridLayout(3, 2, 5, 5));
        pnl.add(new JLabel("Cantidad a ingresar:"));
        pnl.add(spnCant);
        pnl.add(new JLabel("Costo unitario de compra:"));
        pnl.add(spnNuevoCosto);
        pnl.add(new JLabel("Motivo / Factura Compra:"));
        pnl.add(txtMotivo);

        if (JOptionPane.showConfirmDialog(this, pnl, "Entrada de inventario", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int cant = (int) spnCant.getValue();
                BigDecimal costo = BigDecimal.valueOf(((Number) spnNuevoCosto.getValue()).doubleValue());
                MovimientoInventario mov = inventarioService.registrarEntradaStock(repuestoId, cant, costo, txtMotivo.getText().trim(), "ADMIN");
                JOptionPane.showMessageDialog(this, "Entrada registrada. Nuevo stock: " + mov.getStockResultante());
                cargarRepuestos("");
                cargarKardexGeneral();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Transaccional", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void dialogoSalidaOrden() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un repuesto de la tabla primero.");
            return;
        }
        int repuestoId = Integer.parseInt(idStr);

        JSpinner spnOrdenId = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        JSpinner spnCant = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

        JPanel pnl = new JPanel(new GridLayout(2, 2, 5, 5));
        pnl.add(new JLabel("ID de Orden de Trabajo:"));
        pnl.add(spnOrdenId);
        pnl.add(new JLabel("Cantidad a descontar:"));
        pnl.add(spnCant);

        if (JOptionPane.showConfirmDialog(this, pnl, "Descuento por orden", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int ordenId = (int) spnOrdenId.getValue();
                int cant = (int) spnCant.getValue();
                MovimientoInventario mov = inventarioService.descontarStockPorOrden(ordenId, repuestoId, cant, "TECNICO");
                JOptionPane.showMessageDialog(this, "Descuento exitoso. Stock anterior: " + mov.getStockAnterior()
                        + " -> Stock resultante: " + mov.getStockResultante());
                cargarRepuestos("");
                cargarKardexGeneral();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se realizó el descuento:\n" + ex.getMessage(),
                        "Control de stock", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void dialogoAjusteStock() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un repuesto de la tabla primero.");
            return;
        }
        int repuestoId = Integer.parseInt(idStr);

        JSpinner spnNuevoStock = new JSpinner(new SpinnerNumberModel((int) spnStock.getValue(), 0, 99999, 1));
        JTextField txtMotivo = new JTextField("Conteo físico / Auditoría de almacén");

        JPanel pnl = new JPanel(new GridLayout(2, 2, 5, 5));
        pnl.add(new JLabel("Nuevo stock físico real:"));
        pnl.add(spnNuevoStock);
        pnl.add(new JLabel("Motivo del ajuste:"));
        pnl.add(txtMotivo);

        if (JOptionPane.showConfirmDialog(this, pnl, "Ajuste de Inventario", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int nuevoStock = (int) spnNuevoStock.getValue();
                MovimientoInventario mov = inventarioService.registrarAjusteInventario(repuestoId, nuevoStock, txtMotivo.getText().trim(), "AUDITOR");
                JOptionPane.showMessageDialog(this, "Ajuste aplicado. Stock resultante: " + mov.getStockResultante());
                cargarRepuestos("");
                cargarKardexGeneral();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmInventario().setVisible(true));
    }
}
