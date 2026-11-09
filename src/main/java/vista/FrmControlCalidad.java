package vista;

import hilos.ProcesadorOrdenWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import modelo.Mecanico;
import modelo.OrdenResumen;
import modelo.enums.EstadoOrden;
import servicio.MecanicoService;

/**
 * Control de calidad antes de la entrega. Obligatorio para servicios de nivel 3 a 5:
 * sin un control aprobado la base de datos no permite finalizar la orden.
 */
public class FrmControlCalidad extends FrmBaseOrden {

    private static final String[] PUNTOS = {
        "Sin fugas de aceite, refrigerante ni combustible",
        "Sin ruidos ni vibraciones anormales",
        "Sin códigos de falla ni testigos encendidos en el tablero",
        "Niveles de fluidos correctos",
        "Torque de tornillería y conexiones verificados",
        "Prueba de ruta satisfactoria",
        "Vehículo limpio y sin daños nuevos"
    };

    private final MecanicoService mecanicos = new MecanicoService();
    private final JComboBox<Mecanico> cmbInspector = new JComboBox<>();
    private final List<JCheckBox> checks = new ArrayList<>();
    private final JRadioButton rbAprobado = new JRadioButton("Aprobado");
    private final JRadioButton rbRechazado = new JRadioButton("No aprobado (vuelve a reparación)");
    private final JTextArea txtObs = area(3);
    private final JTable tblControles = tabla();

    // Cierre de la orden (hilo en segundo plano)
    private final JButton btnCerrar = EstiloUI.botonPrimario("Cerrar orden y facturar");
    private final JButton btnIrPagos = EstiloUI.botonSecundario("Ir a Pagos");
    private final JProgressBar barra = new JProgressBar(0, 100);
    private final JLabel lblProceso = nota("Disponible cuando la orden esté en control de calidad.");
    private final JTextArea txtResultado = area(4);
    private boolean procesando;

    public FrmControlCalidad() {
        super("Control de calidad", "MÓDULO 3: CONTROL DE CALIDAD",
                "Verificación final (obligatoria en niveles 3 a 5) y cierre de la orden con garantía y factura", true);

        JPanel lista = new JPanel(new GridLayout(0, 1, 0, 2));
        lista.setOpaque(false);
        for (String p : PUNTOS) {
            JCheckBox c = EstiloUI.casilla(new JCheckBox(p));
            checks.add(c);
            lista.add(c);
        }
        JButton todos = EstiloUI.botonSecundario("Marcar todo");
        todos.addActionListener(e -> checks.forEach(c -> c.setSelected(true)));
        ButtonGroup g = new ButtonGroup();
        g.add(rbAprobado);
        g.add(rbRechazado);
        rbAprobado.setOpaque(false);
        rbRechazado.setOpaque(false);
        rbAprobado.setSelected(true);

        JButton enviar = EstiloUI.botonSecundario("Enviar orden a control de calidad");
        enviar.addActionListener(e -> {
            if (ejecutar(() -> ordenes.cambiarEstado(orden(), EstadoOrden.EN_CONTROL_CALIDAD))) {
                recargar();
            }
        });
        JButton registrar = EstiloUI.botonPrimario("Registrar control");
        registrar.addActionListener(e -> registrar());

        Formulario f = new Formulario("Inspección")
                .ancho(botones(enviar))
                .ancho(nota("Use el botón de arriba cuando la reparación termine (orden <b>En reparación</b>)."))
                .campo("Inspector", cmbInspector)
                .ancho(lista)
                .ancho(botones(todos))
                .ancho(botones(rbAprobado, rbRechazado))
                .campo("Observaciones", txtObs)
                .ancho(botones(registrar))
                .ancho(nota("Al aprobar, cierre la orden con <b>Cerrar orden y facturar</b> (a la derecha)."))
                .relleno();
        JScrollPane izq = new JScrollPane(f);
        izq.setBorder(null);
        izq.setPreferredSize(new Dimension(500, 0));
        izq.getVerticalScrollBar().setUnitIncrement(16);

        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(izq, BorderLayout.WEST);
        JPanel der = new JPanel(new BorderLayout(0, 8));
        der.setOpaque(false);
        der.add(conTitulo(tblControles, "Controles registrados"), BorderLayout.CENTER);
        der.add(panelCierre(), BorderLayout.SOUTH);
        p.add(der, BorderLayout.CENTER);
        setContenido(p);

        ejecutar(() -> llenarCombo(cmbInspector, mecanicos.listarActivos()));
        cargarOrdenes(null);
    }

