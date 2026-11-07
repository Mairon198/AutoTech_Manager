package vista;

import conexion.ConexionDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * Menú principal de AutoTech Manager: navegación hacia todos los módulos.
 *
 * Los módulos se declaran en la lista MODULOS. Cada uno indica el nombre de la
 * clase de su formulario; si la clase todavía no existe, la tarjeta aparece como
 * "Pendiente" y el sistema sigue compilando y funcionando.
 */
public class FrmMenuPrincipal extends JFrame {

    // Misma paleta de los demás formularios (vista.EstiloUI)
    private static final Color AZUL = EstiloUI.AZUL_OSCURO;
    private static final Color ACENTO = EstiloUI.PRIMARIO;
    private static final Color HOVER = EstiloUI.VERDE_CLARO;
    private static final Color GRIS_TEXTO = EstiloUI.TEXTO_SUAVE;
    private static final Color VERDE = new Color(0x04, 0x78, 0x57);
    private static final Color ROJO = EstiloUI.ERROR;
    private static final Color FONDO = EstiloUI.FONDO_CLARO;

    /** Un módulo del menú: texto, formulario que abre y breve descripción. */
    private static final class Modulo {
        final String titulo;
        final String clase;
        final String descripcion;
        final int tecla;

        Modulo(String titulo, String clase, String descripcion, int tecla) {
            this.titulo = titulo;
            this.clase = clase;
            this.descripcion = descripcion;
            this.tecla = tecla;
        }
    }

    private static final Map<String, List<Modulo>> MODULOS = new LinkedHashMap<>();

    static {
        agregar("Clientes y vehículos",
                new Modulo("Clientes", "FrmClientes", "Registrar, modificar y desactivar clientes", KeyEvent.VK_1),
                new Modulo("Vehículos", "FrmVehiculos", "Placa, VIN, marca, modelo y kilometraje", KeyEvent.VK_2));
        agregar("Taller",
                new Modulo("Órdenes de trabajo", "FrmOrdenTrabajo", "Ingreso, servicios y flujo de estados", KeyEvent.VK_3),
                new Modulo("Diagnóstico y presupuesto", "FrmDiagnosticoPresupuesto", "Evaluación técnica y cotización", KeyEvent.VK_4),
                new Modulo("Asignación y actividades", "FrmAsignacionActividades", "Mecánicos por orden y avance", KeyEvent.VK_5),
                new Modulo("Control de calidad", "FrmControlCalidad", "Verificación antes de la entrega", KeyEvent.VK_6),
                new Modulo("Cambio de motor", "FrmCambioMotor", "Sustitución de motor (caso Hilux)", KeyEvent.VK_7));
        agregar("Catálogo, personal e inventario",
                new Modulo("Servicios", "FrmServicios", "Catálogo por nivel de complejidad", 0),
                new Modulo("Mecánicos", "FrmMecanicos", "Personal, especialidades y disponibilidad", 0),
                new Modulo("Inventario", "FrmInventario", "Repuestos, entradas, salidas y stock mínimo", 0),
                new Modulo("Proveedores", "FrmProveedores", "Suministradores de repuestos y motores", 0));
        agregar("Caja y consultas",
                new Modulo("Pagos y facturación", "FrmPagosFacturacion", "Abonos, saldo y factura", KeyEvent.VK_8),
                new Modulo("Historial de vehículo", "FrmHistorialVehiculo", "Servicios, mecánicos, repuestos y costos", KeyEvent.VK_9),
                new Modulo("Reportes", "FrmReportes", "Reportes administrativos", 0));
    }

    private static void agregar(String seccion, Modulo... modulos) {
        MODULOS.put(seccion, List.of(modulos));
    }

    private final JLabel lblConexion = new JLabel("Conectando con la base de datos...");
    private final JLabel lblReloj = new JLabel();
    private final List<JButton> tarjetas = new ArrayList<>();

