package vista;

import conexion.ConexionDB;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Acceso JDBC parametrizado para los seis formularios visuales. */
public final class GuardarFormularios {
    private GuardarFormularios() {}

    private static String dato(String[] a, int n) { return a[n].trim(); }
    private static int entero(String[] a, int n) { return Integer.parseInt(dato(a,n)); }
    private static BigDecimal decimal(String[] a, int n) { return new BigDecimal(dato(a,n).replace(',', '.')); }
    private static String opt(String[] a, int n) { return dato(a,n).isEmpty()?null:dato(a,n); }
    private static boolean si(String[] a,int n) {
        String s=dato(a,n).toLowerCase(java.util.Locale.ROOT);
        if (s.equals("si") || s.equals("sí") || s.equals("true") || s.equals("1")) return true;
        if (s.equals("no") || s.equals("false") || s.equals("0")) return false;
        throw new IllegalArgumentException("Escriba Sí o No en el campo: " + (n+1));
    }
    private static Timestamp fecha(String[] a,int n) {
        String s=dato(a,n).replace('T',' ');
        if (s.length()==10) s+=" 00:00:00";
        if (s.length()==16) s+=":00";
        return Timestamp.valueOf(s);
    }
    private static Timestamp fechaOpcional(String[] a,int n) { return dato(a,n).isEmpty()?null:fecha(a,n); }
    private static int id(PreparedStatement st) throws SQLException {
        try(ResultSet rs=st.executeQuery()) { if(rs.next()) return rs.getInt(1); }
        throw new SQLException("SQL Server no devolvió el ID generado.");
    }
    public static String orden(String[] a) throws Exception {
        if (dato(a,4).isEmpty()) throw new IllegalArgumentException("Los síntomas son obligatorios.");
        try(Connection c=ConexionDB.getConexion(); PreparedStatement ps=c.prepareStatement(
                "INSERT INTO dbo.OrdenesTrabajo (cliente_id,vehiculo_id,kilometraje_ingreso,sintomas,diagnostico_preliminar) " +
                "OUTPUT INSERTED.orden_id VALUES (?,?,?,?,?)")) {
            ps.setInt(1,entero(a,1)); ps.setInt(2,entero(a,2)); ps.setInt(3,entero(a,3));
            ps.setString(4,dato(a,4)); ps.setString(5,opt(a,5));
            int id=id(ps); return "Orden registrada: OT-"+String.format("%06d",id);
        }
    }
    public static String diagnostico(String[] a) throws Exception {
        if(dato(a,2).isEmpty()) throw new IllegalArgumentException("La descripción del diagnóstico es obligatoria.");
        try(Connection c=ConexionDB.getConexion(); PreparedStatement ps=c.prepareStatement(
                "INSERT INTO dbo.Diagnosticos (orden_id,mecanico_id,descripcion,causa_raiz,recomendacion,requiere_cambio_motor) " +
                "OUTPUT INSERTED.diagnostico_id VALUES (?,?,?,?,?,?)")) {
            ps.setInt(1,entero(a,0)); ps.setInt(2,entero(a,1)); ps.setString(3,dato(a,2));
            ps.setString(4,opt(a,3)); ps.setString(5,opt(a,4)); ps.setBoolean(6,si(a,5));
            int id=id(ps);
            return "Diagnóstico #"+id+" guardado. El presupuesto todavía NO se genera: requiere desglose por servicio/repuesto.";
        }
    }
    public static String actividad(String[] a) throws Exception {
        try(Connection c=ConexionDB.getConexion()) {
            c.setAutoCommit(false);
            try {
                int orden=entero(a,0), mecanico=entero(a,1);
                try(PreparedStatement ps=c.prepareStatement(
                        "IF NOT EXISTS(SELECT 1 FROM dbo.AsignacionMecanicos WHERE orden_id=? AND mecanico_id=?) " +
                        "INSERT INTO dbo.AsignacionMecanicos(orden_id,mecanico_id,es_responsable) VALUES (?,?,0)")) {
                    ps.setInt(1,orden); ps.setInt(2,mecanico); ps.setInt(3,orden); ps.setInt(4,mecanico); ps.executeUpdate();
                }
                try(PreparedStatement ps=c.prepareStatement(
                        "INSERT INTO dbo.ActividadesTrabajo (orden_id,mecanico_id,descripcion,horas,estado,inicio,observaciones) " +
                        "OUTPUT INSERTED.actividad_id VALUES (?,?,?,?,?,?,?)")) {
                    ps.setInt(1,orden); ps.setInt(2,mecanico); ps.setString(3,dato(a,2));
                    ps.setBigDecimal(4,dato(a,3).isEmpty()?BigDecimal.ZERO:decimal(a,3));
                    ps.setString(5,dato(a,4).isEmpty()?"PENDIENTE":dato(a,4).toUpperCase());
                    ps.setTimestamp(6,fechaOpcional(a,5)); ps.setString(7,opt(a,6));
                    int id=id(ps); c.commit(); return "Actividad #"+id+" y asignación guardadas.";
                }
            } catch(Exception e) {c.rollback(); throw e;}
        }
    }
    public static String control(String[] a) throws Exception {
        boolean aprobado=si(a,3);
        if(!aprobado && dato(a,4).isEmpty()) throw new IllegalArgumentException("Debe explicar por qué no fue aprobado.");
        try(Connection c=ConexionDB.getConexion(); PreparedStatement ps=c.prepareStatement(
                "INSERT INTO dbo.ControlesCalidad (orden_id,inspector_id,aprobado,checklist,observaciones) " +
                "OUTPUT INSERTED.control_id VALUES (?,?,?,?,?)")) {
            ps.setInt(1,entero(a,0)); ps.setInt(2,entero(a,1)); ps.setBoolean(3,aprobado);
            ps.setString(4,opt(a,2)); ps.setString(5,opt(a,4));
            return "Control de calidad #"+id(ps)+" registrado.";
        }
    }
    public static String motor(String[] a) throws Exception {
        if(!si(a,9)) throw new IllegalArgumentException("Se requiere autorización del cliente.");
        try(Connection c=ConexionDB.getConexion()) {
            // Verificar que el vehículo indicado corresponda a la orden antes de ejecutar el SP.
            try(PreparedStatement q=c.prepareStatement("SELECT vehiculo_id FROM dbo.OrdenesTrabajo WHERE orden_id=?")) {
                q.setInt(1,entero(a,0));
                try(ResultSet rs=q.executeQuery()) {
                    if(!rs.next() || rs.getInt(1)!=entero(a,1))
                        throw new IllegalArgumentException("El vehículo no corresponde a esa orden.");
                }
            }
            try(CallableStatement ps=c.prepareCall("{call dbo.sp_RegistrarCambioMotor(?,?,?,?,?,?,?,?,?,?,?)}")) {
                ps.setInt(1,entero(a,0)); ps.setString(2,dato(a,2)); ps.setInt(3,entero(a,3));
                ps.setString(4,dato(a,4).toUpperCase()); ps.setTimestamp(5,fecha(a,5));
                ps.setTimestamp(6,fecha(a,6)); ps.setString(7,dato(a,7)); ps.setBoolean(8,si(a,8));
                ps.setString(9,opt(a,10)); ps.setString(10,"AUTO-TECH"); ps.registerOutParameter(11,Types.INTEGER);
                ps.execute(); return "Cambio de motor #"+ps.getInt(11)+" registrado.";
            }
        }
    }
    public static String pago(String[] a) throws Exception {
        try(Connection c=ConexionDB.getConexion(); CallableStatement ps=c.prepareCall("{call dbo.sp_RegistrarPago(?,?,?,?,?)}")) {
            ps.setInt(1,entero(a,0)); ps.setBigDecimal(2,decimal(a,2));
            ps.setString(3,dato(a,3).toUpperCase()); ps.setString(4,opt(a,4));
            ps.registerOutParameter(5,Types.DECIMAL); ps.execute();
            return "Pago registrado correctamente. Saldo pendiente: L " + ps.getBigDecimal(5);
        }
    }
    public static String factura(String[] a) throws Exception {
        try(Connection c=ConexionDB.getConexion(); CallableStatement ps=c.prepareCall("{call dbo.sp_GenerarFactura(?,?)}")) {
            ps.setInt(1,entero(a,0)); ps.registerOutParameter(2,Types.INTEGER); ps.execute();
            return "Factura generada #" + ps.getInt(2) + ". Consulte la tabla Facturas para el número FAC.";
        }
    }
}
