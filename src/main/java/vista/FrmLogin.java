package vista;

import excepciones.AutoTechException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import modelo.Usuario;
import servicio.AutenticacionService;

/**
 * Inicio de sesión de AutoTech Manager. La verificación (consulta a la base y cálculo
 * de la huella de la contraseña) se hace en segundo plano para no congelar la ventana.
 */
public class FrmLogin extends JFrame {

    private static final String PREF_USUARIO = "ultimoUsuario";

    private final AutenticacionService autenticacion = new AutenticacionService();
    private final JTextField txtUsuario = EstiloUI.campo(new JTextField());
    private final JPasswordField txtContrasena = EstiloUI.campo(new JPasswordField());
    private final JCheckBox chkMostrar = EstiloUI.casilla(new JCheckBox("Mostrar contraseña"));
    private final JCheckBox chkRecordar = EstiloUI.casilla(new JCheckBox("Recordar usuario"));
    private final JLabel lblMayusculas = new JLabel(" ");
    private final JLabel lblError = EstiloUI.mensajeError(new JLabel(" "), 40);
    private final JButton btnIngresar = EstiloUI.botonPrimario("Ingresar");
    private final char ecoContrasena;

    public FrmLogin() {
        super("AutoTech Manager - Iniciar sesión");
        setIconImages(EstiloUI.iconosVentana());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(920, 560);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Color.WHITE);
        raiz.add(new PanelMarca(), BorderLayout.WEST);
        raiz.add(crearFormulario(), BorderLayout.CENTER);
        setContentPane(raiz);
        ecoContrasena = txtContrasena.getEchoChar();

