package vista;

import conexion.ConexionDB;

import javax.swing.*;
import java.awt.*;

public class FrmMenuPrincipal extends JFrame {

    public FrmMenuPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("AutoTech Manager — Sistema Integral de Gestión de Taller Automotriz");
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(EstiloUI.AZUL_OSCURO);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel lblTitulo = new JLabel("AUTOTECH MANAGER — PANEL PRINCIPAL DE MÓDULOS");
        lblTitulo.setFont(EstiloUI.fuente(Font.BOLD, 20f));
        lblTitulo.setForeground(Color.WHITE);
        pnlHeader.add(lblTitulo, BorderLayout.WEST);

        boolean conectado = ConexionDB.probarConexion();
        JLabel lblEstadoDb = new JLabel(conectado ? "● SQL Server Conectado (AutoTechDB)" : "● Desconectado de BD");
        lblEstadoDb.setFont(EstiloUI.fuente(Font.BOLD, 13f));
        lblEstadoDb.setForeground(conectado ? new Color(0x34D399) : new Color(0xF87171));
        pnlHeader.add(lblEstadoDb, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridLayout(2, 4, 15, 15));
        pnlGrid.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        pnlGrid.setBackground(EstiloUI.FONDO_CLARO);

        pnlGrid.add(crearTarjetaModulo("Módulo 1: Clientes",
                "Registro, edición y baja lógica de propietarios.",
                "Abrir Clientes", () -> new FrmClientes().setVisible(true)));

        pnlGrid.add(crearTarjetaModulo("Módulo 1: Vehículos",
                "Placas, VIN de 17 car., kilometraje y clientes.",
                "Abrir Vehículos", () -> new FrmVehiculos().setVisible(true)));

        pnlGrid.add(crearTarjetaModulo("Módulo 2: Servicios",
                "Catálogo por nivel (1-5), costos polimórficos y garantía.",
                "Abrir Catálogo", () -> new FrmServicios().setVisible(true)));

        pnlGrid.add(crearTarjetaModulo("Módulo 4: Mecánicos",
                "Personal técnico, especialidades y disponibilidad.",
                "Abrir Mecánicos", () -> new FrmMecanicos().setVisible(true)));

        pnlGrid.add(crearTarjetaModulo("Módulo 5: Proveedores",
                "Suministradores por RTN, contactos y catálogo.",
                "Abrir Proveedores", () -> new FrmProveedores().setVisible(true)));

        pnlGrid.add(crearTarjetaModulo("Módulo 5: Inventario y Hilos",
                "Stock, Kardex, MonitorStockMinimo y Concurrencia (2 Hilos).",
                "Abrir Inventario", () -> new FrmInventario().setVisible(true)));

        pnlGrid.add(crearTarjetaModulo("Reportes Administrativos",
                "Vistas SQL: Resumen Órdenes, Stock Bajo, Saldos Facturas.",
                "Abrir Reportes", () -> new FrmReportes().setVisible(true)));

        pnlGrid.add(crearTarjetaModulo("Historial de Vehículos",
                "Expediente clínico separado por placa y cambios de motor.",
                "Abrir Historial", () -> new FrmHistorialVehiculo().setVisible(true)));

        add(pnlGrid, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        JLabel lblFoot = new JLabel("Programación Avanzada II · Segundo Parcial · Módulos 1, 2, 4, 5 y Reportes");
        lblFoot.setForeground(EstiloUI.TEXTO_SUAVE);
        pnlFooter.add(lblFoot);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel crearTarjetaModulo(String titulo, String desc, String textoBoton, Runnable accion) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloUI.BORDE, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(EstiloUI.fuente(Font.BOLD, 14f));
        lblTit.setForeground(EstiloUI.AZUL_OSCURO);

        JLabel lblDesc = new JLabel("<html><p style='width:140px'>" + desc + "</p></html>");
        lblDesc.setFont(EstiloUI.fuente(Font.PLAIN, 12f));
        lblDesc.setForeground(EstiloUI.TEXTO_SUAVE);

        JButton btn = EstiloUI.botonPrimario(textoBoton);
        btn.addActionListener(e -> accion.run());

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmMenuPrincipal().setVisible(true));
    }
}
