package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import modelo.OrdenResumen;
import modelo.enums.EstadoOrden;
import modelo.enums.MetodoPago;
import servicio.FacturacionService;
import servicio.PagoService;

/**
 * Módulo 8: pagos parciales o totales (efectivo, tarjeta, transferencia) con control
 * de saldo, factura y entrega del vehículo.
 */
public class FrmPagosFacturacion extends FrmBaseOrden {

    private final PagoService pagos = new PagoService();
    private final FacturacionService facturacion = new FacturacionService();

    private final JLabel lblTotal = cifra();
    private final JLabel lblPagado = cifra();
    private final JLabel lblSaldo = cifra();
    private final JLabel lblGravado = monto();
    private final JLabel lblDescuento = monto();
    private final JLabel lblSubtotal = monto();
    private final JLabel lblIsvTitulo = new JLabel("ISV");
    private final JLabel lblIsv = monto();
    private final JLabel lblTotalFactura = monto();
    private final JSpinner spnMonto = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10_000_000.0, 100.0));
    private final JComboBox<MetodoPago> cmbMetodo = new JComboBox<>(MetodoPago.values());
    private final JTextField txtReferencia = EstiloUI.campoCompacto(new JTextField());
    private final JTable tblPagos = tabla();
    private final JTable tblFacturas = tabla();

    public FrmPagosFacturacion() {
        super("Pagos y facturación", "MÓDULO 8: FACTURACIÓN Y PAGOS",
                "Pagos parciales o totales, control de saldo, factura y entrega", false);

        JPanel cifras = new JPanel(new GridLayout(1, 3, 8, 0));
        cifras.setOpaque(false);
        cifras.add(tarjeta("Total", lblTotal));
        cifras.add(tarjeta("Pagado", lblPagado));
        cifras.add(tarjeta("Saldo", lblSaldo));

        JButton saldo = EstiloUI.enlace(new JButton("Pagar el saldo completo"));
        saldo.addActionListener(e -> {
            OrdenResumen o = orden();
            if (o != null) {
                spnMonto.setValue(o.getSaldo().doubleValue());
            }
        });
        JButton pagar = EstiloUI.botonPrimario("Registrar pago");
        pagar.addActionListener(e -> registrarPago());
        JButton facturar = EstiloUI.botonSecundario("Generar factura");
        facturar.addActionListener(e -> {
            if (ejecutar(() -> facturacion.generarFactura(orden()))) {
                recargar();
                avisar("Factura generada.");
            }
        });
        JButton entregar = EstiloUI.botonSecundario("Entregar vehículo");
        entregar.addActionListener(e -> entregar());

        Formulario f = new Formulario("Registrar pago")
                .ancho(desglose())
                .ancho(cifras)
                .campo("Monto (L)", spnMonto)
                .ancho(botones(saldo))
                .campo("Método", cmbMetodo)
                .campo("Referencia", txtReferencia)
                .ancho(botones(pagar))
                .ancho(nota("Se permiten pagos parciales; nunca se acepta un monto mayor que el saldo."))
                .ancho(botones(facturar, entregar))
                .ancho(nota("La factura se emite al cerrar la orden en Control de calidad; "
                        + "este botón sirve si quedó pendiente. Al entregar, la orden pasa a <b>Entregada</b>."))
                .relleno();
        f.setPreferredSize(new Dimension(450, 0));

        JPanel der = new JPanel(new GridLayout(2, 1, 0, 8));
        der.setOpaque(false);
        der.add(conTitulo(tblPagos, "Pagos de la orden"));
        der.add(conTitulo(tblFacturas, "Facturas emitidas y saldos"));

        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(f, BorderLayout.WEST);
        p.add(der, BorderLayout.CENTER);
        setContenido(p);
        cargarOrdenes(null);
    }

    private static JLabel cifra() {
        JLabel l = new JLabel("L 0.00");
        l.setFont(EstiloUI.fuente(Font.BOLD, 15f));
        l.setForeground(EstiloUI.AZUL_OSCURO);
        return l;
    }

    private static JLabel monto() {
        JLabel l = new JLabel("L 0.00", javax.swing.SwingConstants.RIGHT);
        l.setFont(EstiloUI.fuente(Font.PLAIN, 13f));
        l.setForeground(EstiloUI.TEXTO);
        return l;
    }

    /** Detalle de la factura: importe gravado, descuento, subtotal, ISV y total. */
    private JPanel desglose() {
        JPanel p = new JPanel(new GridLayout(5, 2, 8, 2));
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(EstiloUI.BORDE),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        fila(p, new JLabel("Importe gravado"), lblGravado, false);
        fila(p, new JLabel("Descuento"), lblDescuento, false);
        fila(p, new JLabel("Subtotal"), lblSubtotal, false);
        fila(p, lblIsvTitulo, lblIsv, false);
        fila(p, new JLabel("Total de la factura"), lblTotalFactura, true);
        return p;
    }

    private static void fila(JPanel p, JLabel titulo, JLabel valor, boolean resaltar) {
        titulo.setFont(EstiloUI.fuente(resaltar ? Font.BOLD : Font.PLAIN, 13f));
        titulo.setForeground(resaltar ? EstiloUI.AZUL_OSCURO : EstiloUI.TEXTO);
        if (resaltar) {
            valor.setFont(EstiloUI.fuente(Font.BOLD, 14f));
            valor.setForeground(EstiloUI.AZUL_OSCURO);
        }
        p.add(titulo);
        p.add(valor);
    }

    private static JPanel tarjeta(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(EstiloUI.FONDO_CLARO);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JLabel t = new JLabel(titulo);
        t.setFont(EstiloUI.fuente(Font.PLAIN, 11f));
        t.setForeground(EstiloUI.TEXTO_SUAVE);
        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private void registrarPago() {
        OrdenResumen o = orden();
        ejecutar(() -> {
            BigDecimal saldo = pagos.registrarPago(o, BigDecimal.valueOf((Double) spnMonto.getValue()),
                    (MetodoPago) cmbMetodo.getSelectedItem(), txtReferencia.getText());
            txtReferencia.setText("");
            recargar();
            avisar(saldo.signum() == 0 ? "Pago registrado. La orden quedó totalmente pagada."
                    : "Pago registrado. Saldo pendiente: L " + dinero(saldo));
        });
    }

    private void entregar() {
        OrdenResumen o = orden();
        if (o == null) {
            return;
        }
        if (o.getSaldo().signum() > 0
                && !confirmar("La orden tiene saldo pendiente de L " + dinero(o.getSaldo()) + ". ¿Entregar de todas formas?")) {
            return;
        }
        if (ejecutar(() -> ordenes.cambiarEstado(o, EstadoOrden.ENTREGADA))) {
            recargar();
            avisar("Vehículo entregado.");
        }
    }

    @Override
    protected void ordenSeleccionada(OrdenResumen o) {
        ejecutar(() -> llenar(tblFacturas, facturacion.facturas()));
        if (o == null) {
            limpiar(tblPagos);
            return;
        }
        // Importe gravado = mano de obra + repuestos; Subtotal = gravado - descuento; Total = subtotal + ISV
        BigDecimal gravado = o.getTotalManoObra().add(o.getTotalRepuestos());
        BigDecimal subtotal = gravado.subtract(o.getDescuento());
        lblGravado.setText("L " + dinero(gravado));
        lblDescuento.setText((o.getDescuento().signum() > 0 ? "- L " : "L ") + dinero(o.getDescuento()));
        lblSubtotal.setText("L " + dinero(subtotal));
        lblIsvTitulo.setText(subtotal.signum() > 0
                ? "ISV (" + o.getImpuesto().multiply(BigDecimal.valueOf(100))
                        .divide(subtotal, 0, java.math.RoundingMode.HALF_UP) + " %)"
                : "ISV");
        lblIsv.setText("L " + dinero(o.getImpuesto()));
        lblTotalFactura.setText("L " + dinero(o.getTotal()));
        lblTotal.setText("L " + dinero(o.getTotal()));
        lblPagado.setText("L " + dinero(o.getTotalPagado()));
        lblSaldo.setText("L " + dinero(o.getSaldo()));
        lblSaldo.setForeground(o.getSaldo().signum() > 0 ? EstiloUI.ERROR : new java.awt.Color(0x047857));
        spnMonto.setValue(o.getSaldo().max(BigDecimal.ZERO).doubleValue());
        ejecutar(() -> llenar(tblPagos, pagos.pagos(o.getOrdenId())));
    }
}
