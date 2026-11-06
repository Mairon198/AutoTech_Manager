package dao.impl;

import conexion.ConexionDB;
import excepciones.PersistenciaException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseDAO {

    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface OperacionTransaccional<R> {
        R ejecutar(Connection cn) throws Exception;
    }

    @FunctionalInterface
    public interface AccionTransaccional {
        void ejecutar(Connection cn) throws Exception;
    }

    protected Connection obtenerConexion() throws PersistenciaException {
        return ConexionDB.getConexion();
    }

    public <R> R ejecutarEnTransaccion(OperacionTransaccional<R> operacion) throws PersistenciaException {
        Connection cn = null;
        boolean originalAutoCommit = true;
        try {
            cn = obtenerConexion();
            originalAutoCommit = cn.getAutoCommit();
            cn.setAutoCommit(false);

            R resultado = operacion.ejecutar(cn);

            cn.commit();
            return resultado;
        } catch (Exception ex) {
            ConexionDB.rollback(cn);
            if (ex instanceof PersistenciaException pe) {
                throw pe;
            }
            throw new PersistenciaException("Error en transacción: " + ex.getMessage(), ex);
        } finally {
            if (cn != null) {
                try {
                    cn.setAutoCommit(originalAutoCommit);
                    cn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión transaccional: " + e.getMessage());
                }
            }
        }
    }

    public void ejecutarEnTransaccion(AccionTransaccional accion) throws PersistenciaException {
        ejecutarEnTransaccion(cn -> {
            accion.ejecutar(cn);
            return null;
        });
    }

    public int ejecutarInsertConClave(String sql, Object... params) throws PersistenciaException {
        try (Connection cn = obtenerConexion()) {
            return ejecutarInsertConClave(cn, sql, params);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al ejecutar INSERT: " + e.getMessage(), e);
        }
    }

    public int ejecutarInsertConClave(Connection cn, String sql, Object... params) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParametros(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al insertar con clave generada: " + e.getMessage(), e);
        }
    }

    public int ejecutarUpdate(String sql, Object... params) throws PersistenciaException {
        try (Connection cn = obtenerConexion()) {
            return ejecutarUpdate(cn, sql, params);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al ejecutar UPDATE/DELETE: " + e.getMessage(), e);
        }
    }

    public int ejecutarUpdate(Connection cn, String sql, Object... params) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            setParametros(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al ejecutar comando SQL: " + e.getMessage(), e);
        }
    }

    public <T> Optional<T> consultarUno(String sql, RowMapper<T> mapper, Object... params) throws PersistenciaException {
        try (Connection cn = obtenerConexion()) {
            return consultarUno(cn, sql, mapper, params);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar registro: " + e.getMessage(), e);
        }
    }

    public <T> Optional<T> consultarUno(Connection cn, String sql, RowMapper<T> mapper, Object... params) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            setParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapper.mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al mapear consulta individual: " + e.getMessage(), e);
        }
    }

    public <T> List<T> consultarLista(String sql, RowMapper<T> mapper, Object... params) throws PersistenciaException {
        try (Connection cn = obtenerConexion()) {
            return consultarLista(cn, sql, mapper, params);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar registros: " + e.getMessage(), e);
        }
    }

    public <T> List<T> consultarLista(Connection cn, String sql, RowMapper<T> mapper, Object... params) throws PersistenciaException {
        List<T> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            setParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapper.mapRow(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al mapear lista de registros: " + e.getMessage(), e);
        }
    }

    protected void setParametros(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            int idx = i + 1;
            if (p == null) {
                ps.setNull(idx, java.sql.Types.NULL);
            } else if (p instanceof String s) {
                ps.setString(idx, s);
            } else if (p instanceof Integer num) {
                ps.setInt(idx, num);
            } else if (p instanceof Long num) {
                ps.setLong(idx, num);
            } else if (p instanceof Double num) {
                ps.setDouble(idx, num);
            } else if (p instanceof BigDecimal bd) {
                ps.setBigDecimal(idx, bd);
            } else if (p instanceof Boolean b) {
                ps.setBoolean(idx, b);
            } else if (p instanceof LocalDate ld) {
                ps.setDate(idx, Date.valueOf(ld));
            } else if (p instanceof LocalDateTime ldt) {
                ps.setTimestamp(idx, Timestamp.valueOf(ldt));
            } else if (p instanceof Enum<?> en) {
                ps.setString(idx, en.name());
            } else {
                ps.setObject(idx, p);
            }
        }
    }
}
