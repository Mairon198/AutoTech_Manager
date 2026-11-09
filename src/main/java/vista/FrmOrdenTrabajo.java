package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.Mecanico;
import modelo.OrdenResumen;
import modelo.Repuesto;
import modelo.Servicio;
import modelo.Vehiculo;
import modelo.enums.EstadoOrden;
import servicio.CatalogoService;
import servicio.ClienteService;
import servicio.InventarioService;
import servicio.MecanicoService;
import servicio.VehiculoService;

/**
 * Módulo 3: órdenes de trabajo. Ingreso del vehículo, servicios solicitados, repuestos
 * consumidos, flujo de estados y cierre de la orden procesado con un hilo (SwingWorker).
 */
public class FrmOrdenTrabajo extends FrmBaseOrden {

    private final ClienteService clientes = new ClienteService();
    private final VehiculoService vehiculos = new VehiculoService();
    private final MecanicoService mecanicos = new MecanicoService();
    private final CatalogoService catalogo = new CatalogoService();
    private final InventarioService inventario = new InventarioService();
    private final servicio.EliminacionService eliminacion = new servicio.EliminacionService();

    // Ingreso
    private final JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private final JComboBox<Vehiculo> cmbVehiculo = new JComboBox<>();
    private final JSpinner spnKm = new JSpinner(new SpinnerNumberModel(0, 0, 2_000_000, 100));
    private final JTextArea txtSintomas = area(3);
    private final JTextArea txtDiagPrelim = area(3);
    private final JComboBox<Mecanico> cmbResponsable = new JComboBox<>();
    private final JTable tblOrdenes = tabla();

    // Servicios
    private final JComboBox<Servicio> cmbServicio = new JComboBox<>();
    private final JSpinner spnCantServicio = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JLabel lblServicio = nota(" ");
    private final JTable tblServicios = tabla();

    // Repuestos
    private final JComboBox<Repuesto> cmbRepuesto = new JComboBox<>();
    private final JSpinner spnCantRepuesto = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
    private final JTable tblRepuestos = tabla();

    // Estado y cierre
    private final JLabel lblEstado = new JLabel(" ");
    private final JComboBox<EstadoOrden> cmbSiguiente = new JComboBox<>();