    private JPanel panelCierre() {
        barra.setStringPainted(true);
        barra.setPreferredSize(new Dimension(300, 24));
        txtResultado.setEditable(false);
        txtResultado.setBackground(EstiloUI.FONDO_CLARO);
        JScrollPane res = new JScrollPane(txtResultado);
        res.setBorder(javax.swing.BorderFactory.createLineBorder(EstiloUI.BORDE));
        res.setPreferredSize(new Dimension(300, 82));
        btnCerrar.addActionListener(e -> cerrarOrden());
        btnIrPagos.addActionListener(e -> irAPagos());
        btnIrPagos.setVisible(false);
        Formulario f = new Formulario("Cierre de la orden")
                .ancho(nota("Calcula la garantía según el nivel de los servicios y, en una sola transacción, "
                        + "genera la garantía, finaliza la orden y emite la factura. La ventana sigue respondiendo "
                        + "mientras el hilo trabaja."))
                .ancho(botones(btnCerrar, btnIrPagos))
                .ancho(barra)
                .ancho(lblProceso)
                .ancho(res);
        return f;
    }

    private void cerrarOrden() {
        OrdenResumen o = orden();
        if (o == null || o.getEstado() != EstadoOrden.EN_CONTROL_CALIDAD) {
            return;
        }
        procesando = true;
        btnCerrar.setEnabled(false);
        btnIrPagos.setVisible(false);
        txtResultado.setText("");
        barra.setValue(0);
        int id = o.getOrdenId();
        new ProcesadorOrdenWorker(id, barra, lblProceso, resultado -> {
            boolean error = resultado.startsWith("ERROR");
            // Se recarga mientras "procesando" sigue activo, para que la recarga no borre el resultado
            cargarOrdenes(id);
            procesando = false;
            txtResultado.setText(resultado);
            txtResultado.setForeground(error ? EstiloUI.ERROR : EstiloUI.TEXTO);
            txtResultado.setCaretPosition(0);
            btnIrPagos.setVisible(!error);
            OrdenResumen actual = orden();
            btnCerrar.setEnabled(actual != null && actual.getEstado() == EstadoOrden.EN_CONTROL_CALIDAD);
            if (error) {
                avisar("No se pudo cerrar la orden:\n" + resultado.substring("ERROR: ".length()));
            }
        }).execute();
    }

    private void irAPagos() {
        Integer id = idOrden();
        java.awt.Window w = Navegador.abrir(this, "FrmPagosFacturacion", "Pagos y facturación");
        if (w instanceof FrmBaseOrden f && id != null) {
            f.mostrarOrden(id);
        }
    }

    private void registrar() {
        List<String> verificados = new ArrayList<>();
        for (JCheckBox c : checks) {
            if (c.isSelected()) {
                verificados.add(c.getText());
            }
        }
        boolean aprobado = rbAprobado.isSelected();
        if (aprobado && verificados.size() < PUNTOS.length) {
            avisar("Para aprobar deben estar verificados todos los puntos de la lista.");
            return;
        }
        OrdenResumen o = orden();
        ejecutar(() -> {
            ordenes.registrarControl(o, (Mecanico) cmbInspector.getSelectedItem(), aprobado, verificados, txtObs.getText());
            if (!aprobado) {
                ordenes.cambiarEstado(o, EstadoOrden.EN_REPARACION);
            }
            checks.forEach(c -> c.setSelected(false));
            txtObs.setText("");
            recargar();
            avisar(aprobado ? "Control aprobado. Ya puede cerrar la orden con \"Cerrar orden y facturar\"."
                    : "Control no aprobado: la orden volvió a reparación.");
        });
    }

    @Override
    protected void ordenSeleccionada(OrdenResumen o) {
        boolean puedeCerrar = o != null && o.getEstado() == EstadoOrden.EN_CONTROL_CALIDAD;
        if (!procesando) {
            btnCerrar.setEnabled(puedeCerrar);
            if (o == null || (o.getEstado() != EstadoOrden.FINALIZADA && o.getEstado() != EstadoOrden.ENTREGADA)) {
                btnIrPagos.setVisible(false);
                txtResultado.setText("");
                barra.setValue(0);
                lblProceso.setText(puedeCerrar ? "Lista para cerrar (si el control obligatorio está aprobado)."
                        : "Disponible cuando la orden esté en control de calidad.");
            }
        }
        if (o == null) {
            limpiar(tblControles);
            return;
        }
        ejecutar(() -> llenar(tblControles, ordenes.controles(o.getOrdenId())));
    }
}
