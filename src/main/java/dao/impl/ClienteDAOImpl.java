package dao.impl;

import dao.ClienteDAO;
import excepciones.PersistenciaException;
import modelo.Cliente;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class ClienteDAOImpl extends BaseDAO implements ClienteDAO {

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("cliente_id"));
        c.setIdentidad(rs.getString("identificacion"));

        String nombreCompleto = rs.getString("nombre");
        if (nombreCompleto != null) {
            String[] partes = nombreCompleto.trim().split("\\s+", 2);
            c.setNombre(partes[0]);
            c.setApellido(partes.length > 1 ? partes[1] : "");
        }
        c.setTelefono(rs.getString("telefono"));
        c.setCorreo(rs.getString("correo"));
        c.setDireccion(rs.getString("direccion"));
        c.setTipoCliente(rs.getString("tipo_cliente"));
        c.setActivo(rs.getBoolean("activo"));

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            c.setFechaCreacion(ts.toLocalDateTime());
        }
        return c;
    }

    @Override
    public Integer insertar(Cliente entidad) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Clientes (identificacion, nombre, telefono, correo, direccion, tipo_cliente, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String nombreGuardar = entidad.getNombreCompleto().trim();
        int id = ejecutarInsertConClave(sql,
                entidad.getIdentidad(),
                nombreGuardar,
                entidad.getTelefono(),
                entidad.getCorreo(),
                entidad.getDireccion(),
                entidad.getTipoCliente() != null ? entidad.getTipoCliente() : "PARTICULAR",
                entidad.isActivo());
        entidad.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(Cliente entidad) throws PersistenciaException {
        String sql = "UPDATE dbo.Clientes SET identificacion = ?, nombre = ?, telefono = ?, correo = ?, "
                   + "direccion = ?, tipo_cliente = ?, activo = ? WHERE cliente_id = ?";
        String nombreGuardar = entidad.getNombreCompleto().trim();
        return ejecutarUpdate(sql,
                entidad.getIdentidad(),
                nombreGuardar,
                entidad.getTelefono(),
                entidad.getCorreo(),
                entidad.getDireccion(),
                entidad.getTipoCliente() != null ? entidad.getTipoCliente() : "PARTICULAR",
                entidad.isActivo(),
                entidad.getId()) > 0;
    }

    @Override
    public Optional<Cliente> buscarPorId(Integer id) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Clientes WHERE cliente_id = ?";
        return consultarUno(sql, this::mapear, id);
    }

    @Override
    public Optional<Cliente> buscarPorIdentidad(String identidad) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Clientes WHERE identificacion = ?";
        return consultarUno(sql, this::mapear, identidad);
    }

    @Override
    public List<Cliente> listar() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Clientes ORDER BY cliente_id DESC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Cliente> listarActivos() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Clientes WHERE activo = 1 ORDER BY nombre ASC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Cliente> buscarPorCriterio(String filtro) throws PersistenciaException {
        String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        String sql = "SELECT * FROM dbo.Clientes WHERE nombre LIKE ? OR identificacion LIKE ? OR telefono LIKE ? ORDER BY nombre ASC";
        return consultarLista(sql, this::mapear, like, like, like);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        String sql = "UPDATE dbo.Clientes SET activo = 0 WHERE cliente_id = ?";
        return ejecutarUpdate(sql, id) > 0;
    }

    @Override
    public boolean existeIdentidad(String identidad, Integer excluirId) throws PersistenciaException {
        String sql = (excluirId != null && excluirId > 0)
                ? "SELECT COUNT(*) FROM dbo.Clientes WHERE identificacion = ? AND cliente_id <> ?"
                : "SELECT COUNT(*) FROM dbo.Clientes WHERE identificacion = ?";
        Optional<Integer> count = (excluirId != null && excluirId > 0)
                ? consultarUno(sql, rs -> rs.getInt(1), identidad, excluirId)
                : consultarUno(sql, rs -> rs.getInt(1), identidad);
        return count.map(c -> c > 0).orElse(false);
    }
}
