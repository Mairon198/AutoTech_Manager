package dao.impl;

import dao.VehiculoDAO;
import excepciones.PersistenciaException;
import modelo.Cliente;
import modelo.Vehiculo;
import modelo.enums.TipoCombustible;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class VehiculoDAOImpl extends BaseDAO implements VehiculoDAO {

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setId(rs.getInt("vehiculo_id"));
        v.setPlaca(rs.getString("placa"));
        v.setVin(rs.getString("vin"));
        v.setMarca(rs.getString("marca"));
        v.setModelo(rs.getString("modelo"));
        v.setAnio(rs.getInt("anio"));
        v.setColor(rs.getString("color"));
        v.setKilometraje(rs.getInt("kilometraje"));

        String combStr = rs.getString("combustible");
        if (combStr != null) {
            try {
                v.setCombustible(TipoCombustible.valueOf(combStr.toUpperCase().trim()));
            } catch (IllegalArgumentException e) {
                v.setCombustible(TipoCombustible.GASOLINA);
            }
        } else {
            v.setCombustible(TipoCombustible.GASOLINA);
        }

        v.setSerieMotorActual(rs.getString("serie_motor_actual"));
        v.setActivo(rs.getBoolean("activo"));

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            v.setFechaCreacion(ts.toLocalDateTime());
        }

        Cliente c = new Cliente();
        c.setId(rs.getInt("cliente_id"));
        try {
            c.setIdentidad(rs.getString("identificacion"));
            String nombreCli = rs.getString("nombre_cliente");
            if (nombreCli != null) {
                String[] partes = nombreCli.trim().split("\\s+", 2);
                c.setNombre(partes[0]);
                c.setApellido(partes.length > 1 ? partes[1] : "");
            }
            c.setTelefono(rs.getString("telefono_cliente"));
        } catch (SQLException ignored) {

        }
        v.setCliente(c);

        return v;
    }

    private static final String SELECT_BASE =
            "SELECT v.vehiculo_id, v.cliente_id, v.placa, v.vin, v.marca, v.modelo, v.anio, "
          + "v.color, v.kilometraje, v.combustible, v.serie_motor_actual, v.activo, v.fecha_registro, "
          + "c.identificacion, c.nombre AS nombre_cliente, c.telefono AS telefono_cliente "
          + "FROM dbo.Vehiculos v "
          + "INNER JOIN dbo.Clientes c ON c.cliente_id = v.cliente_id ";

    @Override
    public Integer insertar(Vehiculo entidad) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Vehiculos (cliente_id, placa, vin, marca, modelo, anio, color, "
                   + "kilometraje, combustible, serie_motor_actual, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id = ejecutarInsertConClave(sql,
                entidad.getCliente() != null ? entidad.getCliente().getId() : null,
                entidad.getPlaca() != null ? entidad.getPlaca().toUpperCase().trim() : null,
                entidad.getVin() != null ? entidad.getVin().toUpperCase().trim() : null,
                entidad.getMarca(),
                entidad.getModelo(),
                entidad.getAnio(),
                entidad.getColor(),
                entidad.getKilometraje(),
                entidad.getCombustible() != null ? entidad.getCombustible().name() : "GASOLINA",
                entidad.getSerieMotorActual(),
                entidad.isActivo());
        entidad.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(Vehiculo entidad) throws PersistenciaException {
        String sql = "UPDATE dbo.Vehiculos SET cliente_id = ?, placa = ?, vin = ?, marca = ?, modelo = ?, "
                   + "anio = ?, color = ?, kilometraje = ?, combustible = ?, serie_motor_actual = ?, activo = ? "
                   + "WHERE vehiculo_id = ?";
        return ejecutarUpdate(sql,
                entidad.getCliente() != null ? entidad.getCliente().getId() : null,
                entidad.getPlaca() != null ? entidad.getPlaca().toUpperCase().trim() : null,
                entidad.getVin() != null ? entidad.getVin().toUpperCase().trim() : null,
                entidad.getMarca(),
                entidad.getModelo(),
                entidad.getAnio(),
                entidad.getColor(),
                entidad.getKilometraje(),
                entidad.getCombustible() != null ? entidad.getCombustible().name() : "GASOLINA",
                entidad.getSerieMotorActual(),
                entidad.isActivo(),
                entidad.getId()) > 0;
    }

    @Override
    public Optional<Vehiculo> buscarPorId(Integer id) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE v.vehiculo_id = ?";
        return consultarUno(sql, this::mapear, id);
    }

    @Override
    public Optional<Vehiculo> buscarPorPlaca(String placa) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE v.placa = ?";
        return consultarUno(sql, this::mapear, placa != null ? placa.toUpperCase().trim() : "");
    }

    @Override
    public Optional<Vehiculo> buscarPorVin(String vin) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE v.vin = ?";
        return consultarUno(sql, this::mapear, vin != null ? vin.toUpperCase().trim() : "");
    }

    @Override
    public List<Vehiculo> listar() throws PersistenciaException {
        String sql = SELECT_BASE + "ORDER BY v.vehiculo_id DESC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Vehiculo> listarActivos() throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE v.activo = 1 ORDER BY v.marca, v.modelo";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Vehiculo> listarPorCliente(int clienteId) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE v.cliente_id = ? ORDER BY v.vehiculo_id DESC";
        return consultarLista(sql, this::mapear, clienteId);
    }

    @Override
    public List<Vehiculo> buscarPorCriterio(String filtro) throws PersistenciaException {
        String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        String sql = SELECT_BASE + "WHERE v.placa LIKE ? OR v.vin LIKE ? OR v.marca LIKE ? OR v.modelo LIKE ? OR c.nombre LIKE ? "
                   + "ORDER BY v.vehiculo_id DESC";
        return consultarLista(sql, this::mapear, like, like, like, like, like);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        String sql = "UPDATE dbo.Vehiculos SET activo = 0 WHERE vehiculo_id = ?";
        return ejecutarUpdate(sql, id) > 0;
    }

    @Override
    public boolean existePlaca(String placa, Integer excluirId) throws PersistenciaException {
        String sql = (excluirId != null && excluirId > 0)
                ? "SELECT COUNT(*) FROM dbo.Vehiculos WHERE placa = ? AND vehiculo_id <> ?"
                : "SELECT COUNT(*) FROM dbo.Vehiculos WHERE placa = ?";
        String p = placa != null ? placa.toUpperCase().trim() : "";
        Optional<Integer> count = (excluirId != null && excluirId > 0)
                ? consultarUno(sql, rs -> rs.getInt(1), p, excluirId)
                : consultarUno(sql, rs -> rs.getInt(1), p);
        return count.map(c -> c > 0).orElse(false);
    }

    @Override
    public boolean existeVin(String vin, Integer excluirId) throws PersistenciaException {
        String sql = (excluirId != null && excluirId > 0)
                ? "SELECT COUNT(*) FROM dbo.Vehiculos WHERE vin = ? AND vehiculo_id <> ?"
                : "SELECT COUNT(*) FROM dbo.Vehiculos WHERE vin = ?";
        String v = vin != null ? vin.toUpperCase().trim() : "";
        Optional<Integer> count = (excluirId != null && excluirId > 0)
                ? consultarUno(sql, rs -> rs.getInt(1), v, excluirId)
                : consultarUno(sql, rs -> rs.getInt(1), v);
        return count.map(c -> c > 0).orElse(false);
    }
}
