package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import modelo.ConfiguracionImpuesto;
import modelo.DetallePresupuesto;
import modelo.Mecanico;
import modelo.OrdenResumen;
import modelo.Presupuesto;
import modelo.enums.EstadoOrden;
import servicio.MecanicoService;
import servicio.PresupuestoService;

/**
 * Módulo 6: diagnóstico técnico con evidencias y presupuesto desglosado
 * (mano de obra, repuestos, horas, impuestos y descuento autorizado) con la
 * aprobación o el rechazo del cliente.
 */
public class FrmDiagnosticoPresupuesto extends FrmBaseOrden {

    private final PresupuestoService presupuestos = new PresupuestoService();
    private final MecanicoService mecanicos = new MecanicoService();

    // Diagnóstico
    private final JComboBox<Mecanico> cmbMecanico = new JComboBox<>();
    private final JTextArea txtDescripcion = area(3);
    private final JTextArea txtCausa = area(2);
    private final JTextArea txtRecomendacion = area(2);
    private final JCheckBox chkMotor = EstiloUI.casilla(new JCheckBox("Requiere cambio de motor"));
    private final JComboBox<String> cmbTipoEvidencia = new JComboBox<>(new String[]{
        "Prueba de compresión", "Escáner OBD-II", "Medición", "Fotografía", "Prueba de ruta", "Inspección visual"});
    private final JTextField txtEvidencia = EstiloUI.campoCompacto(new JTextField());
    private final JTextField txtValor = EstiloUI.campoCompacto(new JTextField());
    private final JTable tblDiagnosticos = tabla();
    private final JTable tblEvidencias = tabla();

