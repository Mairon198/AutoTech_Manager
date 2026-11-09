package vista;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public final class EstiloUI {

    // Tema AutoTech: rojo taller sobre grafito
    public static final Color PRIMARIO = new Color(0xC62828);
    public static final Color PRIMARIO_HOVER = new Color(0xDC3B3B);
    public static final Color PRIMARIO_PRESIONADO = new Color(0x8E1C1C);
    /** Color oscuro del tema (encabezados). Conserva el nombre para no romper los formularios existentes. */
    public static final Color AZUL_OSCURO = new Color(0x2A0D10);
    public static final Color TEXTO_ENCABEZADO = new Color(0xF6D5D5);
    public static final Color TEXTO = new Color(0x1F2937);
    public static final Color TEXTO_SUAVE = new Color(0x6B7280);
    public static final Color BORDE = new Color(0xD1D5DB);
    public static final Color FONDO_CLARO = new Color(0xF3F4F6);
    /** Acento claro del tema (selección y resaltados). */
    public static final Color ACENTO_CLARO = new Color(0xFDE4E4);
    /** Se conserva por compatibilidad: ahora es el acento claro rojo. */
    public static final Color VERDE_CLARO = ACENTO_CLARO;
    public static final Color EXITO = new Color(0x047857);
    public static final Color EXITO_CLARO = new Color(0xDCFCE7);
    public static final Color ERROR = new Color(0xB91C1C);
    public static final Color ERROR_FONDO = new Color(0xFEE2E2);
    private static final Color DESHABILITADO = new Color(0x9CA3AF);

    private EstiloUI() {
    }

    public static Font fuente(int estilo, float tamano) {
        return new Font("Segoe UI", estilo, 12).deriveFont(estilo, tamano);
    }

    private static void suavizar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static <T extends JTextField> T campo(T c) {
        return campo(c, 14f, new Insets(10, 14, 10, 14));
    }

    public static <T extends JTextField> T campoCompacto(T c) {
        return campo(c, 13f, new Insets(6, 10, 6, 10));
    }

    private static <T extends JTextField> T campo(T c, float tamano, Insets margen) {
        c.setFont(fuente(Font.PLAIN, tamano));
        c.setForeground(TEXTO);
        c.setBackground(Color.WHITE);
        c.setCaretColor(PRIMARIO);
        c.setBorder(new BordeRedondeado(BORDE, 1, margen));
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                c.setBorder(new BordeRedondeado(PRIMARIO, 2, margen));
            }

            @Override
            public void focusLost(FocusEvent e) {
                c.setBorder(new BordeRedondeado(BORDE, 1, margen));
            }
        });
        return c;
    }

    public static JButton botonPrimario(String texto) {
        return primario(new JButton(texto));
    }

    public static JButton botonSecundario(String texto) {
        return secundario(new JButton(texto));
    }

    public static JButton botonPeligro(String texto) {
        return boton(new JButton(texto), new BotonUI(Color.WHITE, ERROR_FONDO, new Color(0xFECACA), ERROR, ERROR, null), 13f);
    }

    public static JButton primario(JButton b) {
        return boton(b, new BotonUI(PRIMARIO, PRIMARIO_HOVER, PRIMARIO_PRESIONADO, null, Color.WHITE, null), 14f);
    }

    public static JButton secundario(JButton b) {
        return boton(b, new BotonUI(Color.WHITE, FONDO_CLARO, BORDE, BORDE, TEXTO, null), 13f);
    }

    public static <T extends AbstractButton> T boton(T b, BotonUI ui, float tamano) {
        b.setUI(ui);
        b.setFont(fuente(Font.BOLD, tamano));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JToggleButton segmento(JToggleButton b) {
        boton(b, new BotonUI(null, FONDO_CLARO, FONDO_CLARO, null, TEXTO_SUAVE, PRIMARIO), 13f);
        b.setPreferredSize(new Dimension(150, 36));
        return b;
    }

    public static JButton enlace(JButton b) {
        b.setFont(fuente(Font.BOLD, 13f));
        b.setForeground(PRIMARIO);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setMargin(new Insets(0, 4, 0, 4));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(PRIMARIO_PRESIONADO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(PRIMARIO);
            }
        });
        return b;
    }

    public static JCheckBox casilla(JCheckBox c) {
        c.setOpaque(false);
        c.setFont(fuente(Font.PLAIN, 12f));
        c.setForeground(TEXTO_SUAVE);
        c.setFocusPainted(false);
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return c;
    }

    public static JLabel etiqueta(JLabel l) {
        l.setFont(fuente(Font.BOLD, 12f));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel mensajeError(JLabel l, int alto) {
        l.setText(" ");
        l.setFont(fuente(Font.PLAIN, 12f));
        l.setForeground(ERROR);
        l.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        l.setPreferredSize(new Dimension(10, alto));
        l.setMinimumSize(new Dimension(10, alto));
        return l;
    }

    public static void mostrarError(JLabel l, String texto) {
        if (texto == null || texto.isBlank()) {
            l.setOpaque(false);
            l.setText(" ");
        } else {
            l.setOpaque(true);
            l.setBackground(ERROR_FONDO);
            l.setText("<html>" + texto + "</html>");
        }
        l.repaint();
    }

    public static void aplicarTema(JFrame ventana, JPanel encabezado, JButton... primarios) {
        Dimension antes = ventana.getSize();
        ventana.getContentPane().setBackground(Color.WHITE);
        estilizar(ventana.getContentPane());
        if (encabezado != null) {
            encabezado(encabezado);
        }
        for (JButton b : primarios) {
            if (b != null) {
                primario(b);
            }
        }
        ventana.pack();
        Dimension despues = ventana.getSize();
        ventana.setSize(Math.max(antes.width, despues.width), Math.max(antes.height, despues.height));
        ventana.setLocationRelativeTo(null);
    }

    public static void estilizar(Container contenedor) {
        for (Component c : contenedor.getComponents()) {
            if (c instanceof JPanel p) {
                p.setBackground(Color.WHITE);
                if (p.getBorder() instanceof TitledBorder t) {
                    p.setBorder(titulado(t.getTitle()));
                }
            } else if (c instanceof JScrollPane s) {
                s.setBorder(BorderFactory.createLineBorder(BORDE));
                s.getViewport().setBackground(Color.WHITE);
            } else if (c instanceof JTable t) {
                tabla(t);
            } else if (c instanceof JTabbedPane tp) {
                tp.setFont(fuente(Font.BOLD, 13f));
                tp.setForeground(TEXTO);
                tp.setBackground(Color.WHITE);
            } else if (c instanceof JButton b) {
                secundario(b);
            } else if (c instanceof JCheckBox cb) {
                casilla(cb);
                cb.setForeground(TEXTO);
            } else if (c instanceof JTextField tf) {
                campoCompacto(tf);
                if (!tf.isEditable()) {
                    tf.setBackground(FONDO_CLARO);
                }
            } else if (c instanceof JTextArea ta) {
                ta.setFont(fuente(Font.PLAIN, 13f));
                ta.setForeground(TEXTO);
            } else if (c instanceof JComboBox<?> cb) {
                cb.setFont(fuente(Font.PLAIN, 13f));
                cb.setBackground(Color.WHITE);
            } else if (c instanceof JLabel l) {
                Font f = l.getFont();
                l.setFont(fuente(f.getStyle(), Math.max(12f, f.getSize2D())));
                if (l.getForeground() instanceof UIResource || Color.BLACK.equals(l.getForeground())) {
                    l.setForeground(TEXTO);
                }
            }
            if (c instanceof JPanel || c instanceof JScrollPane || c instanceof JViewport || c instanceof JTabbedPane) {
                estilizar((Container) c);
            }
        }
    }

    public static void agregarEncabezado(JFrame ventana, String titulo, String subtitulo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        JLabel t = new JLabel(titulo);
        t.setFont(fuente(Font.BOLD, 24f));
        JLabel s = new JLabel(subtitulo);
        s.setFont(fuente(Font.PLAIN, 13f));
        p.add(t, BorderLayout.CENTER);
        p.add(s, BorderLayout.SOUTH);
        Container contenido = ventana.getContentPane();
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(p, BorderLayout.NORTH);
        raiz.add(contenido, BorderLayout.CENTER);
        ventana.setContentPane(raiz);
        encabezado(p);
    }

    public static void encabezado(JPanel p) {
        Insets i = p.getInsets();
        p.setOpaque(true);
        p.setBorder(new FondoDegradado(new Insets(Math.max(14, i.top), 24, Math.max(14, i.bottom), 24)));
        JLabel titulo = null;
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel l && (titulo == null || l.getFont().getSize2D() > titulo.getFont().getSize2D())) {
                titulo = l;
            }
        }
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel l) {
                if (l == titulo) {
                    l.setFont(fuente(Font.BOLD, 22f));
                    l.setForeground(Color.WHITE);
                } else if (l.getForeground() instanceof UIResource || TEXTO.equals(l.getForeground())) {
                    l.setFont(fuente(Font.PLAIN, 13f));
                    l.setForeground(TEXTO_ENCABEZADO);
                } else {
                    l.setFont(fuente(Font.BOLD, 13f));
                    l.setForeground(new Color(0xFDE68A));
                }
            } else if (c instanceof JComponent j) {
                j.setOpaque(false);
            }
        }
        if (p.getLayout() instanceof BorderLayout bl && bl.getLayoutComponent(BorderLayout.EAST) == null) {
            JLabel logo = new JLabel(iconoTaller(76, Color.WHITE));
            logo.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
            p.add(logo, BorderLayout.EAST);
        }
    }

    public static Border titulado(String titulo) {
        TitledBorder t = BorderFactory.createTitledBorder(new BordeRedondeado(BORDE, 1, new Insets(8, 10, 8, 10)), titulo);
        t.setTitleFont(fuente(Font.BOLD, 13f));
        t.setTitleColor(PRIMARIO);
        return BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0), t);
    }

    public static void tabla(JTable t) {
        t.setFont(fuente(Font.PLAIN, 12.5f));
        t.setRowHeight(27);
        t.setForeground(TEXTO);
        t.setBackground(Color.WHITE);
        t.setGridColor(new Color(0xE5E7EB));
        t.setShowVerticalLines(false);
        t.setSelectionBackground(VERDE_CLARO);
        t.setSelectionForeground(TEXTO);
        t.setIntercellSpacing(new Dimension(0, 1));
        JTableHeader h = t.getTableHeader();
        if (h != null) {
            DefaultTableCellRenderer r = new DefaultTableCellRenderer();
            r.setBackground(PRIMARIO);
            r.setForeground(Color.WHITE);
            r.setFont(fuente(Font.BOLD, 12.5f));
            r.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            r.setHorizontalAlignment(SwingConstants.LEFT);
            h.setDefaultRenderer((table, valor, sel, foco, fila, col) -> {
                r.setText(valor == null ? "" : valor.toString());
                return r;
            });
            h.setPreferredSize(new Dimension(h.getPreferredSize().width, 32));
            h.setBackground(PRIMARIO);
        }
    }

    /** Ícono del sistema (vehículo con llave y tuerca); "ancho" es el tamaño en píxeles. */
    public static Icon iconoTaller(int ancho, Color color) {
        return new IconoTaller(ancho, color);
    }

    /** Íconos de la ventana (barra de título y barra de tareas) en varios tamaños. */
    public static java.util.List<java.awt.Image> iconosVentana() {
        java.util.List<java.awt.Image> lista = new java.util.ArrayList<>();
        for (int t : new int[]{16, 20, 24, 32, 48, 64, 128, 256}) {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(t, t, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            suavizar(g2);
            g2.setPaint(new GradientPaint(0, 0, PRIMARIO_HOVER, t, t, PRIMARIO_PRESIONADO));
            float r = t * 0.22f;
            g2.fill(new RoundRectangle2D.Float(0, 0, t, t, r, r));
            int m = Math.round(t * 0.10f);
            new IconoTaller(t - 2 * m, Color.WHITE).paintIcon(null, g2, m, m);
            g2.dispose();
            lista.add(img);
        }
        return lista;
    }

    /**
     * Coloca un formulario de captura en una columna lateral con desplazamiento vertical:
     * conserva el tamaño natural de cada campo (nada se aplasta ni se corta) y, si la
     * ventana es baja, aparece la barra de desplazamiento.
     */
    public static JScrollPane panelLateral(JPanel formulario, int ancho) {
        formulario.setPreferredSize(null);
        JPanel contenedor = new ColumnaFormulario();
        contenedor.setBackground(formulario.getBackground());
        contenedor.add(formulario, BorderLayout.NORTH);
        JScrollPane s = new JScrollPane(contenedor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.getVerticalScrollBar().setUnitIncrement(16);
        s.setPreferredSize(new Dimension(ancho, 0));
        s.setMinimumSize(new Dimension(ancho, 0));
        return s;
    }

    private static final class ColumnaFormulario extends JPanel implements javax.swing.Scrollable {

        ColumnaFormulario() {
            super(new BorderLayout());
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
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
    }

    /**
     * Agrega al encabezado del formulario el botón "Menú principal", que cierra la ventana
     * y regresa al menú. Si el encabezado ya tiene algo a la derecha, lo conserva al lado.
     */
    public static void agregarVolverAlMenu(JFrame ventana, Runnable volver) {
        PanelEncabezado h = buscarEncabezado(ventana.getContentPane());
        JButton b = new BotonVolver();
        b.setToolTipText("Cerrar este módulo y regresar al menú principal");
        b.addActionListener(e -> volver.run());
        if (h == null) {
            JPanel barra = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
            barra.setBackground(AZUL_OSCURO);
            barra.add(b);
            ventana.getContentPane().add(barra, BorderLayout.NORTH);
            return;
        }
        BorderLayout bl = (BorderLayout) h.getLayout();
        Component este = bl.getLayoutComponent(BorderLayout.EAST);
        JPanel derecha = new JPanel(new java.awt.GridBagLayout());
        derecha.setOpaque(false);
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.insets = new Insets(0, 10, 0, 0);
        if (este != null) {
            h.remove(este);
            derecha.add(este, g);
        }
        derecha.add(b, g);
        h.add(derecha, BorderLayout.EAST);
        h.revalidate();
    }

    private static PanelEncabezado buscarEncabezado(Container c) {
        for (Component x : c.getComponents()) {
            if (x instanceof PanelEncabezado p) {
                return p;
            }
            if (x instanceof Container cc) {
                PanelEncabezado p = buscarEncabezado(cc);
                if (p != null) {
                    return p;
                }
            }
        }
        return null;
    }

    /** Botón translúcido con flecha para regresar al menú. */
    private static final class BotonVolver extends JButton {

        BotonVolver() {
            super("Menú principal");
            setFont(fuente(Font.BOLD, 13f));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            FontMetrics fm = getFontMetrics(getFont());
            setPreferredSize(new Dimension(fm.stringWidth(getText()) + 52, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth();
            int h = getHeight();
            boolean encima = getModel().isRollover();
            boolean presionado = getModel().isPressed();
            g2.setColor(presionado ? new Color(255, 255, 255, 90) : encima ? Color.WHITE : new Color(255, 255, 255, 34));
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, h, h));
            g2.setColor(new Color(255, 255, 255, 110));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 2, h - 2, h, h));
            if (isFocusOwner() && !encima) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 3, h - 3, h, h));
            }
            Color tinta = encima && !presionado ? PRIMARIO : Color.WHITE;
            // flecha hacia la izquierda
            g2.setColor(tinta);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cy = h / 2;
            g2.drawLine(16, cy, 30, cy);
            g2.drawPolyline(new int[]{22, 16, 22}, new int[]{cy - 6, cy, cy + 6}, 3);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 38, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Encabezado de los formularios: degradado rojo con el ícono del sistema a la izquierda. */
    public static JPanel panelEncabezado() {
        return new PanelEncabezado(46);
    }

    public static JPanel panelEncabezado(int tamanoIcono) {
        return new PanelEncabezado(tamanoIcono);
    }

    private static final class PanelEncabezado extends JPanel {

        private final IconoTaller icono;
        private Border externo = BorderFactory.createEmptyBorder(15, 20, 15, 20);

        PanelEncabezado(int tamano) {
            super(new BorderLayout());
            icono = new IconoTaller(tamano, Color.WHITE);
            setOpaque(true);
            setBorder(externo);
        }

        // El formulario fija sus márgenes; se agrega a la izquierda el espacio del ícono
        @Override
        public void setBorder(Border b) {
            externo = b;
            super.setBorder(b == null ? null
                    : BorderFactory.createCompoundBorder(b, BorderFactory.createEmptyBorder(0, icono.getIconWidth() + 14, 0, 0)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintarDegradado(g, 0, 0, getWidth(), getHeight());
            Insets i = externo == null ? new Insets(0, 0, 0, 0) : externo.getBorderInsets(this);
            icono.paintIcon(this, g, i.left, (getHeight() - icono.getIconHeight()) / 2);
        }
    }

    public static class PanelDegradado extends JPanel {

        public PanelDegradado() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintarDegradado(g, 0, 0, getWidth(), getHeight());
        }
    }

    private static void pintarDegradado(Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        suavizar(g2);
        g2.translate(x, y);
        g2.setPaint(new GradientPaint(0, 0, AZUL_OSCURO, w, h * 0.3f + w * 0.6f, PRIMARIO_PRESIONADO));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(255, 255, 255, 18));
        float d = Math.min(w, h) * 1.4f;
        g2.fill(new Ellipse2D.Float(w - d * 0.7f, -d * 0.35f, d, d));
        g2.setColor(new Color(255, 255, 255, 14));
        g2.fill(new Ellipse2D.Float(-d * 0.3f, h - d * 0.45f, d, d));
        g2.dispose();
    }

    private static final class FondoDegradado extends AbstractBorder {

        private final Insets margen;

        FondoDegradado(Insets margen) {
            this.margen = margen;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            pintarDegradado(g, x, y, w, h);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return margen;
        }
    }

    private static final class BordeRedondeado extends AbstractBorder {

        private final Color color;
        private final int grosor;
        private final Insets margen;

        BordeRedondeado(Color color, int grosor, Insets margen) {
            this.color = color;
            this.grosor = grosor;
            this.margen = margen;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(grosor));
            float d = grosor / 2f;
            g2.draw(new RoundRectangle2D.Float(x + d, y + d, w - grosor, h - grosor, 12, 12));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return margen;
        }
    }

    public static final class BotonUI extends BasicButtonUI {

        private final Color fondo;
        private final Color hover;
        private final Color presionado;
        private final Color borde;
        private final Color texto;
        private final Color seleccionado;

        BotonUI(Color fondo, Color hover, Color presionado, Color borde, Color texto, Color seleccionado) {
            this.fondo = fondo;
            this.hover = hover;
            this.presionado = presionado;
            this.borde = borde;
            this.texto = texto;
            this.seleccionado = seleccionado;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel m = b.getModel();
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            boolean marcado = seleccionado != null && m.isSelected();
            Color color = !b.isEnabled() ? (fondo == null ? null : fondo == Color.WHITE ? FONDO_CLARO : DESHABILITADO)
                    : marcado ? seleccionado
                    : m.isPressed() ? presionado
                    : m.isRollover() ? hover : fondo;
            if (color != null) {
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 12, 12));
            }
            if (borde != null && !marcado) {
                g2.setColor(borde);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, c.getWidth() - 1, c.getHeight() - 1, 12, 12));
            }
            g2.dispose();
            super.paint(g, c);
        }

        @Override
        protected void paintText(Graphics g, AbstractButton b, Rectangle r, String text) {
            Graphics2D g2 = (Graphics2D) g;
            suavizar(g2);
            boolean marcado = seleccionado != null && b.getModel().isSelected();
            g2.setColor(marcado ? Color.WHITE : !b.isEnabled() && texto != Color.WHITE ? DESHABILITADO : texto);
            FontMetrics fm = g2.getFontMetrics();
            BasicGraphicsUtils.drawStringUnderlineCharAt(b, g2, text, -1, r.x, r.y + fm.getAscent());
        }

        @Override
        protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            Dimension d = super.getPreferredSize(c);
            return d == null ? null : new Dimension(d.width, Math.max(d.height, 34));
        }
    }

    /**
     * Ícono de AutoTech: vehículo de perfil con una llave que aprieta una tuerca.
     * Se dibuja con áreas (los huecos quedan transparentes), así se ve bien sobre
     * el degradado del encabezado y como ícono de la ventana.
     */
    private static final class IconoTaller implements Icon {

        private final int tamano;
        private final Color color;

        IconoTaller(int tamano, Color color) {
            this.tamano = tamano;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.translate(x, y);
            g2.scale(tamano / 100.0, tamano / 100.0);
            g2.setColor(color);
            g2.fill(vehiculo());
            g2.fill(llaveYTuerca());
            g2.dispose();
        }

        private static java.awt.geom.Area vehiculo() {
            java.awt.geom.Path2D.Float cuerpo = new java.awt.geom.Path2D.Float();
            cuerpo.moveTo(6, 76);
            cuerpo.lineTo(6, 64);
            cuerpo.quadTo(6, 57, 13, 56);
            cuerpo.lineTo(27, 54);
            cuerpo.lineTo(37, 41);
            cuerpo.quadTo(39, 39, 42, 39);
            cuerpo.lineTo(64, 39);
            cuerpo.quadTo(67, 39, 69, 41);
            cuerpo.lineTo(80, 54);
            cuerpo.lineTo(89, 56);
            cuerpo.quadTo(95, 57.5f, 95, 64);
            cuerpo.lineTo(95, 76);
            cuerpo.closePath();
            java.awt.geom.Area a = new java.awt.geom.Area(cuerpo);

            java.awt.geom.Path2D.Float v1 = new java.awt.geom.Path2D.Float();
            v1.moveTo(41, 44); v1.lineTo(51, 44); v1.lineTo(51, 53); v1.lineTo(33, 53); v1.closePath();
            java.awt.geom.Path2D.Float v2 = new java.awt.geom.Path2D.Float();
            v2.moveTo(55, 44); v2.lineTo(66, 44); v2.lineTo(74, 53); v2.lineTo(55, 53); v2.closePath();
            a.subtract(new java.awt.geom.Area(v1));
            a.subtract(new java.awt.geom.Area(v2));

            for (float cx : new float[]{26, 75}) {
                a.subtract(new java.awt.geom.Area(new Ellipse2D.Float(cx - 12.5f, 76 - 12.5f, 25, 25)));
                java.awt.geom.Area rueda = new java.awt.geom.Area(new Ellipse2D.Float(cx - 10, 76 - 10, 20, 20));
                rueda.subtract(new java.awt.geom.Area(new Ellipse2D.Float(cx - 5.5f, 76 - 5.5f, 11, 11)));
                rueda.add(new java.awt.geom.Area(new Ellipse2D.Float(cx - 2.5f, 76 - 2.5f, 5, 5)));
                a.add(rueda);
            }
            return a;
        }

        private static java.awt.geom.Area llaveYTuerca() {
            // Llave inclinada sobre el techo; su boca abierta sujeta una tuerca hexagonal
            double ang = Math.toRadians(-20);
            java.awt.geom.AffineTransform t = new java.awt.geom.AffineTransform();
            t.translate(48, 22);
            t.rotate(ang);

            java.awt.geom.Area llave = new java.awt.geom.Area(
                    new RoundRectangle2D.Float(-30, -3.6f, 46, 7.2f, 7, 7));
            // extremo de anillo
            llave.add(new java.awt.geom.Area(new Ellipse2D.Float(-38, -8, 16, 16)));
            llave.subtract(new java.awt.geom.Area(new Ellipse2D.Float(-33.5f, -3.5f, 7, 7)));
            // cabeza de boca abierta
            java.awt.geom.Area cabeza = new java.awt.geom.Area(new Ellipse2D.Float(12, -10.5f, 21, 21));
            cabeza.subtract(new java.awt.geom.Area(new java.awt.Rectangle.Float(22, -5.2f, 14, 10.4f)));
            llave.add(cabeza);

            // tuerca dentro de la boca, con un pequeño espacio
            java.awt.geom.Path2D.Float hex = new java.awt.geom.Path2D.Float();
            for (int i = 0; i < 6; i++) {
                double a = Math.toRadians(60 * i);
                float px = (float) (29 + 6.4 * Math.cos(a));
                float py = (float) (6.4 * Math.sin(a));
                if (i == 0) hex.moveTo(px, py); else hex.lineTo(px, py);
            }
            hex.closePath();
            java.awt.geom.Area tuerca = new java.awt.geom.Area(hex);
            java.awt.geom.Area holgura = new java.awt.geom.Area(new java.awt.BasicStroke(3f).createStrokedShape(hex));
            holgura.add(tuerca);
            llave.subtract(holgura);
            tuerca.subtract(new java.awt.geom.Area(new Ellipse2D.Float(26.4f, -2.6f, 5.2f, 5.2f)));
            llave.add(tuerca);

            llave.transform(t);
            return llave;
        }

        @Override
        public int getIconWidth() {
            return tamano;
        }

        @Override
        public int getIconHeight() {
            return tamano;
        }
    }
}