        String recordado = leerUsuarioRecordado();
        if (!recordado.isEmpty()) {
            txtUsuario.setText(recordado);
            chkRecordar.setSelected(true);
        }
        SwingUtilities.invokeLater(() -> (recordado.isEmpty() ? txtUsuario : txtContrasena).requestFocusInWindow());
    }

    private JPanel crearFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(40, 64, 30, 64));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.anchor = GridBagConstraints.NORTHWEST;

        JLabel titulo = new JLabel("Bienvenido");
        titulo.setFont(EstiloUI.fuente(Font.BOLD, 28f));
        titulo.setForeground(EstiloUI.AZUL_OSCURO);
        JLabel sub = new JLabel("Inicie sesión para entrar al sistema del taller.");
        sub.setFont(EstiloUI.fuente(Font.PLAIN, 13.5f));
        sub.setForeground(EstiloUI.TEXTO_SUAVE);

        txtUsuario.setFont(EstiloUI.fuente(Font.PLAIN, 14f));
        txtContrasena.setFont(EstiloUI.fuente(Font.PLAIN, 14f));
        txtUsuario.setPreferredSize(new Dimension(300, 42));
        txtContrasena.setPreferredSize(new Dimension(300, 42));

        lblMayusculas.setFont(EstiloUI.fuente(Font.BOLD, 11.5f));
        lblMayusculas.setForeground(new Color(0xB45309));
        KeyAdapter mayus = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                revisarMayusculas();
            }
        };
        txtContrasena.addKeyListener(mayus);
        txtContrasena.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                revisarMayusculas();
            }
        });

        chkMostrar.addActionListener(e -> txtContrasena.setEchoChar(chkMostrar.isSelected() ? (char) 0 : ecoContrasena));
        JPanel opciones = new JPanel(new BorderLayout());
        opciones.setOpaque(false);
        opciones.add(chkMostrar, BorderLayout.WEST);
        opciones.add(chkRecordar, BorderLayout.EAST);

        btnIngresar.setPreferredSize(new Dimension(300, 44));
        btnIngresar.setFont(EstiloUI.fuente(Font.BOLD, 15f));
        btnIngresar.addActionListener(e -> ingresar());
        txtUsuario.addActionListener(e -> txtContrasena.requestFocusInWindow());
        txtContrasena.addActionListener(e -> ingresar());
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "salir");
        getRootPane().getActionMap().put("salir", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.exit(0);
            }
        });

        JLabel pie = new JLabel("Tiene " + AutenticacionService.MAXIMO_INTENTOS + " intentos disponibles para iniciar sesión.");
        pie.setFont(EstiloUI.fuente(Font.PLAIN, 11.5f));
        pie.setForeground(EstiloUI.TEXTO_SUAVE);

        int y = 0;
        g.gridy = y++;
        p.add(titulo, g);
        g.gridy = y++;
        g.insets = new Insets(2, 0, 26, 0);
        p.add(sub, g);
        g.insets = new Insets(0, 0, 6, 0);
        g.gridy = y++;
        p.add(EstiloUI.etiqueta(new JLabel("Usuario")), g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 16, 0);
        p.add(txtUsuario, g);
        g.insets = new Insets(0, 0, 6, 0);
        g.gridy = y++;
        p.add(EstiloUI.etiqueta(new JLabel("Contraseña")), g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 4, 0);
        p.add(txtContrasena, g);
        g.gridy = y++;
        p.add(lblMayusculas, g);
        g.gridy = y++;
        g.insets = new Insets(2, 0, 16, 0);
        p.add(opciones, g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 10, 0);
        p.add(btnIngresar, g);
        g.gridy = y++;
        p.add(lblError, g);
        g.gridy = y++;
        g.weighty = 1;
        g.anchor = GridBagConstraints.SOUTHWEST;
        g.insets = new Insets(0, 0, 0, 0);
        p.add(pie, g);
        return p;
    }

    private void revisarMayusculas() {
        boolean activo;
        try {
            activo = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        } catch (UnsupportedOperationException e) {
            activo = false;
        }
        lblMayusculas.setText(activo ? "Bloq Mayús está activado" : " ");
    }

    private void ingresar() {
        if (!btnIngresar.isEnabled()) {
            return;
        }
        String usuario = txtUsuario.getText();
        char[] contrasena = txtContrasena.getPassword();
        EstiloUI.mostrarError(lblError, null);
        habilitar(false);
        btnIngresar.setText("Verificando…");

        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                return autenticacion.iniciarSesion(usuario, contrasena);
            }

            @Override
            protected void done() {
                habilitar(true);
                btnIngresar.setText("Ingresar");
                try {
                    Usuario u = get();
                    guardarUsuarioRecordado(chkRecordar.isSelected() ? u.getNombreUsuario() : "");
                    abrirMenu();
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    String mensaje = causa instanceof AutoTechException ? causa.getMessage()
                            : "No se pudo conectar con la base de datos: " + causa.getMessage();
                    EstiloUI.mostrarError(lblError, mensaje);
                    txtContrasena.setText("");
                    txtContrasena.requestFocusInWindow();
                    sacudir();
                }
            }
        }.execute();
    }

    private void abrirMenu() {
        FrmMenuPrincipal menu = new FrmMenuPrincipal();
        menu.setVisible(true);
        dispose();
    }

    private void habilitar(boolean si) {
        btnIngresar.setEnabled(si);
        txtUsuario.setEnabled(si);
        txtContrasena.setEnabled(si);
        setCursor(si ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /** Pequeña sacudida de la ventana cuando el acceso es rechazado. */
    private void sacudir() {
        Point origen = getLocation();
        int[] pasos = {10, -10, 8, -8, 5, -5, 2, 0};
        int[] i = {0};
        Timer t = new Timer(28, null);
        t.addActionListener(e -> {
            if (i[0] >= pasos.length) {
                setLocation(origen);
                t.stop();
                return;
            }
            setLocation(origen.x + pasos[i[0]++], origen.y);
        });
        t.start();
    }

    private static String leerUsuarioRecordado() {
        try {
            return Preferences.userNodeForPackage(FrmLogin.class).get(PREF_USUARIO, "");
        } catch (Exception e) {
            return "";
        }
    }

    private static void guardarUsuarioRecordado(String usuario) {
        try {
            Preferences.userNodeForPackage(FrmLogin.class).put(PREF_USUARIO, usuario);
        } catch (Exception ignorada) {
            // solo es una comodidad
        }
    }

    /** Panel izquierdo con la marca del sistema. */
    private static final class PanelMarca extends JPanel {

        private final Icon icono = EstiloUI.iconoTaller(132, Color.WHITE);
        private final Icon marcaAgua = EstiloUI.iconoTaller(360, new Color(255, 255, 255, 16));

        PanelMarca() {
            setPreferredSize(new Dimension(390, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new LinearGradientPaint(0, 0, w, h, new float[]{0f, 0.6f, 1f},
                    new Color[]{new Color(0x1C0709), EstiloUI.PRIMARIO_PRESIONADO, EstiloUI.PRIMARIO}));
            g2.fillRect(0, 0, w, h);
            // líneas de velocidad
            g2.setStroke(new BasicStroke(2f));
            for (int i = 0; i < 8; i++) {
                g2.setColor(new Color(255, 255, 255, 9 + (i % 3) * 5));
                int x = -60 + i * 70;
                g2.drawLine(x, h, x + 200, 0);
            }
            marcaAgua.paintIcon(this, g2, w - 230, h - 300);

            // insignia con el ícono
            int s = 168;
            int ix = (w - s) / 2;
            int iy = 92;
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fill(new RoundRectangle2D.Float(ix, iy, s, s, 36, 36));
            g2.setColor(new Color(255, 255, 255, 70));
            g2.draw(new RoundRectangle2D.Float(ix + 0.5f, iy + 0.5f, s - 1, s - 1, 36, 36));
            icono.paintIcon(this, g2, ix + (s - icono.getIconWidth()) / 2, iy + (s - icono.getIconHeight()) / 2);

            centrar(g2, "AutoTech Manager", EstiloUI.fuente(Font.BOLD, 28f), Color.WHITE, iy + s + 50);
            centrar(g2, "AutoTech Motors S. de R.L.", EstiloUI.fuente(Font.PLAIN, 14f), EstiloUI.TEXTO_ENCABEZADO, iy + s + 76);
            centrar(g2, "Gestión y control de servicios automotrices", EstiloUI.fuente(Font.PLAIN, 12.5f),
                    new Color(255, 255, 255, 170), iy + s + 98);
            centrar(g2, "Programación Avanzada II  ·  Segundo Parcial", EstiloUI.fuente(Font.PLAIN, 11.5f),
                    new Color(255, 255, 255, 140), h - 34);

            // franja a cuadros
            int c = 7;
            for (int x = 0, i = 0; x < w; x += c, i++) {
                for (int fila = 0; fila < 2; fila++) {
                    g2.setColor((i + fila) % 2 == 0 ? new Color(255, 255, 255, 50) : new Color(0, 0, 0, 45));
                    g2.fillRect(x, h - c * (2 - fila), c, c);
                }
            }
            g2.dispose();
        }

        private void centrar(Graphics2D g2, String texto, Font f, Color color, int y) {
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(color);
            g2.drawString(texto, (getWidth() - fm.stringWidth(texto)) / 2, y);
        }
    }
}
