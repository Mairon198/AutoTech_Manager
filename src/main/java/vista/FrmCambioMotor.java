package vista;

/**
 * Formulario editable en el diseñador GUI de NetBeans.
 * Los botones son una base visual: conectar Guardar con el servicio/DAO correspondiente.
 */
public class FrmCambioMotor extends javax.swing.JFrame {

    public FrmCambioMotor() {
        initComponents();
        btnLimpiar.addActionListener(evt -> limpiarCampos());
        btnGuardar.addActionListener(evt -> guardarFormulario());
        setSize(880, 657);
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
        lblCampo8 = new javax.swing.JLabel();
        txtCampo8 = new javax.swing.JTextField();
        lblCampo9 = new javax.swing.JLabel();
        txtCampo9 = new javax.swing.JTextField();
        lblCampo10 = new javax.swing.JLabel();
        txtCampo10 = new javax.swing.JTextField();
        lblCampo11 = new javax.swing.JLabel();
        txtCampo11 = new javax.swing.JTextField();
        panelBotones = new javax.swing.JPanel();
        btnLimpiar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AutoTech Manager - Cambio de motor - Toyota Hilux 2015");
        getContentPane().setLayout(new java.awt.BorderLayout());

        lblTitulo.setText("Cambio de motor - Toyota Hilux 2015");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(java.awt.Font.BOLD, 21f));
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 20, 12, 20));
        getContentPane().add(lblTitulo, java.awt.BorderLayout.NORTH);

        panelCampos.setLayout(new java.awt.GridLayout(11, 2, 12, 10));
        lblCampo1.setText("ID de orden");
        panelCampos.add(lblCampo1);
        txtCampo1.setColumns(24);
        panelCampos.add(txtCampo1);
        lblCampo2.setText("ID del vehículo");
        panelCampos.add(lblCampo2);
        txtCampo2.setColumns(24);
        panelCampos.add(txtCampo2);
        lblCampo3.setText("Serie del motor anterior");
        panelCampos.add(lblCampo3);
        txtCampo3.setColumns(24);
        panelCampos.add(txtCampo3);
        lblCampo4.setText("ID del motor nuevo");
        panelCampos.add(lblCampo4);
        txtCampo4.setColumns(24);
        panelCampos.add(txtCampo4);
        lblCampo5.setText("Destino del motor retirado");
        panelCampos.add(lblCampo5);
        txtCampo5.setColumns(24);
        panelCampos.add(txtCampo5);
        lblCampo6.setText("Fecha de desmontaje");
        panelCampos.add(lblCampo6);
        txtCampo6.setColumns(24);
        panelCampos.add(txtCampo6);
        lblCampo7.setText("Fecha de instalación");
        panelCampos.add(lblCampo7);
        txtCampo7.setColumns(24);
        panelCampos.add(txtCampo7);
        lblCampo8.setText("Resultado de pruebas");
        panelCampos.add(lblCampo8);
        txtCampo8.setColumns(24);
        panelCampos.add(txtCampo8);
        lblCampo9.setText("¿Pruebas aprobadas?");
        panelCampos.add(lblCampo9);
        txtCampo9.setColumns(24);
        panelCampos.add(txtCampo9);
        lblCampo10.setText("¿Autorizado por el cliente?");
        panelCampos.add(lblCampo10);
        txtCampo10.setColumns(24);
        panelCampos.add(txtCampo10);
        lblCampo11.setText("Observaciones");
        panelCampos.add(lblCampo11);
        txtCampo11.setColumns(24);
        panelCampos.add(txtCampo11);
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
        txtCampo8.setText("");
        txtCampo9.setText("");
        txtCampo10.setText("");
        txtCampo11.setText("");
    }

    private void guardarFormulario() {
        
        String[] campos = new String[]{txtCampo1.getText(), txtCampo2.getText(), txtCampo3.getText(), txtCampo4.getText(), txtCampo5.getText(), txtCampo6.getText(), txtCampo7.getText(), txtCampo8.getText(), txtCampo9.getText(), txtCampo10.getText(), txtCampo11.getText()};
        btnGuardar.setEnabled(false);
        new javax.swing.SwingWorker<String,Void>() {
            protected String doInBackground() throws Exception { return GuardarFormularios.motor(campos); }
            protected void done() {
                btnGuardar.setEnabled(true);
                try {
                    String mensaje=get();
                    javax.swing.JOptionPane.showMessageDialog(FrmCambioMotor.this, mensaje, "AutoTech Manager",javax.swing.JOptionPane.INFORMATION_MESSAGE);
                } catch(Exception err) {
                    Throwable causa=err instanceof java.util.concurrent.ExecutionException && err.getCause()!=null?err.getCause():err;
                    javax.swing.JOptionPane.showMessageDialog(FrmCambioMotor.this, causa.getMessage(), "Error de guardado",javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

        public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FrmCambioMotor().setVisible(true));
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
    private javax.swing.JLabel lblCampo8;
    private javax.swing.JTextField txtCampo8;
    private javax.swing.JLabel lblCampo9;
    private javax.swing.JTextField txtCampo9;
    private javax.swing.JLabel lblCampo10;
    private javax.swing.JTextField txtCampo10;
    private javax.swing.JLabel lblCampo11;
    private javax.swing.JTextField txtCampo11;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelCampos;
    private javax.swing.JScrollPane scrollCampos;
    // End of variables declaration//GEN-END:variables
}