    public FrmOrdenTrabajo() {
        super("Órdenes de trabajo", "MÓDULO 3: ÓRDENES DE TRABAJO",
                "Ingreso del vehículo, servicios solicitados, repuestos utilizados y flujo de estados", true);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstiloUI.fuente(Font.BOLD, 13f));
        tabs.addTab("1. Ingreso del vehículo", panelIngreso());
        tabs.addTab("2. Servicios", panelServicios());
        tabs.addTab("3. Repuestos", panelRepuestos());
        tabs.addTab("4. Estado", panelEstado());
        setContenido(tabs);
        cargarCatalogos();
        cargarOrdenes(null);
    }

    // ================================================================== paneles

    private JPanel panelIngreso() {
        mostrarCon(cmbVehiculo, v -> v.getPlaca() + " · " + v.getDescripcion() + " · " + v.getKilometraje() + " km");
        mostrarCon(cmbResponsable, m -> m + " (" + m.getNivelCertificacion() + ")");
        cmbCliente.addActionListener(e -> cargarVehiculos());
        cmbVehiculo.addActionListener(e -> {
            Vehiculo v = (Vehiculo) cmbVehiculo.getSelectedItem();
            if (v != null) {
                spnKm.setValue(v.getKilometraje());
            }
        });
        JButton registrar = EstiloUI.botonPrimario("Registrar ingreso");
        registrar.addActionListener(e -> registrarIngreso());
        JButton nuevo = EstiloUI.botonSecundario("Limpiar");
        nuevo.addActionListener(e -> {
            txtSintomas.setText("");
            txtDiagPrelim.setText("");
        });

        Formulario f = new Formulario("Nueva orden de trabajo")
                .campo("Cliente", cmbCliente)
                .campo("Vehículo", cmbVehiculo)
                .campo("Kilometraje", spnKm)
                .campo("Síntomas reportados", txtSintomas)
                .campo("Diagnóstico preliminar", txtDiagPrelim)
                .campo("Mecánico responsable", cmbResponsable)
                .ancho(botones(registrar, nuevo))
                .ancho(nota("Se genera un número único (OT-000000) y la orden queda <b>Recibida</b>."))
                .relleno();
        f.setPreferredSize(new Dimension(430, 0));

        tblOrdenes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Integer id = idSeleccionado(tblOrdenes);
                if (id != null) {
                    seleccionarOrden(id);
                }
            }
        });
        JButton eliminar = EstiloUI.botonPeligro("Eliminar orden seleccionada");
        eliminar.addActionListener(e -> eliminarOrden());
        JPanel lista = new JPanel(new BorderLayout(0, 8));
        lista.setOpaque(false);
        lista.add(conTitulo(tblOrdenes, "Órdenes (clic para seleccionarla arriba)"), BorderLayout.CENTER);
        lista.add(botones(eliminar), BorderLayout.SOUTH);
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.add(f, BorderLayout.WEST);
        p.add(lista, BorderLayout.CENTER);
        return p;
    }

    private JPanel panelServicios() {
        mostrarCon(cmbServicio, s -> s.getCodigo() + " · " + s.getNombre() + " · " + s.getNivel());
        cmbServicio.addActionListener(e -> describirServicio());
        JButton agregar = EstiloUI.botonPrimario("Agregar a la orden");
        agregar.addActionListener(e -> ejecutar(() -> {
            BigDecimal precio = ordenes.agregarServicio(orden(), (Servicio) cmbServicio.getSelectedItem(),
                    (Integer) spnCantServicio.getValue());
            recargar();
            lblServicio.setText("<html>Agregado con precio L " + dinero(precio) + "</html>");
        }));
        JButton quitar = EstiloUI.botonPeligro("Quitar seleccionado");
        quitar.addActionListener(e -> {
            Integer id = idSeleccionado(tblServicios);
            if (id == null) {
                avisar("Seleccione un servicio de la tabla.");
            } else if (ejecutar(() -> ordenes.quitarServicio(orden(), id))) {
                recargar();
            }
        });
        Formulario f = new Formulario("Agregar servicio solicitado")
                .campo("Servicio", cmbServicio)
                .campo("Cantidad", spnCantServicio)
                .ancho(lblServicio)
                .ancho(botones(agregar, quitar))
                .ancho(nota("El precio lo calcula cada tipo de servicio según su nivel. "
                        + "Los servicios se agregan hasta que la orden entra en reparación."))
                .relleno();
        f.setPreferredSize(new Dimension(430, 0));
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.add(f, BorderLayout.WEST);
        p.add(conTitulo(tblServicios, "Servicios de la orden"), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelRepuestos() {
        mostrarCon(cmbRepuesto, r -> r.getCodigo() + " · " + r.getNombre() + "  (stock " + r.getStock() + ", L "
                + dinero(r.getPrecioVenta()) + ")");
        JButton consumir = EstiloUI.botonPrimario("Descontar del inventario");
        consumir.addActionListener(e -> {
            Repuesto r = (Repuesto) cmbRepuesto.getSelectedItem();
            int cant = (Integer) spnCantRepuesto.getValue();
            if (ejecutar(() -> ordenes.consumirRepuesto(orden(), r, cant, usuario()))) {
                cargarRepuestosCatalogo();
                recargar();
            }
        });
        Formulario f = new Formulario("Repuestos utilizados")
                .campo("Repuesto", cmbRepuesto)
                .campo("Cantidad", spnCantRepuesto)
                .ancho(botones(consumir))
                .ancho(nota("Se descuenta del inventario al instante: si no hay stock suficiente no se registra nada, "
                        + "y cada descuento queda en el kárdex. "
                        + "Permitido con la orden en diagnóstico o en reparación."))
                .relleno();
        f.setPreferredSize(new Dimension(430, 0));
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.add(f, BorderLayout.WEST);
        p.add(conTitulo(tblRepuestos, "Repuestos de la orden (precio histórico)"), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelEstado() {
        lblEstado.setFont(EstiloUI.fuente(Font.BOLD, 16f));
        lblEstado.setForeground(EstiloUI.AZUL_OSCURO);
        JButton cambiar = EstiloUI.botonPrimario("Cambiar estado");
        cambiar.addActionListener(e -> cambiarEstado());
        JButton eliminar = EstiloUI.botonPeligro("Eliminar orden");
        eliminar.addActionListener(e -> eliminarOrden());

        Formulario flujo = new Formulario("Flujo de estados")
                .campo("Estado actual", lblEstado)
                .campo("Pasar a", cmbSiguiente)
                .ancho(botones(cambiar, eliminar))
                .ancho(nota("Recibida → En diagnóstico → Pendiente de aprobación → En reparación → "
                        + "En control de calidad → Finalizada → Entregada. Solo se ofrecen los cambios válidos; "
                        + "la base de datos vuelve a verificar las reglas (presupuesto aprobado, actividades "
                        + "completas, control de calidad, etc.)."))
                .ancho(nota("El cierre de la orden (garantía, finalizar y factura) se hace en el módulo "
                        + "<b>Control de calidad</b>, después de aprobar la inspección."))
                .relleno();
        flujo.setPreferredSize(new Dimension(520, 0));
        JPanel p = new JPanel(new BorderLayout());
        p.add(flujo, BorderLayout.WEST);
        return p;
    }

    // ================================================================== datos

    private void cargarCatalogos() {
        ejecutar(() -> {
            llenarCombo(cmbCliente, clientes.listarActivos());
            llenarCombo(cmbResponsable, mecanicos.listarDisponibles());
            llenarCombo(cmbServicio, catalogo.listarActivos());
            cargarRepuestosCatalogo();
        });
        cargarVehiculos();
        describirServicio();
    }

    private void cargarRepuestosCatalogo() {
        ejecutar(() -> {
            Object sel = cmbRepuesto.getSelectedItem();
            List<Repuesto> lista = inventario.listarRepuestosActivos();
            llenarCombo(cmbRepuesto, lista);
            if (sel instanceof Repuesto r) {
                lista.stream().filter(x -> x.getId() == r.getId()).findFirst().ifPresent(cmbRepuesto::setSelectedItem);
            }
        });
    }

    private void cargarVehiculos() {
        Cliente c = (Cliente) cmbCliente.getSelectedItem();
        ejecutar(() -> llenarCombo(cmbVehiculo, c == null ? List.of() : vehiculos.listarPorCliente(c.getId())));
        Vehiculo v = (Vehiculo) cmbVehiculo.getSelectedItem();
        spnKm.setValue(v == null ? 0 : v.getKilometraje());
    }

    private void describirServicio() {
        Servicio s = (Servicio) cmbServicio.getSelectedItem();
        lblServicio.setText(s == null ? " " : String.format(
                "<html><body style='width:330px'>Precio: <b>L %s</b> · Tiempo estimado: %.1f h · Garantía: %d días%s%s</html>",
                dinero(s.calcularCosto()), s.getTiempoEstimadoHoras(), s.getDiasGarantia(),
                s.requiereDiagnostico() ? " · Requiere diagnóstico" : "",
                s.requiereControlCalidad() ? " · Control de calidad obligatorio" : ""));
    }

    private void registrarIngreso() {
        ejecutar(() -> {
            int id = ordenes.crearOrden((Cliente) cmbCliente.getSelectedItem(), (Vehiculo) cmbVehiculo.getSelectedItem(),
                    (Integer) spnKm.getValue(), txtSintomas.getText(), txtDiagPrelim.getText(),
                    (Mecanico) cmbResponsable.getSelectedItem());
            txtSintomas.setText("");
            txtDiagPrelim.setText("");
            cargarOrdenes(id);
            cargarVehiculos();
            OrdenResumen o = orden();
            avisar("Orden " + (o == null ? id : o.getNumero()) + " registrada.\nAhora agregue los servicios solicitados.");
        });
    }

    private void seleccionarOrden(int id) {
        for (int i = 0; i < cmbOrden.getItemCount(); i++) {
            if (cmbOrden.getItemAt(i).getOrdenId() == id) {
                cmbOrden.setSelectedIndex(i);
                return;
            }
        }
        cargarOrdenes(id);
    }

    private void cambiarEstado() {
        EstadoOrden nuevo = (EstadoOrden) cmbSiguiente.getSelectedItem();
        if (nuevo == null) {
            avisar("La orden no tiene cambios de estado disponibles.");
            return;
        }
        if ((nuevo == EstadoOrden.CANCELADA || nuevo == EstadoOrden.RECHAZADA)
                && !confirmar("¿Seguro que desea pasar la orden a " + nuevo + "? No se podrá reabrir.")) {
            return;
        }
        if (nuevo == EstadoOrden.FINALIZADA) {
            avisar("Para finalizar use el módulo Control de calidad → \"Cerrar orden y facturar\":\n"
                    + "genera la garantía, finaliza la orden y emite la factura en la misma operación.");
            return;
        }
        if (ejecutar(() -> ordenes.cambiarEstado(orden(), nuevo))) {
            recargar();
        }
    }

    /** Elimina la orden si no tiene historial (inventario, pagos, factura, motor o garantía). */
    private void eliminarOrden() {
        OrdenResumen o = orden();
        if (o == null) {
            avisar("Seleccione una orden.");
            return;
        }
        String motivo = (String) javax.swing.JOptionPane.showInputDialog(this,
                "¿Eliminar la orden " + o.getNumero() + " (" + o.getPlaca() + ", " + o.getCliente() + ")?\n"
                + "Dejará de aparecer en listas y reportes. Escriba el motivo:",
                "Eliminar orden", javax.swing.JOptionPane.WARNING_MESSAGE, null, null, "Registrada por error");
        if (motivo == null) {
            return;
        }
        if (ejecutar(() -> eliminacion.eliminarOrden(o, motivo))) {
            avisar("La orden " + o.getNumero() + " fue eliminada.");
            cargarOrdenes(null);
        }
    }

    @Override
    protected void ordenSeleccionada(OrdenResumen o) {
        llenarTablaOrdenes();
        if (o == null) {
            limpiar(tblServicios);
            limpiar(tblRepuestos);
            lblEstado.setText("—");
            cmbSiguiente.removeAllItems();
            return;
        }
        ejecutar(() -> {
            llenar(tblServicios, ordenes.servicios(o.getOrdenId()));
            llenar(tblRepuestos, ordenes.repuestos(o.getOrdenId()));
        });
        lblEstado.setText(o.getEstado().toString());
        lblEstado.setForeground(o.estaCerrada() ? new Color(0x047857) : EstiloUI.AZUL_OSCURO);
        cmbSiguiente.removeAllItems();
        o.getEstado().siguientesPermitidos().forEach(cmbSiguiente::addItem);
    }

    private void llenarTablaOrdenes() {
        ejecutar(() -> {
            DefaultTableModel m = new DefaultTableModel(
                    new Object[]{"Id", "Orden", "Ingreso", "Cliente", "Placa", "Vehículo", "Estado", "Total", "Saldo"}, 0);
            for (OrdenResumen x : ordenes.listar(false)) {
                m.addRow(new Object[]{x.getOrdenId(), x.getNumero(),
                        x.getFechaIngreso() == null ? "" : x.getFechaIngreso().format(FECHA), x.getCliente(),
                        x.getPlaca(), x.getVehiculo(), x.getEstado(), dinero(x.getTotal()), dinero(x.getSaldo())});
            }
            tblOrdenes.setModel(m);
            tblOrdenes.getColumnModel().getColumn(0).setMinWidth(0);
            tblOrdenes.getColumnModel().getColumn(0).setMaxWidth(0);
        });
    }

}