    // Presupuesto
    private final JComboBox<ConfiguracionImpuesto> cmbImpuesto = new JComboBox<>();
    private final JSpinner spnDescuento = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000.0, 50.0));
    private final JTextField txtAutoriza = EstiloUI.campoCompacto(new JTextField());
    private final JTable tblPropuesta = tabla();
    private final JLabel lblTotales = new JLabel(" ");
    private final JTextField txtRespuesta = EstiloUI.campoCompacto(new JTextField());
    private final JTable tblPresupuestos = tabla();
    private final JTable tblLineas = tabla();
    private Presupuesto propuesta;

    public FrmDiagnosticoPresupuesto() {
        super("Diagnóstico y presupuesto", "MÓDULO 6: DIAGNÓSTICO TÉCNICO Y PRESUPUESTO",
                "Evaluación con evidencias, presupuesto desglosado y aprobación del cliente", true);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstiloUI.fuente(Font.BOLD, 13f));
        tabs.addTab("1. Diagnóstico y evidencias", panelDiagnostico());
        tabs.addTab("2. Presupuesto", panelPresupuesto());
        tabs.addTab("3. Aprobación del cliente", panelAprobacion());
        setContenido(tabs);
        ejecutar(() -> {
            llenarCombo(cmbMecanico, mecanicos.listarActivos());
            llenarCombo(cmbImpuesto, presupuestos.impuestos());
        });
        mostrarCon(cmbImpuesto, i -> i.getNombre() + " (" + i.getPorcentaje().stripTrailingZeros().toPlainString() + " %)");
        cargarOrdenes(null);
    }

    // ================================================================== paneles

    private JPanel panelDiagnostico() {
        cmbTipoEvidencia.setEditable(true);
        JButton guardar = EstiloUI.botonPrimario("Registrar diagnóstico");
        guardar.addActionListener(e -> registrarDiagnostico());
        JButton evidencia = EstiloUI.botonSecundario("Agregar evidencia");
        evidencia.addActionListener(e -> {
            if (ejecutar(() -> presupuestos.agregarEvidencia(orden(), (String) cmbTipoEvidencia.getSelectedItem(),
                    txtEvidencia.getText(), txtValor.getText()))) {
                txtEvidencia.setText("");
                txtValor.setText("");
                ordenSeleccionada(orden());
            }
        });
        Formulario f = new Formulario("Diagnóstico técnico")
                .campo("Mecánico", cmbMecanico)
                .campo("Descripción", txtDescripcion)
                .campo("Causa raíz", txtCausa)
                .campo("Recomendación", txtRecomendacion)
                .ancho(chkMotor)
                .ancho(botones(guardar))
                .ancho(new JLabel(" "))
                .campo("Tipo de evidencia", cmbTipoEvidencia)
                .campo("Descripción", txtEvidencia)
                .campo("Valor medido", txtValor)
                .ancho(botones(evidencia))
                .ancho(nota("Si la orden está <b>Recibida</b>, al registrar el diagnóstico pasa a <b>En diagnóstico</b>. "
                        + "Los servicios de nivel 3 a 5 exigen diagnóstico antes del presupuesto."))
                .relleno();
        JScrollPane izq = new JScrollPane(f);
        izq.setBorder(null);
        izq.setPreferredSize(new Dimension(450, 0));
        izq.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane der = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                conTitulo(tblDiagnosticos, "Diagnósticos de la orden"), conTitulo(tblEvidencias, "Evidencias técnicas"));
        der.setResizeWeight(0.5);
        der.setBorder(null);
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.add(izq, BorderLayout.WEST);
        p.add(der, BorderLayout.CENTER);
        return p;
    }

    private JPanel panelPresupuesto() {
        JButton generar = EstiloUI.botonSecundario("Generar propuesta");
        generar.addActionListener(e -> generarPropuesta());
        JButton guardar = EstiloUI.botonPrimario("Guardar y enviar al cliente");
        guardar.addActionListener(e -> guardarPresupuesto());
        lblTotales.setFont(EstiloUI.fuente(Font.PLAIN, 13f));
        Formulario f = new Formulario("Condiciones")
                .campo("Impuesto", cmbImpuesto)
                .campo("Descuento (L)", spnDescuento)
                .campo("Autorizado por", txtAutoriza)
                .ancho(botones(generar, guardar))
                .ancho(lblTotales)
                .ancho(nota("La propuesta toma la mano de obra de los servicios de la orden (precio y horas) y los "
                        + "repuestos asociados a cada servicio. Todo descuento necesita quien lo autorice. "
                        + "Al guardar, la orden pasa a <b>Pendiente de aprobación</b>."))
                .relleno();
        f.setPreferredSize(new Dimension(450, 0));
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.add(f, BorderLayout.WEST);
        p.add(conTitulo(tblPropuesta, "Desglose propuesto"), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelAprobacion() {
        JButton aprobar = EstiloUI.botonPrimario("Cliente aprueba");
        aprobar.addActionListener(e -> responder(true));
        JButton rechazar = EstiloUI.botonPeligro("Cliente rechaza");
        rechazar.addActionListener(e -> responder(false));
        Formulario f = new Formulario("Respuesta del cliente")
                .campo("Observación", txtRespuesta)
                .ancho(botones(aprobar, rechazar))
                .ancho(nota("Con la aprobación la orden pasa a <b>En reparación</b> (requiere mecánico responsable). "
                        + "Si el cliente rechaza, se puede cerrar la orden como <b>Rechazada</b> o preparar otro presupuesto."))
                .relleno();
        f.setPreferredSize(new Dimension(450, 0));
        tblPresupuestos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer id = idSeleccionado(tblPresupuestos);
                if (id == null) {
                    limpiar(tblLineas);
                } else {
                    ejecutar(() -> llenar(tblLineas, presupuestos.lineas(id)));
                }
            }
        });
        JSplitPane der = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                conTitulo(tblPresupuestos, "Presupuestos de la orden (seleccione uno para ver su detalle)"),
                conTitulo(tblLineas, "Detalle del presupuesto"));
        der.setResizeWeight(0.45);
        der.setBorder(null);
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.add(f, BorderLayout.WEST);
        p.add(der, BorderLayout.CENTER);
        return p;
    }

    // ================================================================== acciones

    private void registrarDiagnostico() {
        OrdenResumen o = orden();
        ejecutar(() -> {
            presupuestos.registrarDiagnostico(o, (Mecanico) cmbMecanico.getSelectedItem(), txtDescripcion.getText(),
                    txtCausa.getText(), txtRecomendacion.getText(), chkMotor.isSelected());
            if (o.getEstado() == EstadoOrden.RECIBIDA) {
                ordenes.cambiarEstado(o, EstadoOrden.EN_DIAGNOSTICO);
            }
            txtDescripcion.setText("");
            txtCausa.setText("");
            txtRecomendacion.setText("");
            chkMotor.setSelected(false);
            recargar();
            avisar("Diagnóstico registrado. Agregue las evidencias técnicas.");
        });
    }

    private void generarPropuesta() {
        ejecutar(() -> {
            propuesta = presupuestos.preparar(orden(), (ConfiguracionImpuesto) cmbImpuesto.getSelectedItem(),
                    BigDecimal.valueOf((Double) spnDescuento.getValue()), txtAutoriza.getText());
            mostrarPropuesta();
        });
    }

    private void mostrarPropuesta() {
        DefaultTableModel m = new DefaultTableModel(new Object[]{"Tipo", "Descripción", "Horas", "Cantidad", "Precio", "Subtotal"}, 0);
        for (DetallePresupuesto d : propuesta.getDetalles()) {
            m.addRow(new Object[]{DetallePresupuesto.MANO_OBRA.equals(d.getTipoLinea()) ? "Mano de obra" : "Repuesto",
                    d.getDescripcion(), d.getHoras() == 0 ? "" : d.getHoras(), d.getCantidad(),
                    dinero(d.getPrecioUnitario()), dinero(d.getSubtotal())});
        }
        tblPropuesta.setModel(m);
        lblTotales.setText(String.format("<html>Mano de obra: L %s (%.1f h)<br>Repuestos: L %s<br>Descuento: L %s<br>"
                + "Impuesto: L %s<br><b>Total: L %s</b></html>",
                dinero(propuesta.getSubtotalManoObra()), propuesta.getTotalHoras(), dinero(propuesta.getSubtotalRepuestos()),
                dinero(propuesta.getDescuento()), dinero(propuesta.getMontoImpuesto()), dinero(propuesta.getTotal())));
    }

    private void guardarPresupuesto() {
        if (propuesta == null) {
            avisar("Primero genere la propuesta.");
            return;
        }
        OrdenResumen o = orden();
        ejecutar(() -> {
            // vuelve a tomar las condiciones por si se cambiaron después de generar
            propuesta = presupuestos.preparar(o, (ConfiguracionImpuesto) cmbImpuesto.getSelectedItem(),
                    BigDecimal.valueOf((Double) spnDescuento.getValue()), txtAutoriza.getText());
            presupuestos.guardar(o, propuesta);
            BigDecimal total = propuesta.getTotal();   // recargar() limpia la propuesta de la pantalla
            if (o.getEstado() == EstadoOrden.EN_DIAGNOSTICO) {
                ordenes.cambiarEstado(o, EstadoOrden.PENDIENTE_APROBACION);
            }
            recargar();
            avisar("Presupuesto guardado por L " + dinero(total) + ".\nRegistre la respuesta del cliente en la pestaña 3.");
        });
    }

    private void responder(boolean aprobado) {
        OrdenResumen o = orden();
        ejecutar(() -> {
            presupuestos.responder(o, aprobado, txtRespuesta.getText());
            txtRespuesta.setText("");
            if (aprobado) {
                if (o.getEstado() == EstadoOrden.PENDIENTE_APROBACION) {
                    ordenes.cambiarEstado(o, EstadoOrden.EN_REPARACION);
                }
                recargar();
                avisar("Presupuesto aprobado. La orden está en reparación.");
            } else {
                recargar();
                if (o.getEstado() == EstadoOrden.PENDIENTE_APROBACION
                        && confirmar("Presupuesto rechazado. ¿Cerrar la orden como Rechazada?")) {
                    ordenes.cambiarEstado(o, EstadoOrden.RECHAZADA);
                    recargar();
                }
            }
        });
    }

    @Override
    protected void ordenSeleccionada(OrdenResumen o) {
        propuesta = null;
        limpiar(tblPropuesta);
        lblTotales.setText(" ");
        limpiar(tblLineas);
        if (o == null) {
            limpiar(tblDiagnosticos);
            limpiar(tblEvidencias);
            limpiar(tblPresupuestos);
            return;
        }
        ejecutar(() -> {
            llenar(tblDiagnosticos, presupuestos.diagnosticos(o.getOrdenId()));
            llenar(tblEvidencias, presupuestos.evidencias(o.getOrdenId()));
            llenar(tblPresupuestos, presupuestos.presupuestos(o.getOrdenId()));
        });
        chkMotor.setSelected(o.isRequiereCambioMotor());
    }
}
