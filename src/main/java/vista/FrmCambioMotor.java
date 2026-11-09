package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Motor;
import modelo.OrdenResumen;
import modelo.enums.DestinoMotorRetirado;
import servicio.CambioMotorService;

/**
 * Módulo 7: reparación mayor con sustitución de motor (caso Toyota Hilux 2015).
 * Muestra los motores con su compatibilidad, registra desmontaje, instalación y pruebas,
 * y lleva el avance de los 11 pasos del caso.
 */
public class FrmCambioMotor extends FrmBaseOrden {

    private final CambioMotorService servicio = new CambioMotorService();
    private final List<Motor> motores = new ArrayList<>();
    private Set<Integer> compatibles = Set.of();

    private final JTable tblMotores = tabla();
    private final JTextField txtSerieAnterior = EstiloUI.campoCompacto(new JTextField());
    private final JComboBox<DestinoMotorRetirado> cmbDestino = new JComboBox<>(DestinoMotorRetirado.values());
    private final JTextField txtDesmontaje = EstiloUI.campoCompacto(new JTextField());
    private final JTextField txtInstalacion = EstiloUI.campoCompacto(new JTextField());
    private final JTextArea txtPruebas = area(3);
    private final JCheckBox chkAprobadas = EstiloUI.casilla(new JCheckBox("Pruebas aprobadas"));
    private final JTextArea txtObs = area(2);
    private final JTable tblPasos = tabla();
    private final JTable tblCambio = tabla();

