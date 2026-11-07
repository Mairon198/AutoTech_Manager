package dao.impl;

import dao.ProveedorDAO;
import excepciones.PersistenciaException;
import modelo.Proveedor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProveedorDAOImpl extends BaseDAO implements ProveedorDAO {

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("proveedor_id"));
        p.setRtn(rs.getString("rtn"));
        p.setNombre(rs.getString("nombre"));
        p.setContacto(rs.getString("contacto"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setDireccion(rs.getString("direccion"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }

    @Override
    public Integer insertar(Proveedor entidad) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Proveedores (rtn, nombre, contacto, telefono, correo, direccion, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        int id = ejecutarInsertConClave(sql,
                entidad.getRtn(),
                entidad.getNombre(),
                entidad.getContacto(),
                entidad.getTelefono(),
                entidad.getCorreo(),
                entidad.getDireccion(),
                entidad.isActivo());
        entidad.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(Proveedor entidad) throws PersistenciaException {
        String sql = "UPDATE dbo.Proveedores SET rtn = ?, nombre = ?, contacto = ?, telefono = ?, "
                   + "correo = ?, direccion = ?, activo = ? WHERE proveedor_id = ?";
        return ejecutarUpdate(sql,
                entidad.getRtn(),
                entidad.getNombre(),
                entidad.getContacto(),
                entidad.getTelefono(),
                entidad.getCorreo(),
                entidad.getDireccion(),
                entidad.isActivo(),
                entidad.getId()) > 0;
    }

    @Override
    public Optional<Proveedor> buscarPorId(Integer id) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Proveedores WHERE proveedor_id = ?";
        return consultarUno(sql, this::mapear, id);
    }

    @Override
    public Optional<Proveedor> buscarPorRtn(String rtn) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Proveedores WHERE rtn = ?";
        return consultarUno(sql, this::mapear, rtn);
    }

    @Override
    public List<Proveedor> listar() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Proveedores ORDER BY proveedor_id DESC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Proveedor> listarActivos() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Proveedores WHERE activo = 1 ORDER BY nombre ASC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Proveedor> buscarPorCriterio(String filtro) throws PersistenciaException {
        String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        String sql = "SELECT * FROM dbo.Proveedores WHERE nombre LIKE ? OR rtn LIKE ? OR contacto LIKE ? ORDER BY nombre ASC";
        return consultarLista(sql, this::mapear, like, like, like);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        String sql = "UPDATE dbo.Proveedores SET activo = 0 WHERE proveedor_id = ?";
        return ejecutarUpdate(sql, id) > 0;
    }

    @Override
    public boolean existeRtn(String rtn, Integer excluirId) throws PersistenciaException {
        String sql = (excluirId != null && excluirId > 0)
                ? "SELECT COUNT(*) FROM dbo.Proveedores WHERE rtn = ? AND proveedor_id <> ?"
                : "SELECT COUNT(*) FROM dbo.Proveedores WHERE rtn = ?";
        String r = rtn != null ? rtn.trim() : "";
        Optional<Integer> count = (excluirId != null && excluirId > 0)
                ? consultarUno(sql, rs -> rs.getInt(1), r, excluirId)
                : consultarUno(sql, rs -> rs.getInt(1), r);
        return count.map(cnt -> cnt > 0).orElse(false);
    }
}
