package dao.impl;

import dao.RepuestoDAO;
import excepciones.PersistenciaException;
import modelo.Proveedor;
import modelo.Repuesto;
import modelo.enums.CategoriaRepuesto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RepuestoDAOImpl extends BaseDAO implements RepuestoDAO {

    private Repuesto mapear(ResultSet rs) throws SQLException {
        Repuesto r = new Repuesto();
        r.setId(rs.getInt("repuesto_id"));
        r.setCodigo(rs.getString("codigo"));
        r.setNombre(rs.getString("nombre"));

        String catStr = rs.getString("categoria");
        if (catStr != null) {
            try {
                r.setCategoria(CategoriaRepuesto.valueOf(catStr.toUpperCase().trim()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        r.setMarca(rs.getString("marca"));
        r.setStock(rs.getInt("stock_actual"));
        r.setStockMinimo(rs.getInt("stock_minimo"));
        r.setCostoUnitario(rs.getBigDecimal("costo_compra"));
        r.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        r.setUbicacion(rs.getString("ubicacion"));
        r.setActivo(rs.getBoolean("activo"));

        int provId = rs.getInt("proveedor_id");
        if (!rs.wasNull() && provId > 0) {
            Proveedor p = new Proveedor();
            p.setId(provId);
            try {
                p.setNombre(rs.getString("nombre_proveedor"));
                p.setTelefono(rs.getString("telefono_proveedor"));
            } catch (SQLException ignored) {
            }
            r.setProveedor(p);
        }
        return r;
    }

    private static final String SELECT_BASE =
            "SELECT r.repuesto_id, r.codigo, r.nombre, r.categoria, r.marca, r.proveedor_id, "
          + "r.stock_actual, r.stock_minimo, r.costo_compra, r.precio_venta, r.ubicacion, r.activo, "
          + "p.nombre AS nombre_proveedor, p.telefono AS telefono_proveedor "
          + "FROM dbo.Repuestos r "
          + "LEFT JOIN dbo.Proveedores p ON p.proveedor_id = r.proveedor_id ";

    @Override
    public Integer insertar(Repuesto entidad) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Repuestos (codigo, nombre, categoria, marca, proveedor_id, "
                   + "stock_actual, stock_minimo, costo_compra, precio_venta, ubicacion, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id = ejecutarInsertConClave(sql,
                entidad.getCodigo(),
                entidad.getNombre(),
                entidad.getCategoria() != null ? entidad.getCategoria().name() : "OTRO",
                entidad.getMarca(),
                entidad.getProveedor() != null && entidad.getProveedor().getId() > 0 ? entidad.getProveedor().getId() : null,
                entidad.getStock(),
                entidad.getStockMinimo(),
                entidad.getCostoUnitario(),
                entidad.getPrecioVenta(),
                entidad.getUbicacion(),
                entidad.isActivo());
        entidad.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(Repuesto entidad) throws PersistenciaException {
        String sql = "UPDATE dbo.Repuestos SET codigo = ?, nombre = ?, categoria = ?, marca = ?, "
                   + "proveedor_id = ?, stock_actual = ?, stock_minimo = ?, costo_compra = ?, "
                   + "precio_venta = ?, ubicacion = ?, activo = ? WHERE repuesto_id = ?";
        return ejecutarUpdate(sql,
                entidad.getCodigo(),
                entidad.getNombre(),
                entidad.getCategoria() != null ? entidad.getCategoria().name() : "OTRO",
                entidad.getMarca(),
                entidad.getProveedor() != null && entidad.getProveedor().getId() > 0 ? entidad.getProveedor().getId() : null,
                entidad.getStock(),
                entidad.getStockMinimo(),
                entidad.getCostoUnitario(),
                entidad.getPrecioVenta(),
                entidad.getUbicacion(),
                entidad.isActivo(),
                entidad.getId()) > 0;
    }

    @Override
    public Optional<Repuesto> buscarPorId(Integer id) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE r.repuesto_id = ?";
        return consultarUno(sql, this::mapear, id);
    }

    @Override
    public Optional<Repuesto> buscarPorCodigo(String codigo) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE r.codigo = ?";
        return consultarUno(sql, this::mapear, codigo);
    }

    @Override
    public List<Repuesto> listar() throws PersistenciaException {
        String sql = SELECT_BASE + "ORDER BY r.repuesto_id DESC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Repuesto> listarActivos() throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE r.activo = 1 ORDER BY r.nombre ASC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Repuesto> listarBajoStockMinimo() throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE r.activo = 1 AND r.stock_actual <= r.stock_minimo ORDER BY r.stock_actual ASC";
        return consultarLista(sql, this::mapear);
    }

    @Override
    public List<Repuesto> listarPorCategoria(CategoriaRepuesto categoria) throws PersistenciaException {
        String sql = SELECT_BASE + "WHERE r.activo = 1 AND r.categoria = ? ORDER BY r.nombre ASC";
        return consultarLista(sql, this::mapear, categoria.name());
    }

    @Override
    public List<Repuesto> buscarPorCriterio(String filtro) throws PersistenciaException {
        String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        String sql = SELECT_BASE + "WHERE r.nombre LIKE ? OR r.codigo LIKE ? OR r.marca LIKE ? ORDER BY r.nombre ASC";
        return consultarLista(sql, this::mapear, like, like, like);
    }

    @Override
    public boolean actualizarStock(int repuestoId, int nuevoStock) throws PersistenciaException {
        String sql = "UPDATE dbo.Repuestos SET stock_actual = ? WHERE repuesto_id = ?";
        return ejecutarUpdate(sql, nuevoStock, repuestoId) > 0;
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        String sql = "UPDATE dbo.Repuestos SET activo = 0 WHERE repuesto_id = ?";
        return ejecutarUpdate(sql, id) > 0;
    }

    @Override
    public boolean existeCodigo(String codigo, Integer excluirId) throws PersistenciaException {
        String sql = (excluirId != null && excluirId > 0)
                ? "SELECT COUNT(*) FROM dbo.Repuestos WHERE codigo = ? AND repuesto_id <> ?"
                : "SELECT COUNT(*) FROM dbo.Repuestos WHERE codigo = ?";
        String c = codigo != null ? codigo.trim() : "";
        Optional<Integer> count = (excluirId != null && excluirId > 0)
                ? consultarUno(sql, rs -> rs.getInt(1), c, excluirId)
                : consultarUno(sql, rs -> rs.getInt(1), c);
        return count.map(cnt -> cnt > 0).orElse(false);
    }
}
