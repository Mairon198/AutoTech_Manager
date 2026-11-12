package reportes;

import conexion.ConexionDB;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;


public class GeneradorReportes {

    // Muestra el reporte en una ventana, desde ahi tambien se puede imprimir o guardar
    public void mostrarReporte(String nombre) {
        try {
            JasperPrint jprint = llenarReporte(nombre);
            JasperViewer visor = new JasperViewer(jprint, false);
            visor.setTitle("Reporte de " + nombre);
            visor.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar el reporte: " + e.getMessage());
        }
    }

    public void imprimirReporte(String nombre) {
        try {
            JasperPrint jprint = llenarReporte(nombre);
            JasperPrintManager.printReport(jprint, true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al imprimir el reporte: " + e.getMessage());
        }
    }

    private JasperPrint llenarReporte(String nombre) throws Exception {
        InputStream diseno = getClass().getResourceAsStream("/reportes/" + nombre + ".jrxml");
        if (diseno == null) {
            throw new Exception("No se encontro el reporte " + nombre + ".jrxml");
        }

        JasperReport reporte;
        try {
            reporte = JasperCompileManager.compileReport(diseno);
        } catch (JRException e) {
            URL compilado = getClass().getResource("/reportes/" + nombre + ".jasper");
            if (compilado == null) {
                throw e;
            }
            reporte = (JasperReport) JRLoader.loadObject(compilado);
        }

        Connection con = ConexionDB.getConexion();
        JasperPrint jprint = JasperFillManager.fillReport(reporte, new HashMap<>(), con);
        con.close();
        return jprint;
    }
}