    public FrmMenuPrincipal() {
        super("AutoTech Manager - Menú principal");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                salir();
            }
        });
        setMinimumSize(new Dimension(980, 640));
        setSize(1120, 760);
        setLocationRelativeTo(null);

        setJMenuBar(crearBarraMenu());
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(crearCuerpo(), BorderLayout.CENTER);
        raiz.add(crearBarraEstado(), BorderLayout.SOUTH);
        setContentPane(raiz);

        iniciarReloj();
        verificarConexion();
    }

    // ------------------------------------------------------------------ interfaz

    private JMenuBar crearBarraMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenuItem conexion = new JMenuItem("Probar conexión");
        conexion.addActionListener(e -> verificarConexion());
        JMenuItem salir = new JMenuItem("Salir");
        salir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        salir.addActionListener(e -> salir());
        archivo.add(conexion);
        archivo.addSeparator();
        archivo.add(salir);
        barra.add(archivo);

        for (Map.Entry<String, List<Modulo>> seccion : MODULOS.entrySet()) {
            JMenu menu = new JMenu(seccion.getKey());
            for (Modulo m : seccion.getValue()) {
                JMenuItem item = new JMenuItem(m.titulo);
                if (m.tecla != 0) {
                    item.setAccelerator(KeyStroke.getKeyStroke(m.tecla, java.awt.event.InputEvent.CTRL_DOWN_MASK));
                }
                item.addActionListener(e -> Navegador.abrir(this, m.clase, m.titulo));
                menu.add(item);
            }
            barra.add(menu);
        }

        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acerca = new JMenuItem("Acerca de AutoTech Manager");
        acerca.addActionListener(e -> mostrarAcercaDe());
        ayuda.add(acerca);
        barra.add(ayuda);
        return barra;
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(AZUL);
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titulo = new JLabel("AutoTech Manager");
        titulo.setFont(EstiloUI.fuente(Font.BOLD, 26f));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("AutoTech Motors S. de R.L.  ·  Gestión y control de servicios automotrices");
        subtitulo.setFont(EstiloUI.fuente(Font.PLAIN, 14f));
        subtitulo.setForeground(new Color(0xC9, 0xD6, 0xEA));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(subtitulo);
        encabezado.add(textos, BorderLayout.WEST);
        return encabezado;
    }

    private JScrollPane crearCuerpo() {
        JPanel cuerpo = new JPanel();
        cuerpo.setBackground(FONDO);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));

        for (Map.Entry<String, List<Modulo>> seccion : MODULOS.entrySet()) {
            JLabel lbl = new JLabel(seccion.getKey());
            lbl.setFont(EstiloUI.fuente(Font.BOLD, 15f));
            lbl.setForeground(AZUL);
            lbl.setBorder(BorderFactory.createEmptyBorder(14, 2, 6, 0));
            JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            fila.setOpaque(false);
            fila.add(lbl);
            fila.setAlignmentX(LEFT_ALIGNMENT);
            cuerpo.add(fila);

            JPanel grilla = new JPanel(new GridLayout(0, 4, 12, 12));
            grilla.setOpaque(false);
            grilla.setAlignmentX(LEFT_ALIGNMENT);
            for (Modulo m : seccion.getValue()) {
                grilla.add(crearTarjeta(m));
            }
            // relleno para que todas las filas tengan 4 columnas del mismo ancho
            for (int i = seccion.getValue().size() % 4; i > 0 && i < 4; i++) {
                JPanel vacio = new JPanel();
                vacio.setOpaque(false);
                grilla.add(vacio);
            }
            cuerpo.add(grilla);
        }

        JScrollPane scroll = new JScrollPane(cuerpo);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JButton crearTarjeta(Modulo m) {
        boolean disponible = Navegador.existe(m.clase);
        String estado = disponible
                ? ""
                : "<br><span style='color:#B26A00;font-size:9px'>PENDIENTE</span>";
        JButton tarjeta = new JButton("<html><div style='width:170px'>"
                + "<b style='font-size:12px'>" + m.titulo + "</b><br>"
                + "<span style='color:#6B7280'>" + m.descripcion + "</span>" + estado
                + "</div></html>");
        tarjeta.setHorizontalAlignment(SwingConstants.LEFT);
        tarjeta.setVerticalAlignment(SwingConstants.TOP);
        tarjeta.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        tarjeta.setFocusPainted(false);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setPreferredSize(new Dimension(220, 84));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, disponible ? ACENTO : EstiloUI.BORDE),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(EstiloUI.BORDE),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12))));
        tarjeta.setToolTipText(disponible ? "Abrir " + m.titulo : "Módulo en desarrollo (vista." + m.clase + ")");
        tarjeta.addActionListener(e -> Navegador.abrir(this, m.clase, m.titulo));
        tarjeta.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                tarjeta.setBackground(HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                tarjeta.setBackground(Color.WHITE);
            }
        });
        tarjetas.add(tarjeta);
        return tarjeta;
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.BORDE),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        lblConexion.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblConexion.setForeground(GRIS_TEXTO);
        lblReloj.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblReloj.setForeground(GRIS_TEXTO);
        barra.add(lblConexion, BorderLayout.WEST);
        barra.add(lblReloj, BorderLayout.EAST);
        return barra;
    }

    // ------------------------------------------------------------------ lógica

    private void iniciarReloj() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy, hh:mm a", Locale.of("es", "HN"));
        Timer reloj = new Timer(1000, e -> lblReloj.setText(LocalDateTime.now().format(formato)));
        reloj.setInitialDelay(0);
        reloj.start();
    }

    /**
     * Prueba la conexión en segundo plano (SwingWorker) para no congelar la
     * ventana mientras responde el servidor de Azure.
     */
    private void verificarConexion() {
        lblConexion.setForeground(GRIS_TEXTO);
        lblConexion.setText("Conectando con la base de datos...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try (java.sql.Connection cn = ConexionDB.getConexion()) {
                    return cn.getCatalog();
                }
            }

            @Override
            protected void done() {
                try {
                    String base = get();
                    lblConexion.setForeground(VERDE);
                    lblConexion.setText("● Conectado a " + base);
                    lblConexion.setToolTipText(null);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    String detalle = String.valueOf(causa.getMessage());
                    lblConexion.setForeground(ROJO);
                    lblConexion.setText("● Sin conexión: " + (detalle.length() > 70 ? detalle.substring(0, 70) + "..." : detalle));
                    lblConexion.setToolTipText("<html><div style='width:400px'>" + detalle + "</div></html>");
                }
            }
        }.execute();
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
                "AutoTech Manager\n"
                + "Sistema integral de gestión para taller mecánico\n\n"
                + "Programación Avanzada II - Segundo Parcial\n"
                + "Java · JDBC · SQL Server",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }

    private void salir() {
        int r = JOptionPane.showConfirmDialog(this, "¿Desea salir de AutoTech Manager?",
                "Salir", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    /** Cantidad de módulos que ya tienen formulario (útil para la integración). */
    public long modulosDisponibles() {
        return MODULOS.values().stream().flatMap(List::stream).filter(m -> Navegador.existe(m.clase)).count();
    }
}
