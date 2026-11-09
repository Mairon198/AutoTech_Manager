package vista;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import modelo.OrdenResumen;
import modelo.enums.EstadoOrden;
import servicio.TableroService;
import servicio.TableroService.Tablero;

/**
 * Menú principal de AutoTech Manager.
 *
 * Además de abrir los módulos, funciona como tablero del taller: indicadores que se
 * actualizan solos (en segundo plano, sin congelar la ventana), el flujo de órdenes
 * por estado, las órdenes recientes y los módulos abiertos últimamente. Los módulos
 * se buscan escribiendo (Ctrl+K), se filtran por sección y se recorren con las flechas.
 */
public class FrmMenuPrincipal extends JFrame {

    private static final Color FONDO = EstiloUI.FONDO_CLARO;
    private static final Color VERDE = EstiloUI.EXITO;
    private static final Color ROJO = EstiloUI.ERROR;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("hh:mm a", Locale.of("es", "HN"));

    /** Un módulo del menú. */
    private record Modulo(String seccion, String titulo, String clase, String descripcion, String sigla, int tecla) {
    }

    private static final Map<String, Color> COLOR_SECCION = new LinkedHashMap<>();
    private static final List<Modulo> MODULOS = new ArrayList<>();

    static {
        seccion("Clientes y vehículos", new Color(0xC62828),
                new Modulo(null, "Clientes", "FrmClientes", "Registrar, modificar y desactivar clientes", "CL", KeyEvent.VK_1),
                new Modulo(null, "Vehículos", "FrmVehiculos", "Placa, VIN, marca, modelo y kilometraje", "VH", KeyEvent.VK_2));
        seccion("Taller", new Color(0xD84315),
                new Modulo(null, "Órdenes de trabajo", "FrmOrdenTrabajo", "Ingreso, servicios, repuestos y estados", "OT", KeyEvent.VK_3),
                new Modulo(null, "Diagnóstico y presupuesto", "FrmDiagnosticoPresupuesto", "Evaluación técnica y cotización", "DP", KeyEvent.VK_4),
                new Modulo(null, "Asignación y actividades", "FrmAsignacionActividades", "Mecánicos por orden y avance", "AS", KeyEvent.VK_5),
                new Modulo(null, "Control de calidad", "FrmControlCalidad", "Verificación y cierre con factura", "QC", KeyEvent.VK_6),
                new Modulo(null, "Cambio de motor", "FrmCambioMotor", "Sustitución de motor (caso Hilux)", "MT", KeyEvent.VK_7));
        seccion("Catálogo, personal e inventario", new Color(0x6D4C41),
                new Modulo(null, "Servicios", "FrmServicios", "Catálogo por nivel de complejidad", "SV", 0),
                new Modulo(null, "Mecánicos", "FrmMecanicos", "Personal, especialidades y disponibilidad", "MC", 0),
                new Modulo(null, "Inventario", "FrmInventario", "Repuestos, entradas, salidas y stock mínimo", "IN", 0),
                new Modulo(null, "Proveedores", "FrmProveedores", "Suministradores de repuestos y motores", "PR", 0));
        seccion("Caja y consultas", new Color(0x37474F),
                new Modulo(null, "Pagos y facturación", "FrmPagosFacturacion", "Abonos, saldo, factura y entrega", "PF", KeyEvent.VK_8),
                new Modulo(null, "Historial de vehículo", "FrmHistorialVehiculo", "Servicios, mecánicos, repuestos y costos", "HV", KeyEvent.VK_9),
                new Modulo(null, "Reportes", "FrmReportes", "Vistas SQL y reportes administrativos", "RP", 0));
    }

    private static void seccion(String nombre, Color color, Modulo... modulos) {
        COLOR_SECCION.put(nombre, color);
        for (Modulo m : modulos) {
            MODULOS.add(new Modulo(nombre, m.titulo, m.clase, m.descripcion, m.sigla, m.tecla));
        }
    }

    private final TableroService tablero = new TableroService();
    private Tablero datos;
    private boolean cargando;

    private final JTextField txtBuscar = new CampoBusqueda("Buscar módulo…   Ctrl+K");
    private String seccionActiva;
    private final List<Tarjeta> tarjetas = new ArrayList<>();
    private final List<Tarjeta> visibles = new ArrayList<>();
    private final JPanel grilla = new JPanel(new GridLayout(0, 4, 14, 14));
    private final JLabel lblSinResultados = new JLabel(" ");

    private final Indicador kpiAbiertas = new Indicador("Órdenes abiertas", "Ver órdenes", "FrmOrdenTrabajo", null);
    private final Indicador kpiAprobar = new Indicador("Por aprobar", "Ir a presupuestos", "FrmDiagnosticoPresupuesto", EstadoOrden.PENDIENTE_APROBACION);
    private final Indicador kpiCalidad = new Indicador("En control calidad", "Revisar", "FrmControlCalidad", EstadoOrden.EN_CONTROL_CALIDAD);
    private final Indicador kpiStock = new Indicador("Stock bajo mínimo", "Ver inventario", "FrmInventario", null);
    private final Indicador kpiMecanicos = new Indicador("Mecánicos libres", "Ver personal", "FrmMecanicos", null);
    private final Indicador kpiCobrar = new Indicador("Saldo por cobrar", "Ir a caja", "FrmPagosFacturacion", null);
    private final FlujoOrdenes flujo = new FlujoOrdenes(this::abrirEstado);

    private final JLabel lblSaludo = new JLabel();
    private final JLabel lblActualizado = new JLabel(" ");
    private final JPanel pnlRecientes = new JPanel();
    private final JPanel pnlAbiertos = new JPanel();
    private final JLabel lblConexion = new JLabel("Conectando con la base de datos...");
    private final JLabel lblReloj = new JLabel();
    private final JButton btnActualizar = EstiloUI.enlace(new JButton("Actualizar"));
    private final IconoRecargar iconoRecargar = new IconoRecargar(15, EstiloUI.PRIMARIO);
    private final Timer giro = new Timer(16, e -> {
        iconoRecargar.girar(9);
        btnActualizar.repaint();
    });
    private final Timer refresco = new Timer(60_000, e -> cargarTablero());
    private Timer reloj;
    private final JLabel lblSesion = new JLabel();
    private final JButton btnReintentar = EstiloUI.enlace(new JButton("Reintentar"));
    private final Set<Window> enganchadas = Collections.newSetFromMap(new WeakHashMap<>());

    public FrmMenuPrincipal() {
        super("AutoTech Manager - Menú principal");
        setIconImages(EstiloUI.iconosVentana());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        setMinimumSize(new Dimension(1100, 700));
        setSize(1320, 820);
        setLocationRelativeTo(null);

        setJMenuBar(crearBarraMenu());
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(crearCuerpo(), BorderLayout.CENTER);
        raiz.add(crearPanelLateral(), BorderLayout.EAST);
        raiz.add(crearBarraEstado(), BorderLayout.SOUTH);
        setContentPane(raiz);

        atajosDeTeclado();
        filtrar();
        mostrarAbiertosRecientemente();
        iniciarReloj();
        cargarTablero();
        // se actualiza solo cada minuto
        refresco.start();
    }

    // ================================================================== estructura

