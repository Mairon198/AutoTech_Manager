package vista;

import excepciones.AutoTechException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import modelo.OrdenResumen;
import modelo.TablaResultado;
import servicio.OrdenTrabajoService;
import util.Configuracion;

/**
 * Base de los formularios que trabajan sobre una orden de trabajo (módulos 5 a 8).
 * Arma el encabezado, el selector de orden con su resumen y ofrece utilidades
 * comunes: llenar tablas, mostrar errores de negocio y armar formularios.
 */
public abstract class FrmBaseOrden extends JFrame {

    /** Acción de un botón que puede lanzar excepciones de negocio. */
    @FunctionalInterface
    protected interface Accion {
        void ejecutar() throws Exception;
    }

    protected static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    protected final OrdenTrabajoService ordenes = new OrdenTrabajoService();
    protected final JComboBox<OrdenResumen> cmbOrden = new JComboBox<>();
    private final JCheckBox chkSoloAbiertas = EstiloUI.casilla(new JCheckBox("Solo abiertas"));
    private final JLabel lblResumen = new JLabel(" ");
    private boolean cargando;

    protected FrmBaseOrden(String tituloVentana, String encabezado, String subtitulo, boolean soloAbiertas) {
        setTitle("AutoTech Manager — " + tituloVentana);
        setSize(1200, 740);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(EstiloUI.FONDO_CLARO);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(encabezado(encabezado, subtitulo), BorderLayout.NORTH);
        norte.add(barraOrden(soloAbiertas), BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
    }

    private JPanel encabezado(String titulo, String subtitulo) {
        JPanel p = EstiloUI.panelEncabezado();
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel t = new JLabel(titulo);
        t.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitulo);
        s.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        s.setForeground(EstiloUI.TEXTO_ENCABEZADO);
        p.add(t, BorderLayout.WEST);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    private JPanel barraOrden(boolean soloAbiertas) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, EstiloUI.BORDE),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);
        fila.add(EstiloUI.etiqueta(new JLabel("Orden de trabajo:")));
        cmbOrden.setPreferredSize(new Dimension(560, 30));
        cmbOrden.setMaximumRowCount(16);
        cmbOrden.addActionListener(e -> {
            if (!cargando) {
                actualizarResumen();
                ordenSeleccionada(orden());
            }
        });
        fila.add(cmbOrden);
        chkSoloAbiertas.setSelected(soloAbiertas);
        chkSoloAbiertas.addActionListener(e -> cargarOrdenes(idOrden()));
        fila.add(chkSoloAbiertas);
        JButton actualizar = EstiloUI.botonSecundario("Actualizar");
        actualizar.addActionListener(e -> recargar());
        fila.add(actualizar);

        lblResumen.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblResumen.setForeground(EstiloUI.TEXTO_SUAVE);
        lblResumen.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        p.add(fila, BorderLayout.NORTH);
        p.add(lblResumen, BorderLayout.SOUTH);
        return p;
    }

    /** Coloca el contenido propio del formulario debajo del selector. */
    protected void setContenido(JComponent contenido) {
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        add(contenido, BorderLayout.CENTER);
    }

    /** Se llama cada vez que cambia la orden seleccionada (o se recarga). Puede ser null. */
    protected abstract void ordenSeleccionada(OrdenResumen orden);

    // ------------------------------------------------------------ órdenes

    protected OrdenResumen orden() {
        return (OrdenResumen) cmbOrden.getSelectedItem();
    }

    protected Integer idOrden() {
        OrdenResumen o = orden();
        return o == null ? null : o.getOrdenId();
    }

    /** Muestra una orden concreta (lo usa el menú principal al abrir el módulo desde el tablero). */
    public void mostrarOrden(int ordenId) {
        cargarOrdenes(ordenId);
    }

    /** Vuelve a leer las órdenes de la base manteniendo la seleccionada. */
    protected void recargar() {
        cargarOrdenes(idOrden());
    }

    protected void cargarOrdenes(Integer seleccionar) {
        cargando = true;
        try {
            List<OrdenResumen> lista = ordenes.listar(chkSoloAbiertas.isSelected());
            DefaultComboBoxModel<OrdenResumen> m = new DefaultComboBoxModel<>();
            OrdenResumen elegida = null;
            for (OrdenResumen o : lista) {
                m.addElement(o);
                if (seleccionar != null && o.getOrdenId() == seleccionar) {
                    elegida = o;
                }
            }
            if (seleccionar != null && elegida == null) {
                // la orden quedó cerrada y se filtra: se muestra igual para ver el resultado
                elegida = ordenes.buscar(seleccionar).orElse(null);
                if (elegida != null) {
                    m.insertElementAt(elegida, 0);
                }
            }
            cmbOrden.setModel(m);
            if (elegida != null) {
                cmbOrden.setSelectedItem(elegida);
            } else if (m.getSize() > 0) {
                cmbOrden.setSelectedIndex(0);
            }
        } catch (Exception e) {
            error(e);
        } finally {
            cargando = false;
        }
        actualizarResumen();
        ordenSeleccionada(orden());
    }

    private void actualizarResumen() {
        OrdenResumen o = orden();
        if (o == null) {
            lblResumen.setText("No hay órdenes para mostrar.");
            return;
        }
        lblResumen.setText(String.format(
                "<html>Cliente: <b>%s</b> &nbsp;·&nbsp; Vehículo: <b>%s %s</b> &nbsp;·&nbsp; Estado: <b>%s</b>"
                + " &nbsp;·&nbsp; Responsable: %s &nbsp;·&nbsp; Total: L %s &nbsp;·&nbsp; Saldo: L %s%s</html>",
                o.getCliente(), o.getPlaca(), o.getVehiculo(), o.getEstado(),
                o.getResponsable() == null ? "—" : o.getResponsable(), dinero(o.getTotal()), dinero(o.getSaldo()),
                o.getNumeroFactura() == null ? "" : " &nbsp;·&nbsp; Factura: " + o.getNumeroFactura()));
    }

    // ------------------------------------------------------------ mensajes

    /** Ejecuta la acción y muestra el error de forma entendible si falla. */
    protected boolean ejecutar(Accion accion) {
        try {
            accion.ejecutar();
            return true;
        } catch (Exception e) {
            error(e);
            return false;
        }
    }

    protected void error(Exception e) {
        if (e instanceof AutoTechException) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se puede realizar", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "AutoTech Manager", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(this, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    protected static String usuario() {
        if (util.Sesion.activa()) {
            return util.Sesion.nombreUsuario();
        }
        String u = Configuracion.get("db.user");
        return u == null || u.isBlank() ? System.getProperty("user.name") : u;
    }

    // ------------------------------------------------------------ tablas

    protected static JTable tabla() {
        JTable t = new JTable(new DefaultTableModel()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        EstiloUI.tabla(t);
        return t;
    }

    protected static JScrollPane conTitulo(JComponent c, String titulo) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(EstiloUI.titulado(titulo));
        s.getViewport().setBackground(Color.WHITE);
        return s;
    }

    /** Muestra un resultado tabular; si la primera columna es "Id" queda oculta pero disponible. */
    protected static void llenar(JTable t, TablaResultado r) {
        DefaultTableModel m = new DefaultTableModel(r.getColumnas().stream().map(FrmBaseOrden::conTildes).toArray(), 0);
        for (Object[] f : r.getFilas()) {
            Object[] fila = new Object[f.length];
            for (int i = 0; i < f.length; i++) {
                fila[i] = formatear(f[i]);
            }
            m.addRow(fila);
        }
        t.setModel(m);
        if (!r.getColumnas().isEmpty() && r.getColumnas().get(0).equalsIgnoreCase("Id")) {
            t.getColumnModel().getColumn(0).setMinWidth(0);
            t.getColumnModel().getColumn(0).setMaxWidth(0);
            t.getColumnModel().getColumn(0).setPreferredWidth(0);
        }
    }

    protected static void limpiar(JTable t) {
        t.setModel(new DefaultTableModel());
    }

    private static final java.util.Map<String, String> TILDES = java.util.Map.ofEntries(
            java.util.Map.entry("Codigo", "Código"), java.util.Map.entry("Mecanico", "Mecánico"),
            java.util.Map.entry("Descripcion", "Descripción"), java.util.Map.entry("Numero", "Número"),
            java.util.Map.entry("Diagnostico", "Diagnóstico"), java.util.Map.entry("Recomendacion", "Recomendación"),
            java.util.Map.entry("raiz", "raíz"), java.util.Map.entry("Metodo", "Método"),
            java.util.Map.entry("Observacion", "Observación"), java.util.Map.entry("Garantia", "Garantía"),
            java.util.Map.entry("Telefono", "Teléfono"), java.util.Map.entry("emision", "emisión"),
            java.util.Map.entry("Instalacion", "Instalación"), java.util.Map.entry("instalacion", "instalación"),
            java.util.Map.entry("Ultimo", "Último"), java.util.Map.entry("Linea", "Línea"));

    private static String conTildes(String columna) {
        StringBuilder sb = new StringBuilder();
        for (String p : columna.split(" ")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(TILDES.getOrDefault(p, p));
        }
        return sb.toString();
    }

    /** Convierte códigos internos (EN_PROCESO, MANO_OBRA, EFECTIVO...) en texto legible. */
    private static String legible(String s) {
        if (s.length() < 4 || !s.matches("[A-Z]+(_[A-Z]+)*")) {
            return s;
        }
        for (Class<? extends Enum<?>> e : java.util.List.<Class<? extends Enum<?>>>of(
                modelo.enums.EstadoOrden.class, modelo.enums.EstadoActividad.class, modelo.enums.EstadoPresupuesto.class,
                modelo.enums.MetodoPago.class, modelo.enums.EstadoMotor.class, modelo.enums.DestinoMotorRetirado.class)) {
            for (Enum<?> c : e.getEnumConstants()) {
                if (c.name().equals(s)) {
                    return c.toString();
                }
            }
        }
        if (s.equals("MANO_OBRA")) {
            return "Mano de obra";
        }
        String t = s.replace('_', ' ').toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private static Object formatear(Object v) {
        if (v instanceof String s) {
            return legible(s);
        }
        if (v instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().format(FECHA);
        }
        if (v instanceof LocalDateTime d) {
            return d.format(FECHA);
        }
        if (v instanceof BigDecimal b) {
            return b.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        if (v instanceof Boolean b) {
            return b ? "Sí" : "No";
        }
        return v;
    }

    /** Id (primera columna) de la fila seleccionada, o null. */
    protected static Integer idSeleccionado(JTable t) {
        int fila = t.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        Object v = t.getModel().getValueAt(t.convertRowIndexToModel(fila), 0);
        return v instanceof Number n ? n.intValue() : null;
    }

    protected static String dinero(BigDecimal v) {
        return v == null ? "0.00" : String.format("%,.2f", v);
    }

    // ------------------------------------------------------------ formularios

    /** Combo que muestra el texto que se le indique para cada elemento. */
    protected static <T> void mostrarCon(JComboBox<T> combo, Function<T, String> texto) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? " " : texto.apply((T) value));
                return this;
            }
        });
    }

    protected static <T> void llenarCombo(JComboBox<T> combo, List<T> elementos) {
        DefaultComboBoxModel<T> m = new DefaultComboBoxModel<>();
        elementos.forEach(m::addElement);
        combo.setModel(m);
    }

    protected static JTextArea area(int filas) {
        JTextArea a = new JTextArea(filas, 20);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(EstiloUI.fuente(Font.PLAIN, 13f));
        a.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return a;
    }

    /** Panel de formulario de dos columnas (etiqueta, campo) con GridBagLayout. */
    protected static final class Formulario extends JPanel implements javax.swing.Scrollable {
        private int fila;

        // Dentro de un JScrollPane solo se desplaza en vertical: el ancho se ajusta a la ventana
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) {
            return r.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() > getPreferredSize().height;
        }

        public Formulario(String titulo) {
            super(new GridBagLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(EstiloUI.titulado(titulo),
                    BorderFactory.createEmptyBorder(4, 8, 8, 8)));
        }

        public Formulario campo(String etiqueta, JComponent c) {
            GridBagConstraints g = base();
            g.gridx = 0;
            g.weightx = 0;
            g.anchor = GridBagConstraints.NORTHWEST;
            g.insets = new Insets(9, 0, 4, 10);
            add(EstiloUI.etiqueta(new JLabel(etiqueta)), g);
            g = base();
            g.gridx = 1;
            if (c instanceof JTextArea) {
                JScrollPane s = new JScrollPane(c);
                s.setBorder(BorderFactory.createLineBorder(EstiloUI.BORDE));
                s.setMinimumSize(new Dimension(120, c.getPreferredSize().height + 4));
                add(s, g);
            } else {
                add(c, g);
            }
            fila++;
            return this;
        }

        public Formulario ancho(JComponent c) {
            GridBagConstraints g = base();
            g.gridx = 0;
            g.gridwidth = 2;
            g.weightx = 0;
            add(c, g);
            fila++;
            return this;
        }

        /** Empuja el contenido hacia arriba. */
        public Formulario relleno() {
            GridBagConstraints g = base();
            g.gridx = 0;
            g.gridwidth = 2;
            g.weightx = 0;
            g.weighty = 1;
            add(new JLabel(), g);
            fila++;
            return this;
        }

        private GridBagConstraints base() {
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = fila;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(4, 0, 4, 0);
            return g;
        }
    }

    /** Fila de botones alineada a la izquierda. */
    protected static JPanel botones(JComponent... bs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        for (JComponent b : bs) {
            p.add(b);
        }
        return p;
    }

    /** Texto de ayuda que se ajusta en varias líneas. */
    protected static JLabel nota(String texto) {
        JLabel l = new JLabel("<html><body style='width:330px'>" + texto + "</body></html>");
        l.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        l.setForeground(EstiloUI.TEXTO_SUAVE);
        return l;
    }
}
