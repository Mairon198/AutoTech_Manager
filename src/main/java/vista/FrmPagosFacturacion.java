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
                .ancho(cifras)
                .campo("Monto (L)", spnMonto)
                .ancho(botones(saldo))
                .campo("Método", cmbMetodo)
                .campo("Referencia", txtReferencia)
                .ancho(botones(pagar))
                .ancho(nota("Se permiten pagos parciales; nunca se acepta un monto mayor que el saldo."))
                .ancho(new JLabel(" "))
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
        lblTotal.setText("L " + dinero(o.getTotal()));
        lblPagado.setText("L " + dinero(o.getTotalPagado()));
        lblSaldo.setText("L " + dinero(o.getSaldo()));
        lblSaldo.setForeground(o.getSaldo().signum() > 0 ? EstiloUI.ERROR : new java.awt.Color(0x047857));
        spnMonto.setValue(o.getSaldo().max(BigDecimal.ZERO).doubleValue());
        ejecutar(() -> llenar(tblPagos, pagos.pagos(o.getOrdenId())));
    }
}
