package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import modelo.Mecanico;
import modelo.OrdenResumen;
import servicio.MecanicoService;

/**
 * Asignación de mecánicos a la orden (uno responsable) y control de las actividades
 * con horas trabajadas. Solo se asignan mecánicos activos y disponibles.
 */
public class FrmAsignacionActividades extends FrmBaseOrden {

    private final MecanicoService mecanicos = new MecanicoService();

    private final JComboBox<Mecanico> cmbMecanico = new JComboBox<>();
    private final JCheckBox chkResponsable = EstiloUI.casilla(new JCheckBox("Es el responsable de la orden"));
    private final JTable tblAsignados = tabla();

    private final JComboBox<Mecanico> cmbMecanicoAct = new JComboBox<>();
    private final JTextField txtActividad = EstiloUI.campoCompacto(new JTextField());
    private final JSpinner spnHoras = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 200.0, 0.5));
    private final JTextField txtObs = EstiloUI.campoCompacto(new JTextField());
    private final JTable tblActividades = tabla();

    public FrmAsignacionActividades() {
        super("Asignación y actividades", "MÓDULO 4: ASIGNACIÓN DE MECÁNICOS Y ACTIVIDADES",
                "Mecánicos por orden, responsable, actividades y horas trabajadas", true);

        JButton asignar = EstiloUI.botonPrimario("Asignar a la orden");
        asignar.addActionListener(e -> {
            if (ejecutar(() -> ordenes.asignarMecanico(orden(), (Mecanico) cmbMecanico.getSelectedItem(),
                    chkResponsable.isSelected()))) {
                chkResponsable.setSelected(false);
                recargar();
            }
        });
        JButton quitar = EstiloUI.botonPeligro("Quitar seleccionado");
        quitar.addActionListener(e -> quitarMecanico());
        Formulario asignacion = new Formulario("Asignar mecánico")
                .campo("Mecánico", cmbMecanico)
                .ancho(chkResponsable)
                .ancho(botones(asignar, quitar))
                .ancho(nota("Solo aparecen mecánicos activos y disponibles. Si marca otro responsable, "
                        + "reemplaza al anterior. Para quitar a alguien, selecciónelo en la tabla de la derecha."))
                .relleno();

        JButton agregar = EstiloUI.botonPrimario("Agregar actividad");
        agregar.addActionListener(e -> {
            if (ejecutar(() -> ordenes.agregarActividad(orden(), (Mecanico) cmbMecanicoAct.getSelectedItem(),
                    txtActividad.getText()))) {
                txtActividad.setText("");
                ordenSeleccionada(orden());
            }
        });
        JButton quitarAct = EstiloUI.botonPeligro("Quitar actividad");
        quitarAct.addActionListener(e -> quitarActividad());
        JButton iniciar = EstiloUI.botonSecundario("Iniciar seleccionada");
        iniciar.addActionListener(e -> conActividad(id -> ordenes.iniciarActividad(id)));
        JButton completar = EstiloUI.botonSecundario("Completar seleccionada");
        completar.addActionListener(e -> conActividad(id -> {
            try {
                spnHoras.commitEdit();
            } catch (java.text.ParseException ex) {
                throw new excepciones.ValidacionException("Escriba las horas con números, por ejemplo 1 o 1.5.");
            }
            ordenes.completarActividad(id, BigDecimal.valueOf(((Number) spnHoras.getValue()).doubleValue()),
                    txtObs.getText());
        }));
        Formulario actividad = new Formulario("Actividades de trabajo")
                .campo("Mecánico", cmbMecanicoAct)
                .campo("Actividad", txtActividad)
                .ancho(botones(agregar, quitarAct))
                .campo("Horas trabajadas", spnHoras)
                .campo("Observación", txtObs)
                .ancho(botones(iniciar, completar))
                .ancho(nota("Las horas se guardan al presionar \"Completar seleccionada\"."))
                .relleno();

        JPanel izq = new JPanel(new GridLayout(2, 1, 0, 8));
        izq.setOpaque(false);
        izq.add(asignacion);
        izq.add(actividad);
        izq.setPreferredSize(new Dimension(440, 0));

        JPanel der = new JPanel(new GridLayout(2, 1, 0, 8));
        der.setOpaque(false);
        der.add(conTitulo(tblAsignados, "Mecánicos asignados"));
        der.add(conTitulo(tblActividades, "Actividades (seleccione una para iniciarla o completarla)"));

        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(izq, BorderLayout.WEST);
        p.add(der, BorderLayout.CENTER);
        setContenido(p);

        ejecutar(() -> {
            llenarCombo(cmbMecanico, mecanicos.listarDisponibles());
            llenarCombo(cmbMecanicoAct, mecanicos.listarActivos());
        });
        mostrarCon(cmbMecanico, m -> m + " · " + m.getNivelCertificacion() + " · L " + dinero(m.getTarifaHora()) + "/h");
        cargarOrdenes(null);
    }

    private void quitarMecanico() {
        Integer id = idSeleccionado(tblAsignados);
        if (id == null) {
            avisar("Seleccione en la tabla \"Mecánicos asignados\" al mecánico que desea quitar.");
            return;
        }
        int fila = tblAsignados.convertRowIndexToModel(tblAsignados.getSelectedRow());
        String nombre = String.valueOf(tblAsignados.getModel().getValueAt(fila, 2));
        if (!confirmar("¿Quitar a " + nombre + " de la orden " + orden().getNumero() + "?")) {
            return;
        }
        if (ejecutar(() -> ordenes.quitarMecanico(orden(), id, nombre))) {
            recargar();
        }
    }

    private void quitarActividad() {
        Integer id = idSeleccionado(tblActividades);
        if (id == null) {
            avisar("Seleccione en la tabla de actividades la que desea quitar.");
            return;
        }
        int fila = tblActividades.convertRowIndexToModel(tblActividades.getSelectedRow());
        String descripcion = String.valueOf(tblActividades.getModel().getValueAt(fila, 1));
        if (!confirmar("¿Quitar la actividad \"" + descripcion + "\" de la orden " + orden().getNumero() + "?")) {
            return;
        }
        if (ejecutar(() -> ordenes.quitarActividad(orden(), id))) {
            ordenSeleccionada(orden());
        }
    }

    private interface AccionId {
        void con(int id) throws Exception;
    }

    private void conActividad(AccionId accion) {
        Integer id = idSeleccionado(tblActividades);
        if (id == null) {
            avisar("Seleccione una actividad de la tabla.");
            return;
        }
        if (ejecutar(() -> accion.con(id))) {
            txtObs.setText("");
            ordenSeleccionada(orden());
        }
    }

    @Override
    protected void ordenSeleccionada(OrdenResumen o) {
        if (o == null) {
            limpiar(tblAsignados);
            limpiar(tblActividades);
            return;
        }
        ejecutar(() -> {
            llenar(tblAsignados, ordenes.asignaciones(o.getOrdenId()));
            llenar(tblActividades, ordenes.actividades(o.getOrdenId()));
        });
    }
}