    private JMenuBar crearBarraMenu() {
        JMenuBar barra = new BarraMenu();
        JMenu archivo = menu("Archivo");
        JMenuItem actualizar = item("Actualizar tablero", new IconoRecargar(14, EstiloUI.PRIMARIO));
        actualizar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        actualizar.addActionListener(e -> cargarTablero());
        JMenuItem cerrarSesion = item("Cerrar sesión", new IconoSalir(14, EstiloUI.PRIMARIO));
        cerrarSesion.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        cerrarSesion.addActionListener(e -> cerrarSesion());
        JMenuItem salir = item("Salir", new IconoSalir(14, EstiloUI.TEXTO_SUAVE));
        salir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        salir.addActionListener(e -> salir());
        archivo.add(actualizar);
        archivo.add(separador());
        archivo.add(cerrarSesion);
        archivo.add(salir);
        barra.add(archivo);

        for (String sec : COLOR_SECCION.keySet()) {
            JMenu menu = menu(sec);
            for (Modulo m : MODULOS) {
                if (!m.seccion.equals(sec)) {
                    continue;
                }
                JMenuItem item = item(m.titulo, new IconoSigla(m.sigla, COLOR_SECCION.get(sec)));
                if (m.tecla != 0) {
                    item.setAccelerator(KeyStroke.getKeyStroke(m.tecla, InputEvent.CTRL_DOWN_MASK));
                }
                item.addActionListener(e -> abrir(m, null));
                menu.add(item);
            }
            barra.add(menu);
        }

        JMenu ayuda = menu("Ayuda");
        JMenuItem atajos = item("Atajos de teclado", new IconoSigla("?", EstiloUI.TEXTO_SUAVE));
        atajos.addActionListener(e -> mostrarAtajos());
        JMenuItem acerca = item("Acerca de AutoTech Manager", new IconoSigla("i", EstiloUI.PRIMARIO));
        acerca.addActionListener(e -> mostrarAcercaDe());
        ayuda.add(atajos);
        ayuda.add(acerca);
        barra.add(ayuda);
        return barra;
    }

