package vista;

/**
 * Formulario editable en el diseñador GUI de NetBeans.
 * Los botones son una base visual: conectar Guardar con el servicio/DAO correspondiente.
 */
public class FrmPagosFacturacion extends javax.swing.JFrame {

    public FrmPagosFacturacion() {
        initComponents();
        btnLimpiar.addActionListener(evt -> limpiarCampos());
        btnGuardar.addActionListener(evt -> guardarFormulario());
        setSize(880, 469);
        setLocationRelativeTo(null);
        panelCampos.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTitulo = new javax.swing.JLabel();
        scrollCampos = new javax.swing.JScrollPane();
        panelCampos = new javax.swing.JPanel();
        lblCampo1 = new javax.swing.JLabel();
        txtCampo1 = new javax.swing.JTextField();
        lblCampo2 = new javax.swing.JLabel();
        txtCampo2 = new javax.swing.JTextField();
        lblCampo3 = new javax.swing.JLabel();
        txtCampo3 = new javax.swing.JTextField();
        lblCampo4 = new javax.swing.JLabel();
        txtCampo4 = new javax.swing.JTextField();
        lblCampo5 = new javax.swing.JLabel();
        txtCampo5 = new javax.swing.JTextField();
        lblCampo6 = new javax.swing.JLabel();
        txtCampo6 = new javax.swing.JTextField();
        lblCampo7 = new javax.swing.JLabel();
        txtCampo7 = new javax.swing.JTextField();
        panelBotones = new javax.swing.JPanel();
        btnLimpiar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AutoTech Manager - Pagos y facturación");
        getContentPane().setLayout(new java.awt.BorderLayout());

        lblTitulo.setText("Pagos y facturación");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(java.awt.Font.BOLD, 21f));
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 20, 12, 20));
        getContentPane().add(lblTitulo, java.awt.BorderLayout.NORTH);

        panelCampos.setLayout(new java.awt.GridLayout(7, 2, 12, 10));
        lblCampo1.setText("ID de orden");
        panelCampos.add(lblCampo1);
        txtCampo1.setColumns(24);
        panelCampos.add(txtCampo1);
        lblCampo2.setText("Total de la orden");
        panelCampos.add(lblCampo2);
        txtCampo2.setColumns(24);
        panelCampos.add(txtCampo2);
        lblCampo3.setText("Monto del pago");
        panelCampos.add(lblCampo3);
        txtCampo3.setColumns(24);
        panelCampos.add(txtCampo3);
        lblCampo4.setText("Método de pago");
        panelCampos.add(lblCampo4);
        txtCampo4.setColumns(24);
        panelCampos.add(txtCampo4);
        lblCampo5.setText("Referencia / comprobante");
        panelCampos.add(lblCampo5);
        txtCampo5.setColumns(24);
        panelCampos.add(txtCampo5);
        lblCampo6.setText("Saldo pendiente");
        panelCampos.add(lblCampo6);
        txtCampo6.setColumns(24);
        panelCampos.add(txtCampo6);
        lblCampo7.setText("Número de factura");
        panelCampos.add(lblCampo7);
        txtCampo7.setColumns(24);
        panelCampos.add(txtCampo7);
        scrollCampos.setViewportView(panelCampos);
        getContentPane().add(scrollCampos, java.awt.BorderLayout.CENTER);

        panelBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        btnLimpiar.setText("Limpiar");
        panelBotones.add(btnLimpiar);
        btnGuardar.setText("Validar / Guardar");
        panelBotones.add(btnGuardar);
        getContentPane().add(panelBotones, java.awt.BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents

    private void limpiarCampos() {
        txtCampo1.setText("");
        txtCampo2.setText("");
        txtCampo3.setText("");
        txtCampo4.setText("");
        txtCampo5.setText("");
        txtCampo6.setText("");
        txtCampo7.setText("");
    }

    private void guardarFormulario() {
        int opcion=javax.swing.JOptionPane.showOptionDialog(this,"¿Qué desea registrar?", "Pagos y facturación", javax.swing.JOptionPane.DEFAULT_OPTION,javax.swing.JOptionPane.QUESTION_MESSAGE,null,new String[]{"Registrar pago","Generar factura","Cancelar"},"Registrar pago");
        if(opcion<0 || opcion==2) return;
        String[] campos = new String[]{txtCampo1.getText(), txtCampo2.getText(), txtCampo3.getText(), txtCampo4.getText(), txtCampo5.getText(), txtCampo6.getText(), txtCampo7.getText()};
        btnGuardar.setEnabled(false);
        new javax.swing.SwingWorker<String,Void>() {
            protected String doInBackground() throws Exception { return opcion==0?GuardarFormularios.pago(campos):GuardarFormularios.factura(campos); }
            protected void done() {
                btnGuardar.setEnabled(true);
                try {
                    String mensaje=get();
                    javax.swing.JOptionPane.showMessageDialog(FrmPagosFacturacion.this, mensaje, "AutoTech Manager",javax.swing.JOptionPane.INFORMATION_MESSAGE);
                } catch(Exception err) {
                    Throwable causa=err instanceof java.util.concurrent.ExecutionException && err.getCause()!=null?err.getCause():err;
                    javax.swing.JOptionPane.showMessageDialog(FrmPagosFacturacion.this, causa.getMessage(), "Error de guardado",javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

        public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FrmPagosFacturacion().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JLabel lblCampo1;
    private javax.swing.JTextField txtCampo1;
    private javax.swing.JLabel lblCampo2;
    private javax.swing.JTextField txtCampo2;
    private javax.swing.JLabel lblCampo3;
    private javax.swing.JTextField txtCampo3;
    private javax.swing.JLabel lblCampo4;
    private javax.swing.JTextField txtCampo4;
    private javax.swing.JLabel lblCampo5;
    private javax.swing.JTextField txtCampo5;
    private javax.swing.JLabel lblCampo6;
    private javax.swing.JTextField txtCampo6;
    private javax.swing.JLabel lblCampo7;
    private javax.swing.JTextField txtCampo7;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelCampos;
    private javax.swing.JScrollPane scrollCampos;
    // End of variables declaration//GEN-END:variables
}
