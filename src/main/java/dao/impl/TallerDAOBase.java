package dao.impl;

import excepciones.PersistenciaException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.TablaResultado;

/**
 * Base de los DAO del módulo de órdenes: agrega a BaseDAO la lectura de
 * consultas como tabla y el manejo de errores de los procedimientos almacenados.
 */
public abstract class TallerDAOBase extends BaseDAO {

    /** Convierte una SQLException en PersistenciaException conservando el mensaje de negocio (THROW 5xxxx). */
    protected PersistenciaException error(String contexto, SQLException e) {
        if (e.getErrorCode() >= 50000) {
            return new PersistenciaException(e.getMessage(), e);   // regla de negocio validada por SQL Server
        }
        return new PersistenciaException(contexto + ": " + e.getMessage(), e);
    }

    /** Ejecuta un SELECT y lo devuelve como tabla (columnas con nombres legibles). */
    protected TablaResultado tabla(String sql, Object... params) throws PersistenciaException {
        try (Connection cn = obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            setParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int n = meta.getColumnCount();
                List<String> columnas = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                    String c = meta.getColumnLabel(i).replace('_', ' ');
                    columnas.add(Character.toUpperCase(c.charAt(0)) + c.substring(1));
                }
                List<Object[]> filas = new ArrayList<>();
                while (rs.next()) {
                    Object[] f = new Object[n];
                    for (int i = 1; i <= n; i++) {
                        f[i - 1] = rs.getObject(i);
                    }
                    filas.add(f);
                }
                return new TablaResultado(columnas, filas);
            }
        } catch (SQLException e) {
            throw error("Error al consultar", e);
        }
    }

    /** Prepara la llamada a un procedimiento: "{call dbo.nombre(?,?,...)}". */
    protected CallableStatement llamada(Connection cn, String procedimiento, int parametros) throws SQLException {
        StringBuilder sb = new StringBuilder("{call dbo.").append(procedimiento).append('(');
        for (int i = 0; i < parametros; i++) {
            sb.append(i == 0 ? "?" : ",?");
        }
        return cn.prepareCall(sb.append(")}").toString());
    }
}