    public FrmCambioMotor() {
        super("Cambio de motor", "MÓDULO 7: REPARACIÓN MAYOR — SUSTITUCIÓN DE MOTOR",
                "Compatibilidad, trazabilidad del motor anterior y nuevo, pruebas y avance del caso", true);

        JButton registrar = EstiloUI.botonPrimario("Registrar cambio de motor");
        registrar.addActionListener(e -> registrar());
        JButton ahora1 = EstiloUI.enlace(new JButton("ahora"));
        ahora1.addActionListener(e -> txtDesmontaje.setText(LocalDateTime.now().minusHours(6).format(FECHA)));
        JButton ahora2 = EstiloUI.enlace(new JButton("ahora"));
        ahora2.addActionListener(e -> txtInstalacion.setText(LocalDateTime.now().format(FECHA)));

        Formulario f = new Formulario("Registro del cambio")
                .ancho(nota("Seleccione el motor nuevo en la tabla de la derecha. Solo se permite instalar un motor "
                        + "<b>disponible</b> y <b>compatible</b>; la orden debe estar <b>En reparación</b> con el "
                        + "presupuesto aprobado."))
                .campo("Serie motor anterior", txtSerieAnterior)
                .campo("Destino del retirado", cmbDestino)
                .campo("Desmontaje", conBoton(txtDesmontaje, ahora1))
                .campo("Instalación", conBoton(txtInstalacion, ahora2))
                .campo("Resultado de pruebas", txtPruebas)
                .ancho(chkAprobadas)
                .campo("Observaciones", txtObs)
                .ancho(botones(registrar))
                .ancho(nota("Fechas en formato dd/MM/aaaa HH:mm. Al registrar se descuenta el motor del inventario, "
                        + "queda instalado y se actualiza la serie del vehículo."))
                .relleno();
        JScrollPane izq = new JScrollPane(f);
        izq.setBorder(null);
        izq.setPreferredSize(new Dimension(450, 0));
        izq.getVerticalScrollBar().setUnitIncrement(16);

        tblPasos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foco, r, c);
                Object estado = t.getModel().getValueAt(t.convertRowIndexToModel(r), 2);
                if (!sel) {
                    setForeground("Completo".equals(estado) ? new Color(0x047857) : EstiloUI.TEXTO);
                }
                return this;
            }
        });
        JSplitPane der = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                conTitulo(tblMotores, "Motores en inventario (verde = compatible con el vehículo)"),
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                        conTitulo(tblPasos, "Avance del caso (11 pasos)"),
                        conTitulo(tblCambio, "Cambio registrado")));
        der.setResizeWeight(0.42);
        der.setBorder(null);
        ((JSplitPane) der.getBottomComponent()).setResizeWeight(0.55);
        ((JSplitPane) der.getBottomComponent()).setBorder(null);

        tblMotores.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foco, r, c);
                Object comp = t.getModel().getValueAt(t.convertRowIndexToModel(r), 6);
                if (!sel) {
                    setBackground("Sí".equals(comp) ? EstiloUI.EXITO_CLARO : Color.WHITE);
                }
                return this;
            }
        });

        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(izq, BorderLayout.WEST);
        p.add(der, BorderLayout.CENTER);
        setContenido(p);
        cargarOrdenes(null);
        seleccionarHiluxSiExiste();
    }

    private static JPanel conBoton(JTextField campo, JButton boton) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        p.add(campo, BorderLayout.CENTER);
        p.add(boton, BorderLayout.EAST);
        return p;
    }

    /** Para la defensa: si hay una orden abierta que requiere cambio de motor, se selecciona. */
    private void seleccionarHiluxSiExiste() {
        for (int i = 0; i < cmbOrden.getItemCount(); i++) {
            if (cmbOrden.getItemAt(i).isRequiereCambioMotor()) {
                cmbOrden.setSelectedIndex(i);
                return;
            }
        }
    }

    private void registrar() {
        int fila = tblMotores.getSelectedRow();
        Motor motor = fila < 0 ? null : motores.get(tblMotores.convertRowIndexToModel(fila));
        LocalDateTime desmontaje;
        LocalDateTime instalacion;
        try {
            desmontaje = LocalDateTime.parse(txtDesmontaje.getText().trim(), FECHA);
            instalacion = LocalDateTime.parse(txtInstalacion.getText().trim(), FECHA);
        } catch (DateTimeParseException ex) {
            avisar("Escriba las fechas con el formato dd/MM/aaaa HH:mm (o use \"ahora\").");
            return;
        }
        ejecutar(() -> {
            servicio.registrar(orden(), motor, txtSerieAnterior.getText(), (DestinoMotorRetirado) cmbDestino.getSelectedItem(),
                    desmontaje, instalacion, txtPruebas.getText(), chkAprobadas.isSelected(), txtObs.getText(), usuario());
            recargar();
            avisar("Cambio de motor registrado.\nEl motor " + motor.getNumeroSerie() + " quedó instalado en el vehículo.");
        });
    }

    @Override
    protected void ordenSeleccionada(OrdenResumen o) {
        motores.clear();
        limpiar(tblMotores);
        limpiar(tblPasos);
        limpiar(tblCambio);
        if (o == null) {
            return;
        }
        ejecutar(() -> {
            motores.addAll(servicio.motores(o.getVehiculoId()));
            compatibles = servicio.compatibles(o.getVehiculoId());
            DefaultTableModel m = new DefaultTableModel(new Object[]{
                "Serie", "Marca", "Modelo", "Cilindrada", "Combustible", "Estado", "Compatible", "Nuevo", "Costo"}, 0);
            for (Motor x : motores) {
                m.addRow(new Object[]{x.getNumeroSerie(), x.getMarca(), x.getModeloMotor(), x.getCilindrada(),
                        x.getCombustible(), x.getEstado(), compatibles.contains(x.getId()) ? "Sí" : "No",
                        x.isEsNuevo() ? "Sí" : "No", "L " + dinero(x.getCosto())});
            }
            tblMotores.setModel(m);

            boolean[] avance = servicio.avance(o.getOrdenId());
            DefaultTableModel pasos = new DefaultTableModel(new Object[]{"#", "Paso", "Estado"}, 0);
            for (int i = 0; i < CambioMotorService.PASOS.size(); i++) {
                pasos.addRow(new Object[]{i + 1, CambioMotorService.PASOS.get(i), avance[i] ? "Completo" : "Pendiente"});
            }
            tblPasos.setModel(pasos);
            tblPasos.getColumnModel().getColumn(0).setMaxWidth(36);
            tblPasos.getColumnModel().getColumn(2).setMaxWidth(90);

            llenar(tblCambio, servicio.cambioRegistrado(o.getOrdenId()));
            txtSerieAnterior.setText(servicio.serieActual(o.getVehiculoId()));
        });
    }
}