    /** Menú de la barra superior con los colores del sistema. */
    private static JMenu menu(String texto) {
        JMenu m = new JMenu(texto);
        m.setUI(new MenuSuperiorUI());
        m.setFont(EstiloUI.fuente(Font.BOLD, 13f));
        m.setForeground(Color.WHITE);
        m.setOpaque(false);
        m.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        m.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        m.getPopupMenu().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloUI.BORDE),
                BorderFactory.createEmptyBorder(4, 0, 4, 0)));
        m.getPopupMenu().setBackground(Color.WHITE);
        // resaltado al pasar el mouse, aunque el menú no esté abierto
        m.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                m.putClientProperty("encima", true);
                m.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                m.putClientProperty("encima", false);
                m.repaint();
            }
        });
        return m;
    }

    private static JMenuItem item(String texto, javax.swing.Icon icono) {
        JMenuItem i = new JMenuItem(texto, icono);
        i.setUI(new ElementoMenuUI());
        i.setFont(EstiloUI.fuente(Font.PLAIN, 13f));
        i.setForeground(EstiloUI.TEXTO);
        i.setBackground(Color.WHITE);
        i.setOpaque(true);
        i.setIconTextGap(10);
        i.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 16));
        return i;
    }

    private static javax.swing.JSeparator separador() {
        javax.swing.JSeparator s = new javax.swing.JPopupMenu.Separator();
        s.setForeground(EstiloUI.BORDE);
        s.setBackground(Color.WHITE);
        return s;
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = EstiloUI.panelEncabezado(60);
        encabezado.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel titulo = new JLabel("AutoTech Manager");
        titulo.setFont(EstiloUI.fuente(Font.BOLD, 26f));
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("AutoTech Motors S. de R.L.  ·  Gestión y control de servicios automotrices");
        subtitulo.setFont(EstiloUI.fuente(Font.PLAIN, 14f));
        subtitulo.setForeground(EstiloUI.TEXTO_ENCABEZADO);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(subtitulo);
        encabezado.add(textos, BorderLayout.WEST);

        txtBuscar.setPreferredSize(new Dimension(320, 38));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
        });
        txtBuscar.addActionListener(e -> {
            if (!visibles.isEmpty()) {
                visibles.get(0).doClick();
            }
        });
        txtBuscar.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "limpiar");
        txtBuscar.getActionMap().put("limpiar", accion(() -> txtBuscar.setText("")));
        txtBuscar.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "bajar");
        txtBuscar.getActionMap().put("bajar", accion(() -> {
            if (!visibles.isEmpty()) {
                visibles.get(0).requestFocusInWindow();
            }
        }));
        BotonSalir btnSalir = new BotonSalir();
        btnSalir.setToolTipText("Cerrar AutoTech Manager");
        btnSalir.addActionListener(e -> salir());
        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setOpaque(false);
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        derecha.add(txtBuscar, g);
        g.insets = new java.awt.Insets(0, 12, 0, 0);
        derecha.add(btnSalir, g);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    private JScrollPane crearCuerpo() {
        JPanel cuerpo = new PanelAncho();
        cuerpo.setBackground(FONDO);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 20));

        lblSaludo.setFont(EstiloUI.fuente(Font.BOLD, 18f));
        lblSaludo.setForeground(EstiloUI.TEXTO);
        lblActualizado.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblActualizado.setForeground(EstiloUI.TEXTO_SUAVE);
        btnActualizar.setIcon(iconoRecargar);
        btnActualizar.setIconTextGap(6);
        btnActualizar.setToolTipText("Actualizar el tablero (F5)");
        btnActualizar.addActionListener(e -> cargarTablero());
        JPanel cabecera = fila(lblSaludo, Box.createHorizontalGlue(), lblActualizado, btnActualizar);
        cuerpo.add(cabecera);
        cuerpo.add(Box.createVerticalStrut(12));

        JPanel kpis = new JPanel(new GridLayout(1, 6, 12, 0));
        kpis.setOpaque(false);
        for (Indicador k : List.of(kpiAbiertas, kpiAprobar, kpiCalidad, kpiStock, kpiMecanicos, kpiCobrar)) {
            k.addActionListener(e -> abrirDesdeIndicador(k));
            kpis.add(k);
        }
        cuerpo.add(alinear(kpis, 104));
        cuerpo.add(Box.createVerticalStrut(20));

        cuerpo.add(titulo("Flujo de órdenes", "clic en una etapa para abrir la primera orden que está en ella"));
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(alinear(flujo, 74));
        cuerpo.add(Box.createVerticalStrut(20));

        cuerpo.add(titulo("Módulos", "flechas para moverse, Enter para abrir"));
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(crearFiltros());
        cuerpo.add(Box.createVerticalStrut(12));

        for (Modulo m : MODULOS) {
            Tarjeta t = new Tarjeta(m, COLOR_SECCION.get(m.seccion), Navegador.existe(m.clase));
            t.addActionListener(e -> abrir(m, null));
            navegacionConFlechas(t);
            tarjetas.add(t);
        }
        grilla.setOpaque(false);
        grilla.setAlignmentX(LEFT_ALIGNMENT);
        cuerpo.add(grilla);
        lblSinResultados.setFont(EstiloUI.fuente(Font.PLAIN, 13f));
        lblSinResultados.setForeground(EstiloUI.TEXTO_SUAVE);
        lblSinResultados.setAlignmentX(LEFT_ALIGNMENT);
        cuerpo.add(lblSinResultados);
        cuerpo.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(cuerpo);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getViewport().setBackground(FONDO);
        return scroll;
    }

    private JPanel crearFiltros() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        ButtonGroup grupo = new ButtonGroup();
        List<String> opciones = new ArrayList<>();
        opciones.add(null);
        opciones.addAll(COLOR_SECCION.keySet());
        for (String sec : opciones) {
            JToggleButton b = EstiloUI.segmento(new JToggleButton(sec == null ? "Todos" : sec));
            b.setFont(EstiloUI.fuente(Font.BOLD, 12.5f));
            b.setPreferredSize(null);
            b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
            b.setSelected(sec == null);
            b.addActionListener(e -> {
                seccionActiva = sec;
                filtrar();
            });
            grupo.add(b);
            p.add(b);
        }
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return p;
    }

    private JPanel crearPanelLateral() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, EstiloUI.BORDE),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        p.setPreferredSize(new Dimension(310, 0));

        p.add(titulo("Órdenes recientes", null));
        p.add(Box.createVerticalStrut(6));
        pnlRecientes.setLayout(new BoxLayout(pnlRecientes, BoxLayout.Y_AXIS));
        pnlRecientes.setOpaque(false);
        pnlRecientes.setAlignmentX(LEFT_ALIGNMENT);
        p.add(pnlRecientes);
        JButton todas = EstiloUI.enlace(new JButton("Ver todas las órdenes →"));
        todas.setAlignmentX(LEFT_ALIGNMENT);
        todas.addActionListener(e -> abrir(modulo("FrmOrdenTrabajo"), null));
        p.add(todas);
        p.add(Box.createVerticalStrut(18));

        p.add(titulo("Abiertos recientemente", null));
        p.add(Box.createVerticalStrut(6));
        pnlAbiertos.setLayout(new BoxLayout(pnlAbiertos, BoxLayout.Y_AXIS));
        pnlAbiertos.setOpaque(false);
        pnlAbiertos.setAlignmentX(LEFT_ALIGNMENT);
        p.add(pnlAbiertos);
        p.add(Box.createVerticalGlue());

        JLabel atajos = new JLabel("<html><div style='width:250px;color:#6B7280'>"
                + "<b style='color:#374151'>Atajos</b><br>"
                + "Ctrl+K buscar módulo<br>"
                + "F5 actualizar · Esc limpiar búsqueda</div></html>");
        atajos.setFont(EstiloUI.fuente(Font.PLAIN, 11.5f));
        atajos.setAlignmentX(LEFT_ALIGNMENT);
        p.add(atajos);
        return p;
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.BORDE),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)));
        lblConexion.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblConexion.setForeground(EstiloUI.TEXTO_SUAVE);
        JButton reintentar = btnReintentar;
        reintentar.setVisible(false);
        reintentar.setFont(EstiloUI.fuente(Font.BOLD, 12f));
        reintentar.addActionListener(e -> cargarTablero());
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izq.setOpaque(false);
        izq.add(lblConexion);
        izq.add(reintentar);
        lblReloj.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblReloj.setForeground(EstiloUI.TEXTO_SUAVE);
        barra.add(izq, BorderLayout.WEST);
        lblSesion.setFont(EstiloUI.fuente(Font.BOLD, 12f));
        lblSesion.setForeground(EstiloUI.AZUL_OSCURO);
        modelo.Usuario u = util.Sesion.actual();
        lblSesion.setText(u == null ? "" : "Sesión: " + u.getNombreCompleto() + " (" + u.getRol() + ")     ");
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        der.setOpaque(false);
        der.add(lblSesion);
        der.add(lblReloj);
        barra.add(der, BorderLayout.EAST);
        return barra;
    }

    // ================================================================== datos del tablero

    /** Lee el tablero en segundo plano (SwingWorker) para no congelar la ventana. */
    private void cargarTablero() {
        if (cargando) {
            return;
        }
        cargando = true;
        giro.start();
        lblActualizado.setText("Actualizando…");
        new SwingWorker<Tablero, Void>() {
            @Override
            protected Tablero doInBackground() throws Exception {
                return tablero.cargar();
            }

            @Override
            protected void done() {
                cargando = false;
                giro.stop();
                iconoRecargar.reiniciar();
                btnActualizar.repaint();
                try {
                    mostrarTablero(get());
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    sinConexion(String.valueOf(causa.getMessage()));
                }
            }
        }.execute();
    }

    private void mostrarTablero(Tablero t) {
        datos = t;
        lblConexion.setForeground(VERDE);
        lblConexion.setText("● Conectado a " + t.baseDatos());
        lblConexion.setToolTipText(null);
        btnReintentar.setVisible(false);
        lblActualizado.setText("Actualizado a las " + LocalTime.now().format(HORA) + "  ·");

        kpiAbiertas.mostrar(t.abiertas(), null, t.cantidad(EstadoOrden.EN_REPARACION) + " en reparación");
        kpiAprobar.mostrar(t.cantidad(EstadoOrden.PENDIENTE_APROBACION), null,
                t.cantidad(EstadoOrden.EN_DIAGNOSTICO) + " en diagnóstico");
        kpiCalidad.mostrar(t.cantidad(EstadoOrden.EN_CONTROL_CALIDAD), null, "por revisar y cerrar");
        kpiStock.mostrar(t.stockBajo(), null, t.stockBajo() > 0 ? "por reabastecer" : "inventario al día");
        kpiStock.setAlerta(t.stockBajo() > 0);
        kpiMecanicos.mostrar(t.mecanicosDisponibles(), null, "activos y disponibles");
        kpiCobrar.mostrar(t.porCobrar().doubleValue(), "L ", t.ordenesPorCobrar() + (t.ordenesPorCobrar() == 1 ? " orden con saldo" : " órdenes con saldo"));
        kpiCobrar.setAlerta(t.porCobrar().signum() > 0);

        flujo.setCantidades(t.porEstado());

        insignia("FrmClientes", t.clientes() + " activos", false);
        insignia("FrmVehiculos", t.vehiculos() + " registrados", false);
        insignia("FrmOrdenTrabajo", t.abiertas() + " abiertas", false);
        insignia("FrmDiagnosticoPresupuesto", contar(t.cantidad(EstadoOrden.PENDIENTE_APROBACION), "por aprobar"), true);
        insignia("FrmAsignacionActividades", contar(t.cantidad(EstadoOrden.EN_REPARACION), "en reparación"), false);
        insignia("FrmControlCalidad", contar(t.cantidad(EstadoOrden.EN_CONTROL_CALIDAD), "por revisar"), true);
        long motor = t.ordenes().stream().filter(o -> o.isRequiereCambioMotor() && !o.estaCerrada()).count();
        insignia("FrmCambioMotor", contar((int) motor, motor == 1 ? "caso abierto" : "casos abiertos"), false);
        insignia("FrmMecanicos", t.mecanicosDisponibles() + " disponibles", false);
        insignia("FrmInventario", contar(t.stockBajo(), "bajo mínimo"), true);
        insignia("FrmPagosFacturacion", contar(t.ordenesPorCobrar(), "por cobrar"), true);

        pnlRecientes.removeAll();
        if (t.ordenes().isEmpty()) {
            pnlRecientes.add(nota("Todavía no hay órdenes registradas."));
        }
        for (OrdenResumen o : t.ordenes().subList(0, Math.min(6, t.ordenes().size()))) {
            Fila f = new Fila(String.format("<html><b>%s</b> &nbsp;<span style='color:%s'>● %s</span><br>"
                    + "<span style='color:#6B7280'>%s · %s</span></html>",
                    o.getNumero(), colorHex(colorEstado(o.getEstado())), o.getEstado(), o.getPlaca(), recortar(o.getVehiculo(), 26)));
            f.setToolTipText(o.getCliente() + " · Total L " + String.format("%,.2f", o.getTotal()));
            f.addActionListener(e -> abrir(modulo(o.estaCerrada() ? "FrmPagosFacturacion" : "FrmOrdenTrabajo"), o.getOrdenId()));
            pnlRecientes.add(f);
        }
        pnlRecientes.revalidate();
        pnlRecientes.repaint();
    }

    private void sinConexion(String detalle) {
        lblConexion.setForeground(ROJO);
        btnReintentar.setVisible(true);
        lblConexion.setText("● Sin conexión: " + recortar(detalle, 70));
        lblConexion.setToolTipText("<html><div style='width:400px'>" + detalle + "</div></html>");
        lblActualizado.setText("Sin datos  ·");
        for (Indicador k : List.of(kpiAbiertas, kpiAprobar, kpiCalidad, kpiStock, kpiMecanicos, kpiCobrar)) {
            k.sinDatos();
        }
        if (datos == null) {
            pnlRecientes.removeAll();
            pnlRecientes.add(nota("Sin conexión con la base de datos."));
            pnlRecientes.revalidate();
        }
    }

    private static String contar(int n, String texto) {
        return n == 0 ? null : n + " " + texto;
    }

    private void insignia(String clase, String texto, boolean alerta) {
        for (Tarjeta t : tarjetas) {
            if (t.modulo.clase.equals(clase)) {
                t.setInsignia(texto, alerta);
            }
        }
    }

    // ================================================================== navegación

    private void abrir(Modulo m, Integer ordenId) {
        if (m == null) {
            return;
        }
        Window w = Navegador.abrir(this, m.clase, m.titulo);
        if (w == null) {
            return;
        }
        if (ordenId != null && w instanceof FrmBaseOrden f) {
            f.mostrarOrden(ordenId);
        }
        if (enganchadas.add(w)) {
            // al cerrar el módulo, el tablero se actualiza con lo que se hizo
            w.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    cargarTablero();
                }
            });
        }
        registrarAbierto(m);
    }

    private void abrirEstado(EstadoOrden estado) {
        OrdenResumen o = datos == null ? null : datos.primeraEn(estado);
        String clase = switch (estado) {
            case EN_DIAGNOSTICO, PENDIENTE_APROBACION -> "FrmDiagnosticoPresupuesto";
            case EN_CONTROL_CALIDAD -> "FrmControlCalidad";
            case FINALIZADA, ENTREGADA -> "FrmPagosFacturacion";
            default -> "FrmOrdenTrabajo";
        };
        abrir(modulo(clase), o == null ? null : o.getOrdenId());
    }

    private void abrirDesdeIndicador(Indicador k) {
        OrdenResumen o = datos == null || k.estado == null ? null : datos.primeraEn(k.estado);
        abrir(modulo(k.clase), o == null ? null : o.getOrdenId());
    }

    private static Modulo modulo(String clase) {
        return MODULOS.stream().filter(m -> m.clase.equals(clase)).findFirst().orElse(null);
    }

    /** Filtra las tarjetas por texto (sin importar tildes) y por sección. */
    private void filtrar() {
        String q = normalizar(txtBuscar.getText());
        visibles.clear();
        grilla.removeAll();
        for (Tarjeta t : tarjetas) {
            Modulo m = t.modulo;
            boolean seccionOk = seccionActiva == null || seccionActiva.equals(m.seccion);
            boolean textoOk = q.isEmpty() || normalizar(m.titulo + " " + m.descripcion + " " + m.seccion + " " + m.sigla).contains(q);
            if (seccionOk && textoOk) {
                visibles.add(t);
                grilla.add(t);
            }
        }
        lblSinResultados.setText(visibles.isEmpty() ? "Ningún módulo coincide con “" + txtBuscar.getText().trim() + "”." : " ");
        lblSinResultados.setVisible(visibles.isEmpty());
        grilla.revalidate();
        grilla.repaint();
    }

    private static String normalizar(String s) {
        return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private void navegacionConFlechas(Tarjeta t) {
        int[][] teclas = {{KeyEvent.VK_LEFT, -1}, {KeyEvent.VK_RIGHT, 1}, {KeyEvent.VK_UP, -4}, {KeyEvent.VK_DOWN, 4}};
        for (int[] k : teclas) {
            String nombre = "mover" + k[1];
            t.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(k[0], 0), nombre);
            t.getActionMap().put(nombre, accion(() -> {
                int i = visibles.indexOf(t) + k[1];
                if (i >= 0 && i < visibles.size()) {
                    visibles.get(i).requestFocusInWindow();
                    visibles.get(i).scrollRectToVisible(new java.awt.Rectangle(0, 0, t.getWidth(), t.getHeight()));
                } else if (k[1] < 0 && i < 0) {
                    txtBuscar.requestFocusInWindow();
                }
            }));
        }
        t.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "abrir");
        t.getActionMap().put("abrir", accion(t::doClick));
    }

    private void atajosDeTeclado() {
        JComponent raiz = getRootPane();
        raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK), "buscar");
        raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "buscar");
        raiz.getActionMap().put("buscar", accion(() -> {
            txtBuscar.requestFocusInWindow();
            txtBuscar.selectAll();
        }));
    }

    // ------------------------------------------------------------------ abiertos recientemente

    private static final String PREF_ABIERTOS = "modulosRecientes";

    private void registrarAbierto(Modulo m) {
        List<String> lista = leerAbiertos();
        lista.remove(m.clase);
        lista.add(0, m.clase);
        while (lista.size() > 5) {
            lista.remove(lista.size() - 1);
        }
        try {
            Preferences.userNodeForPackage(FrmMenuPrincipal.class).put(PREF_ABIERTOS, String.join(",", lista));
        } catch (Exception ignorada) {
            // las preferencias son una comodidad: si no se pueden guardar, el menú sigue igual
        }
        mostrarAbiertosRecientemente();
    }

    private static List<String> leerAbiertos() {
        List<String> lista = new ArrayList<>();
        try {
            String v = Preferences.userNodeForPackage(FrmMenuPrincipal.class).get(PREF_ABIERTOS, "");
            for (String s : v.split(",")) {
                if (!s.isBlank() && modulo(s) != null) {
                    lista.add(s);
                }
            }
        } catch (Exception ignorada) {
            // sin preferencias guardadas
        }
        return lista;
    }

    private void mostrarAbiertosRecientemente() {
        pnlAbiertos.removeAll();
        List<String> lista = leerAbiertos();
        if (lista.isEmpty()) {
            pnlAbiertos.add(nota("Los módulos que abra aparecerán aquí."));
        }
        for (String clase : lista) {
            Modulo m = modulo(clase);
            Fila f = new Fila("<html><b>" + m.titulo + "</b><br><span style='color:#6B7280'>" + m.seccion + "</span></html>");
            f.setMarca(COLOR_SECCION.get(m.seccion), m.sigla);
            f.addActionListener(e -> abrir(m, null));
            pnlAbiertos.add(f);
        }
        pnlAbiertos.revalidate();
        pnlAbiertos.repaint();
    }

    // ------------------------------------------------------------------ varios

    private void iniciarReloj() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy, hh:mm a", Locale.of("es", "HN"));
        reloj = new Timer(1000, e -> {
            LocalDateTime ahora = LocalDateTime.now();
            lblReloj.setText(ahora.format(formato));
            int h = ahora.getHour();
            String nombre = util.Sesion.activa() ? ", " + util.Sesion.actual().getNombreCompleto() : "";
            lblSaludo.setText((h < 12 ? "Buenos días" : h < 19 ? "Buenas tardes" : "Buenas noches")
                    + nombre + ". Así va el taller hoy");
        });
        reloj.setInitialDelay(0);
        reloj.start();
    }

    private void mostrarAtajos() {
        JOptionPane.showMessageDialog(this,
                "Ctrl+K o Ctrl+F    Buscar un módulo\n"
                + "Enter                   Abrir el primer resultado / la tarjeta seleccionada\n"
                + "Flechas                Moverse entre las tarjetas\n"
                + "Esc                      Limpiar la búsqueda\n"
                + "Ctrl+1 … Ctrl+9    Abrir módulos del menú\n"
                + "F5                        Actualizar el tablero",
                "Atajos de teclado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
                "AutoTech Manager\n"
                + "Sistema integral de gestión para taller mecánico\n\n"
                + "Programación Avanzada II - Segundo Parcial\n"
                + "Java · JDBC · SQL Server",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE, EstiloUI.iconoTaller(64, EstiloUI.PRIMARIO));
    }

    private void cerrarSesion() {
        String nombre = util.Sesion.activa() ? util.Sesion.actual().getNombreCompleto() : "";
        if (JOptionPane.showConfirmDialog(this, "¿Cerrar la sesión de " + nombre + "?\nSe cerrarán los módulos abiertos.",
                "Cerrar sesión", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        refresco.stop();
        if (reloj != null) {
            reloj.stop();
        }
        giro.stop();
        Navegador.cerrarTodos();
        new servicio.AutenticacionService().cerrarSesion();
        dispose();
        new FrmLogin().setVisible(true);
    }

    private void salir() {
        int r = JOptionPane.showConfirmDialog(this, "¿Desea salir de AutoTech Manager?",
                "Salir", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /** Cantidad de módulos que ya tienen formulario. */
    public long modulosDisponibles() {
        return MODULOS.stream().filter(m -> Navegador.existe(m.clase)).count();
    }

    // ================================================================== utilidades de la vista

    private static AbstractAction accion(Runnable r) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                r.run();
            }
        };
    }

    private static JPanel fila(Component... cs) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setAlignmentX(LEFT_ALIGNMENT);
        for (Component c : cs) {
            p.add(c);
        }
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 4));
        return p;
    }

    private static JComponent alinear(JComponent c, int alto) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setPreferredSize(new Dimension(600, alto));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        c.setMinimumSize(new Dimension(300, alto));
        return c;
    }

    private static JPanel titulo(String texto, String ayuda) {
        JLabel t = new JLabel(texto);
        t.setFont(EstiloUI.fuente(Font.BOLD, 15f));
        t.setForeground(EstiloUI.AZUL_OSCURO);
        if (ayuda == null) {
            return fila(t);
        }
        JLabel a = new JLabel("   " + ayuda);
        a.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        a.setForeground(EstiloUI.TEXTO_SUAVE);
        return fila(t, a);
    }

    private static JLabel nota(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(EstiloUI.fuente(Font.PLAIN, 12.5f));
        l.setForeground(EstiloUI.TEXTO_SUAVE);
        l.setBorder(BorderFactory.createEmptyBorder(4, 2, 8, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static String recortar(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    static Color colorEstado(EstadoOrden e) {
        return switch (e) {
            case RECIBIDA -> new Color(0x4B5563);
            case EN_DIAGNOSTICO -> new Color(0x1D4ED8);
            case PENDIENTE_APROBACION -> new Color(0xB45309);
            case EN_REPARACION -> new Color(0xC2410C);
            case EN_CONTROL_CALIDAD -> new Color(0x7E22CE);
            case FINALIZADA -> new Color(0x047857);
            case ENTREGADA -> new Color(0x065F46);
            case RECHAZADA -> new Color(0xB91C1C);
            case CANCELADA -> new Color(0x6B7280);
        };
    }

    private static String colorHex(Color c) {
        return String.format("#%06X", c.getRGB() & 0xFFFFFF);
    }

    private static Color mezclar(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    private static Graphics2D suave(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    // ================================================================== componentes

    /** Botón que anima suavemente su estado "encima" (hover o foco) entre 0 y 1. */
    private abstract static class BotonAnimado extends JButton {

        protected float hover;
        private boolean objetivo;
        private final Timer animacion = new Timer(15, e -> paso());

        BotonAnimado() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getModel().addChangeListener(e -> animarHacia(getModel().isRollover() || isFocusOwner()));
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    animarHacia(true);
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    animarHacia(getModel().isRollover());
                }
            });
        }

        private void animarHacia(boolean valor) {
            objetivo = valor;
            if (!animacion.isRunning()) {
                animacion.start();
            }
        }

        private void paso() {
            hover += objetivo ? 0.14f : -0.14f;
            if (hover >= 1 || hover <= 0) {
                hover = Math.max(0, Math.min(1, hover));
                animacion.stop();
            }
            repaint();
        }

        @Override
        protected void paintBorder(Graphics g) {
        }
    }

    /** Tarjeta de un módulo: se eleva al pasar el mouse y muestra su atajo y alertas. */
    private static final class Tarjeta extends BotonAnimado {

        final Modulo modulo;
        private final Color color;
        private final boolean disponible;
        private String insignia;
        private boolean alerta;

        Tarjeta(Modulo m, Color color, boolean disponible) {
            this.modulo = m;
            this.color = color;
            this.disponible = disponible;
            setText(m.titulo);
            setPreferredSize(new Dimension(230, 104));
            getAccessibleContext().setAccessibleName(m.titulo);
            setToolTipText(disponible ? m.titulo + " — " + m.descripcion : "Módulo en desarrollo (vista." + m.clase + ")");
        }

        void setInsignia(String texto, boolean alerta) {
            this.insignia = texto;
            this.alerta = alerta;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int w = getWidth();
            int h = getHeight();
            float elev = hover * 3f;
            boolean presionado = getModel().isArmed() && getModel().isPressed();

            // sombra que crece al elevarse
            for (int i = 3; i >= 1; i--) {
                g2.setColor(new Color(90, 20, 20, Math.round((6 + 14 * hover) / i)));
                g2.fill(new RoundRectangle2D.Float(2 - i * 0.5f, 6 + i - elev * 0.3f, w - 4 + i, h - 9 + i * 0.5f, 16, 16));
            }
            RoundRectangle2D.Float tarjeta = new RoundRectangle2D.Float(2, 4 - elev, w - 4, h - 9, 14, 14);
            g2.setColor(presionado ? new Color(0xFFF1F1) : Color.WHITE);
            g2.fill(tarjeta);

            // franja de la sección
            java.awt.Shape clip = g2.getClip();
            g2.clip(tarjeta);
            g2.setColor(disponible ? color : EstiloUI.BORDE);
            g2.fillRect(2, 0, 5, h);
            g2.setClip(clip);

            g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
            g2.setColor(isFocusOwner() ? EstiloUI.PRIMARIO : mezclar(EstiloUI.BORDE, color, hover * 0.8f));
            g2.draw(tarjeta);

            int y0 = Math.round(4 - elev);
            // sigla del módulo
            int s = 40;
            float icx = 20;
            float icy = y0 + 16;
            g2.setColor(disponible ? mezclar(color, color.brighter(), hover * 0.4f) : EstiloUI.BORDE);
            g2.fill(new RoundRectangle2D.Float(icx, icy, s, s, 12, 12));
            g2.setFont(EstiloUI.fuente(Font.BOLD, 14f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(modulo.sigla, icx + (s - fm.stringWidth(modulo.sigla)) / 2f, icy + (s + fm.getAscent() - fm.getDescent()) / 2f);

            // textos
            int tx = 74;
            int ancho = w - tx - 12;
            g2.setFont(EstiloUI.fuente(Font.BOLD, 13.5f));
            g2.setColor(EstiloUI.TEXTO);
            List<String> titulo = lineas(g2.getFontMetrics(), modulo.titulo, ancho - 18, 2);
            for (int i = 0; i < titulo.size(); i++) {
                g2.drawString(titulo.get(i), tx, y0 + 30 + i * 17);
            }
            int yd = y0 + 48 + (titulo.size() - 1) * 17;
            g2.setFont(EstiloUI.fuente(Font.PLAIN, 11.5f));
            g2.setColor(EstiloUI.TEXTO_SUAVE);
            List<String> lineas = lineas(g2.getFontMetrics(), modulo.descripcion, ancho, 3 - titulo.size());
            for (int i = 0; i < lineas.size(); i++) {
                g2.drawString(lineas.get(i), tx, yd + i * 15);
            }

            // insignia (alertas o totales) y atajo de teclado
            int by = y0 + h - 33;
            if (!disponible) {
                pastilla(g2, "Pendiente", tx, by, new Color(0xFEF3C7), new Color(0xB45309));
            } else if (insignia != null) {
                pastilla(g2, insignia, tx, by, alerta ? EstiloUI.ERROR_FONDO : FONDO, alerta ? EstiloUI.ERROR : EstiloUI.TEXTO_SUAVE);
            }
            // flecha que aparece al pasar el mouse
            if (hover > 0.05f && disponible) {
                g2.setColor(mezclar(new Color(255, 255, 255, 0), color, hover));
                g2.setFont(EstiloUI.fuente(Font.BOLD, 16f));
                g2.drawString("→", w - 30 + Math.round((1 - hover) * -6), y0 + 30);
            }
            g2.dispose();
        }

        private static void pastilla(Graphics2D g2, String texto, int x, int y, Color fondo, Color tinta) {
            g2.setFont(EstiloUI.fuente(Font.BOLD, 10.5f));
            FontMetrics fm = g2.getFontMetrics();
            int pw = fm.stringWidth(texto) + 14;
            g2.setColor(fondo);
            g2.fill(new RoundRectangle2D.Float(x, y, pw, 18, 18, 18));
            g2.setColor(tinta);
            g2.drawString(texto, x + 7, y + 13);
        }

        private static String ajustar(FontMetrics fm, String s, int ancho) {
            if (fm.stringWidth(s) <= ancho) {
                return s;
            }
            while (s.length() > 1 && fm.stringWidth(s + "…") > ancho) {
                s = s.substring(0, s.length() - 1);
            }
            return s + "…";
        }

        private static List<String> lineas(FontMetrics fm, String texto, int ancho, int max) {
            List<String> res = new ArrayList<>();
            StringBuilder actual = new StringBuilder();
            for (String p : texto.split(" ")) {
                String prueba = actual.length() == 0 ? p : actual + " " + p;
                if (fm.stringWidth(prueba) > ancho && actual.length() > 0) {
                    res.add(actual.toString());
                    actual = new StringBuilder(p);
                } else {
                    actual = new StringBuilder(prueba);
                }
            }
            if (actual.length() > 0) {
                res.add(actual.toString());
            }
            if (res.size() > max) {
                List<String> corto = new ArrayList<>(res.subList(0, max));
                corto.set(max - 1, ajustar(fm, corto.get(max - 1) + " " + res.get(max), ancho));
                return corto;
            }
            return res;
        }
    }

    /** Indicador del tablero: el número sube animado y al hacer clic abre el módulo. */
    private static final class Indicador extends BotonAnimado {

        final String titulo;
        final String accion;
        final String clase;
        final EstadoOrden estado;
        private double valor;
        private double mostrado;
        private String prefijo;
        private String detalle = " ";
        private boolean conDatos;
        private boolean alerta;
        private final Timer conteo = new Timer(16, e -> contar());

        Indicador(String titulo, String accion, String clase, EstadoOrden estado) {
            this.titulo = titulo;
            this.accion = accion;
            this.clase = clase;
            this.estado = estado;
            setToolTipText(accion);
        }

        void mostrar(double nuevo, String prefijo, String detalle) {
            this.prefijo = prefijo;
            this.detalle = detalle;
            this.conDatos = true;
            this.valor = nuevo;
            conteo.restart();
        }

        void sinDatos() {
            conDatos = false;
            alerta = false;
            detalle = "sin conexión";
            repaint();
        }

        void setAlerta(boolean a) {
            alerta = a;
            repaint();
        }

        private void contar() {
            double dif = valor - mostrado;
            if (Math.abs(dif) < 0.5) {
                mostrado = valor;
                conteo.stop();
            } else {
                mostrado += dif * 0.18;
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int w = getWidth();
            int h = getHeight();
            float elev = hover * 2f;
            g2.setColor(new Color(90, 20, 20, Math.round(8 + 16 * hover)));
            g2.fill(new RoundRectangle2D.Float(1, 4, w - 2, h - 5, 14, 14));
            RoundRectangle2D.Float r = new RoundRectangle2D.Float(1, 2 - elev, w - 2, h - 6, 14, 14);
            g2.setColor(Color.WHITE);
            g2.fill(r);
            g2.setColor(isFocusOwner() ? EstiloUI.PRIMARIO : mezclar(EstiloUI.BORDE, EstiloUI.PRIMARIO, hover * 0.7f));
            g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
            g2.draw(r);
            int y0 = Math.round(2 - elev);

            g2.setFont(EstiloUI.fuente(Font.PLAIN, 11.5f));
            g2.setColor(EstiloUI.TEXTO_SUAVE);
            g2.drawString(Tarjeta.ajustar(g2.getFontMetrics(), titulo, w - 28), 14, y0 + 22);
            if (alerta) {
                g2.setColor(EstiloUI.PRIMARIO);
                g2.fillOval(w - 18, y0 + 13, 8, 8);
            }

            String texto = !conDatos ? "—"
                    : prefijo != null ? prefijo + String.format("%,.2f", mostrado)
                    : String.valueOf(Math.round(mostrado));
            float tam = texto.length() > 10 ? 20f : 26f;
            g2.setFont(EstiloUI.fuente(Font.BOLD, tam));
            g2.setColor(alerta ? EstiloUI.PRIMARIO : EstiloUI.AZUL_OSCURO);
            g2.drawString(texto, 14, y0 + 56);

            g2.setFont(EstiloUI.fuente(Font.PLAIN, 11f));
            String abajo = hover > 0.5f ? accion + " →" : detalle;
            g2.setColor(hover > 0.5f ? EstiloUI.PRIMARIO : EstiloUI.TEXTO_SUAVE);
            g2.drawString(Tarjeta.ajustar(g2.getFontMetrics(), abajo, w - 24), 14, y0 + 80);
            g2.dispose();
        }
    }

    /** Fila clicable del panel lateral (órdenes recientes y módulos abiertos). */
    private static final class Fila extends BotonAnimado {

        private Color marca;
        private String sigla;

        Fila(String html) {
            setText(html);
            setFont(EstiloUI.fuente(Font.PLAIN, 12.5f));
            setForeground(EstiloUI.TEXTO);
            setHorizontalAlignment(LEFT);
            setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        }

        void setMarca(Color color, String sigla) {
            this.marca = color;
            this.sigla = sigla;
            setBorder(BorderFactory.createEmptyBorder(7, 48, 7, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            if (hover > 0) {
                g2.setColor(mezclar(new Color(255, 255, 255, 0), EstiloUI.ACENTO_CLARO, hover));
                g2.fill(new RoundRectangle2D.Float(0, 1, getWidth(), getHeight() - 2, 10, 10));
            }
            if (isFocusOwner()) {
                g2.setColor(EstiloUI.PRIMARIO);
                g2.draw(new RoundRectangle2D.Float(0.5f, 1.5f, getWidth() - 1, getHeight() - 3, 10, 10));
            }
            if (marca != null) {
                int s = 28;
                int y = (getHeight() - s) / 2;
                g2.setColor(marca);
                g2.fill(new RoundRectangle2D.Float(10, y, s, s, 8, 8));
                g2.setFont(EstiloUI.fuente(Font.BOLD, 11f));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(sigla, 10 + (s - fm.stringWidth(sigla)) / 2f, y + (s + fm.getAscent() - fm.getDescent()) / 2f);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Etapas del flujo de órdenes dibujadas como flechas, con su cantidad. */
    private static final class FlujoOrdenes extends JComponent {

        private static final EstadoOrden[] ETAPAS = {
            EstadoOrden.RECIBIDA, EstadoOrden.EN_DIAGNOSTICO, EstadoOrden.PENDIENTE_APROBACION,
            EstadoOrden.EN_REPARACION, EstadoOrden.EN_CONTROL_CALIDAD, EstadoOrden.FINALIZADA, EstadoOrden.ENTREGADA};
        private static final String[] NOMBRES = {
            "Recibida", "Diagnóstico", "Por aprobar", "Reparación", "Calidad", "Finalizada", "Entregada"};

        private final Map<EstadoOrden, Integer> cantidades = new LinkedHashMap<>();
        private final Consumer<EstadoOrden> alHacerClic;
        private int encima = -1;

        FlujoOrdenes(Consumer<EstadoOrden> alHacerClic) {
            this.alHacerClic = alHacerClic;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("");
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int i = etapaEn(e.getPoint());
                    if (i != encima) {
                        encima = i;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = -1;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    int i = etapaEn(e.getPoint());
                    if (i >= 0) {
                        alHacerClic.accept(ETAPAS[i]);
                    }
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
        }

        void setCantidades(Map<EstadoOrden, Integer> datos) {
            cantidades.clear();
            cantidades.putAll(datos);
            repaint();
        }

        private int etapaEn(Point p) {
            int i = (int) (p.x / (getWidth() / (double) ETAPAS.length));
            return i >= 0 && i < ETAPAS.length ? i : -1;
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int i = etapaEn(e.getPoint());
            if (i < 0) {
                return null;
            }
            int n = cantidades.getOrDefault(ETAPAS[i], 0);
            return ETAPAS[i] + ": " + n + (n == 1 ? " orden" : " órdenes") + (n > 0 ? " — clic para abrir" : "");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int n = ETAPAS.length;
            float ancho = getWidth() / (float) n;
            int h = getHeight() - 4;
            float punta = 14;
            for (int i = 0; i < n; i++) {
                float x = i * ancho;
                int cant = cantidades.getOrDefault(ETAPAS[i], 0);
                Color base = colorEstado(ETAPAS[i]);
                Path2D.Float flecha = new Path2D.Float();
                flecha.moveTo(x + (i == 0 ? 0 : 2), 2);
                flecha.lineTo(x + ancho - punta + 2, 2);
                flecha.lineTo(x + ancho + 2 - (i == n - 1 ? punta : 0), 2 + h / 2f);
                flecha.lineTo(x + ancho - punta + 2, 2 + h);
                flecha.lineTo(x + (i == 0 ? 0 : 2), 2 + h);
                if (i > 0) {
                    flecha.lineTo(x + punta + 2, 2 + h / 2f);
                }
                flecha.closePath();
                if (i == n - 1) {
                    flecha = new Path2D.Float();
                    flecha.moveTo(x + 2, 2);
                    flecha.lineTo(x + ancho, 2);
                    flecha.lineTo(x + ancho, 2 + h);
                    flecha.lineTo(x + 2, 2 + h);
                    flecha.lineTo(x + punta + 2, 2 + h / 2f);
                    flecha.closePath();
                }
                boolean activo = cant > 0;
                Color fondo = activo ? mezclar(Color.WHITE, base, encima == i ? 0.95f : 0.85f)
                        : (encima == i ? new Color(0xE5E7EB) : Color.WHITE);
                g2.setColor(fondo);
                g2.fill(flecha);
                g2.setColor(activo ? base.darker() : EstiloUI.BORDE);
                g2.draw(flecha);

                float cx = x + ancho / 2f + (i == 0 ? -4 : 4);
                g2.setFont(EstiloUI.fuente(Font.BOLD, 20f));
                String num = String.valueOf(cant);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(activo ? Color.WHITE : EstiloUI.TEXTO_SUAVE);
                g2.drawString(num, cx - fm.stringWidth(num) / 2f, h / 2f - 1);
                g2.setFont(EstiloUI.fuente(Font.PLAIN, 11.5f));
                fm = g2.getFontMetrics();
                g2.drawString(NOMBRES[i], cx - fm.stringWidth(NOMBRES[i]) / 2f, h / 2f + 18);
            }
            g2.dispose();
        }
    }

    /** Barra de menú con el degradado oscuro del tema. */
    private static final class BarraMenu extends JMenuBar {

        BarraMenu() {
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(0x1C0709), getWidth(), 0, EstiloUI.AZUL_OSCURO));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 25));
            g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            g2.dispose();
        }
    }

    /** Menú superior: texto blanco, píldora roja al pasar el mouse o al abrirlo. */
    private static final class MenuSuperiorUI extends javax.swing.plaf.basic.BasicMenuUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();
            selectionBackground = EstiloUI.PRIMARIO;
            selectionForeground = Color.WHITE;
        }

        @Override
        protected void paintBackground(Graphics g, JMenuItem item, Color bg) {
            boolean abierto = item.getModel().isSelected();
            boolean encima = Boolean.TRUE.equals(item.getClientProperty("encima"));
            if (abierto || encima) {
                Graphics2D g2 = suave(g);
                g2.setColor(abierto ? EstiloUI.PRIMARIO : new Color(255, 255, 255, 30));
                g2.fill(new RoundRectangle2D.Float(2, 4, item.getWidth() - 4, item.getHeight() - 8, 10, 10));
                g2.dispose();
            }
        }
    }

    /** Opción de un menú desplegable: fondo blanco y resaltado rojo claro. */
    private static final class ElementoMenuUI extends javax.swing.plaf.basic.BasicMenuItemUI {

        // Se dibuja todo a mano (ícono, texto y atajo) para que se vea igual en Windows y en
        // cualquier otro sistema: la distribución estándar de Windows reserva espacio para
        // casillas de verificación y descuadraba los íconos.
        private static final int ALTO = 34;
        private static final int X_ICONO = 14;
        private static final int X_TEXTO = 46;

        @Override
        public Dimension getPreferredSize(JComponent c) {
            JMenuItem item = (JMenuItem) c;
            FontMetrics fm = item.getFontMetrics(item.getFont());
            int ancho = X_TEXTO + fm.stringWidth(item.getText()) + 28;
            return new Dimension(Math.max(ancho, 210), ALTO);
        }

        @Override
        public Dimension getMinimumSize(JComponent c) {
            return getPreferredSize(c);
        }

        @Override
        public Dimension getMaximumSize(JComponent c) {
            return new Dimension(Short.MAX_VALUE, ALTO);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            JMenuItem item = (JMenuItem) c;
            Graphics2D g2 = suave(g);
            int w = item.getWidth();
            int h = item.getHeight();
            boolean activo = item.getModel().isArmed() || item.getModel().isSelected();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            if (activo) {
                g2.setColor(EstiloUI.ACENTO_CLARO);
                g2.fill(new RoundRectangle2D.Float(4, 1, w - 8, h - 2, 8, 8));
                g2.setColor(EstiloUI.PRIMARIO);
                g2.fill(new RoundRectangle2D.Float(4, 7, 3, h - 14, 3, 3));
            }
            javax.swing.Icon icono = item.getIcon();
            if (icono != null) {
                icono.paintIcon(item, g2, X_ICONO, (h - icono.getIconHeight()) / 2);
            }
            g2.setFont(item.getFont());
            FontMetrics fm = g2.getFontMetrics();
            int base = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(activo ? EstiloUI.PRIMARIO_PRESIONADO : EstiloUI.TEXTO);
            g2.drawString(item.getText(), X_TEXTO, base);
            g2.dispose();
        }

        @Override
        public void update(Graphics g, JComponent c) {
            paint(g, c);
        }
    }

    /** Sigla del módulo en un cuadrito de color (ícono de las opciones del menú). */
    private static final class IconoSigla implements javax.swing.Icon {

        private final String sigla;
        private final Color color;

        IconoSigla(String sigla, Color color) {
            this.sigla = sigla;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = suave(g);
            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Float(x, y, 22, 18, 7, 7));
            g2.setFont(EstiloUI.fuente(Font.BOLD, 9.5f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(sigla, x + (22 - fm.stringWidth(sigla)) / 2f, y + (18 + fm.getAscent() - fm.getDescent()) / 2f);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 22;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }
    }

    /** Botón "Salir" del encabezado: píldora translúcida que se vuelve blanca al pasar el mouse. */
    private static final class BotonSalir extends BotonAnimado {

        BotonSalir() {
            setText("Salir");
            setFont(EstiloUI.fuente(Font.BOLD, 13.5f));
            FontMetrics fm = getFontMetrics(getFont());
            setPreferredSize(new Dimension(fm.stringWidth("Salir") + 58, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D.Float r = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, h, h);
            g2.setColor(new Color(255, 255, 255, Math.round(34 + 221 * hover)));
            g2.fill(r);
            g2.setColor(new Color(255, 255, 255, isFocusOwner() ? 230 : 110));
            g2.draw(r);
            Color tinta = mezclar(Color.WHITE, EstiloUI.PRIMARIO, hover);
            new IconoSalir(14, tinta).paintIcon(this, g2, 12, (h - 18) / 2);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(tinta);
            g2.drawString(getText(), 38, (h + fm.getAscent() - fm.getDescent()) / 2f);
            g2.dispose();
        }
    }

    /** Ícono de salir: puerta con flecha. */
    private static final class IconoSalir implements javax.swing.Icon {

        private final int tam;
        private final Color color;

        IconoSalir(int tam, Color color) {
            this.tam = tam;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = suave(g);
            g2.translate(x + 4, y + 2);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawPolyline(new int[]{8, 1, 1, 8}, new int[]{0, 0, tam, tam}, 4);
            g2.drawLine(5, tam / 2, tam + 2, tam / 2);
            g2.drawPolyline(new int[]{tam - 2, tam + 2, tam - 2}, new int[]{tam / 2 - 4, tam / 2, tam / 2 + 4}, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 22;
        }

        @Override
        public int getIconHeight() {
            return tam + 4;
        }
    }

    /** Ícono de recargar dibujado (flecha circular); gira mientras se actualiza el tablero. */
    private static final class IconoRecargar implements javax.swing.Icon {

        private final int tam;
        private final Color color;
        private double angulo;

        IconoRecargar(int tam, Color color) {
            this.tam = tam;
            this.color = color;
        }

        void girar(double grados) {
            angulo = (angulo + grados) % 360;
        }

        void reiniciar() {
            angulo = 0;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = suave(g);
            double cx = x + tam / 2.0;
            double cy = y + tam / 2.0;
            g2.rotate(Math.toRadians(angulo), cx, cy);
            float r = tam / 2f - 2f;
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.8f, tam / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new java.awt.geom.Arc2D.Double(cx - r, cy - r, 2 * r, 2 * r, 70, 290, java.awt.geom.Arc2D.OPEN));
            double a = Math.toRadians(70);
            double px = cx + r * Math.cos(a);
            double py = cy - r * Math.sin(a);
            double s = tam * 0.32;
            Path2D.Double punta = new Path2D.Double();
            punta.moveTo(px + s * 0.9, py - s * 0.15);
            punta.lineTo(px - s * 0.35, py - s * 0.75);
            punta.lineTo(px - s * 0.1, py + s * 0.55);
            punta.closePath();
            g2.fill(punta);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return tam + 4;
        }

        @Override
        public int getIconHeight() {
            return tam;
        }
    }

    /** Contenido desplazable solo en vertical: el ancho se ajusta a la ventana. */
    private static final class PanelAncho extends JPanel implements javax.swing.Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) {
            return 18;
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
            return false;
        }
    }

    /** Campo de búsqueda con texto de ayuda y lupa. */
    private static final class CampoBusqueda extends JTextField {

        private final String ayuda;

        CampoBusqueda(String ayuda) {
            this.ayuda = ayuda;
            setFont(EstiloUI.fuente(Font.PLAIN, 14f));
            setOpaque(false);
            setForeground(EstiloUI.TEXTO);
            setCaretColor(EstiloUI.PRIMARIO);
            setBorder(BorderFactory.createEmptyBorder(6, 36, 6, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            if (isFocusOwner()) {
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, getWidth() - 3, getHeight() - 3, getHeight(), getHeight()));
            }
            // lupa
            g2.setColor(EstiloUI.TEXTO_SUAVE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cy = getHeight() / 2;
            g2.drawOval(14, cy - 8, 12, 12);
            g2.drawLine(24, cy + 2, 28, cy + 6);
            if (getText().isEmpty()) {
                g2.setFont(getFont());
                g2.setColor(new Color(0x9CA3AF));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ayuda, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
