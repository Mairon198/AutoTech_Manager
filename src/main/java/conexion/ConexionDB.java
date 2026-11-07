package conexion;

import excepciones.PersistenciaException;
import util.Configuracion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionDB {

    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String URL = Configuracion.get("db.url");
    private static final String USER = Configuracion.get("db.user");
    private static final String PASSWORD = Configuracion.get("db.password");

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("No se encontró el driver mssql-jdbc. Revise la dependencia en pom.xml.");
        }
    }

    private ConexionDB() {
    }

    public static Connection getConexion() throws PersistenciaException {
        if (!Configuracion.estaCargado() || URL == null) {
            throw new PersistenciaException("No se encontró config.properties en la carpeta del proyecto. "
                    + "Copie config.properties.example con el nombre config.properties y escriba sus datos de conexión.");
        }
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al conectar con SQL Server: " + e.getMessage(), e);
        }
    }

    public static boolean probarConexion() {
        try (Connection cn = getConexion()) {
            System.out.println("-> Conexión exitosa a " + cn.getCatalog());
            return true;
        } catch (PersistenciaException | SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public static void rollback(Connection cn) {
        if (cn != null) {
            try {
                cn.rollback();
            } catch (SQLException e) {
                System.err.println("Error al revertir la transacción: " + e.getMessage());
            }
        }
    }
}
