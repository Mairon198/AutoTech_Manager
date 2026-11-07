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

    public static final Color PRIMARIO = new Color(0x0F766E);
    public static final Color PRIMARIO_HOVER = new Color(0x0D9488);
    public static final Color PRIMARIO_PRESIONADO = new Color(0x115E59);
    public static final Color AZUL_OSCURO = new Color(0x0B2545);
    public static final Color TEXTO = new Color(0x1F2937);
    public static final Color TEXTO_SUAVE = new Color(0x6B7280);
    public static final Color BORDE = new Color(0xD1D5DB);
    public static final Color FONDO_CLARO = new Color(0xF3F4F6);
    public static final Color VERDE_CLARO = new Color(0xCCFBF1);
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
        return boton(new JButton(texto), new BotonUI(ERROR, new Color(0xDC2626), new Color(0x991B1B), null, Color.WHITE, null), 13f);
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
                    l.setForeground(VERDE_CLARO);
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

    public static Icon iconoGafas(int ancho, Color color) {
        return new IconoTaller(ancho, color);
    }

    public static Icon iconoTaller(int ancho, Color color) {
        return new IconoTaller(ancho, color);
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
        g2.setPaint(new GradientPaint(0, 0, AZUL_OSCURO, w, h, PRIMARIO));
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

    private static final class IconoTaller implements Icon {

        private final int ancho;
        private final Color color;

        IconoTaller(int ancho, Color color) {
            this.ancho = ancho;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.translate(x, y);
            int h = getIconHeight();
            float trazo = Math.max(2.6f, ancho / 24f);
            g2.setStroke(new BasicStroke(trazo, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(color);

            float wCar = ancho * 0.86f;
            float xCar = (ancho - wCar) / 2f;
            float yBase = h * 0.65f;
            g2.draw(new RoundRectangle2D.Float(xCar, yBase - h * 0.22f, wCar, h * 0.24f, 10, 10));
            int[] px = {
                (int) (xCar + wCar * 0.18f),
                (int) (xCar + wCar * 0.32f),
                (int) (xCar + wCar * 0.68f),
                (int) (xCar + wCar * 0.84f)
            };
            int[] py = {
                (int) (yBase - h * 0.22f),
                (int) (yBase - h * 0.48f),
                (int) (yBase - h * 0.48f),
                (int) (yBase - h * 0.22f)
            };
            g2.drawPolyline(px, py, 4);

            float rRueda = h * 0.16f;
            g2.setColor(AZUL_OSCURO);
            g2.fill(new Ellipse2D.Float(xCar + wCar * 0.14f, yBase - rRueda * 0.35f, rRueda * 2, rRueda * 2));
            g2.fill(new Ellipse2D.Float(xCar + wCar * 0.86f - rRueda * 2, yBase - rRueda * 0.35f, rRueda * 2, rRueda * 2));
            g2.setColor(color);
            g2.draw(new Ellipse2D.Float(xCar + wCar * 0.14f, yBase - rRueda * 0.35f, rRueda * 2, rRueda * 2));
            g2.draw(new Ellipse2D.Float(xCar + wCar * 0.86f - rRueda * 2, yBase - rRueda * 0.35f, rRueda * 2, rRueda * 2));
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return ancho;
        }

        @Override
        public int getIconHeight() {
            return (int) (ancho * 0.56f);
        }
    }
}
