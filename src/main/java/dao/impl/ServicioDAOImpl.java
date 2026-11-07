package dao.impl;

import dao.ServicioDAO;
import excepciones.PersistenciaException;
import modelo.Repuesto;
import modelo.Servicio;
import modelo.ServicioFactory;
import modelo.enums.CategoriaRepuesto;
import modelo.enums.NivelComplejidad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ServicioDAOImpl extends BaseDAO implements ServicioDAO {

    private Servicio mapear(ResultSet rs) throws SQLException {
        int nivel = rs.getInt("nivel_complejidad");
        Servicio s = ServicioFactory.crear(nivel);
        s.setId(rs.getInt("servicio_id"));
        s.setCodigo(rs.getString("codigo"));
        s.setNombre(rs.getString("nombre"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setCostoManoObra(rs.getBigDecimal("costo_mano_obra"));
        s.setTiempoEstimadoHoras(rs.getDouble("tiempo_estimado_horas"));
        s.setActivo(rs.getBoolean("activo"));
        return s;
    }

    private Repuesto mapearRepuestoAsociado(ResultSet rs) throws SQLException {
        Repuesto r = new Repuesto();
        r.setId(rs.getInt("repuesto_id"));
        r.setCodigo(rs.getString("codigo"));
        r.setNombre(rs.getString("nombre"));
        r.setMarca(rs.getString("marca"));
        r.setStock(rs.getInt("stock_actual"));
        r.setStockMinimo(rs.getInt("stock_minimo"));
        r.setCostoUnitario(rs.getBigDecimal("costo_compra"));
        r.setPrecioVenta(rs.getBigDecimal("precio_venta"));

        String catStr = rs.getString("categoria");
        if (catStr != null) {
            try {
                r.setCategoria(CategoriaRepuesto.valueOf(catStr.toUpperCase().trim()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return r;
    }

    @Override
    public Integer insertar(Servicio entidad) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Servicios (codigo, nombre, descripcion, nivel_complejidad, "
                   + "categoria_complejidad, costo_mano_obra, tiempo_estimado_horas, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int id = ejecutarInsertConClave(sql,
                entidad.getCodigo(),
                entidad.getNombre(),
                entidad.getDescripcion(),
                entidad.getNivel().getNivel(),
                entidad.getNivel().getCategoria(),
                entidad.getCostoManoObra(),
                entidad.getTiempoEstimadoHoras(),
                entidad.isActivo());
        entidad.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(Servicio entidad) throws PersistenciaException {
        String sql = "UPDATE dbo.Servicios SET codigo = ?, nombre = ?, descripcion = ?, nivel_complejidad = ?, "
                   + "categoria_complejidad = ?, costo_mano_obra = ?, tiempo_estimado_horas = ?, activo = ? "
                   + "WHERE servicio_id = ?";
        return ejecutarUpdate(sql,
                entidad.getCodigo(),
                entidad.getNombre(),
                entidad.getDescripcion(),
                entidad.getNivel().getNivel(),
                entidad.getNivel().getCategoria(),
                entidad.getCostoManoObra(),
                entidad.getTiempoEstimadoHoras(),
                entidad.isActivo(),
                entidad.getId()) > 0;
    }

    @Override
    public Optional<Servicio> buscarPorId(Integer id) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Servicios WHERE servicio_id = ?";
        Optional<Servicio> opt = consultarUno(sql, this::mapear, id);
        opt.ifPresent(s -> {
            try {
                s.setRepuestosAsociados(listarRepuestosAsociados(s.getId()));
            } catch (PersistenciaException ignored) {
            }
        });
        return opt;
    }

    @Override
    public Optional<Servicio> buscarPorCodigo(String codigo) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Servicios WHERE codigo = ?";
        Optional<Servicio> opt = consultarUno(sql, this::mapear, codigo);
        opt.ifPresent(s -> {
            try {
                s.setRepuestosAsociados(listarRepuestosAsociados(s.getId()));
            } catch (PersistenciaException ignored) {
            }
        });
        return opt;
    }

    @Override
    public List<Servicio> listar() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Servicios ORDER BY nivel_complejidad ASC, codigo ASC";
        List<Servicio> lista = consultarLista(sql, this::mapear);
        for (Servicio s : lista) {
            s.setRepuestosAsociados(listarRepuestosAsociados(s.getId()));
        }
        return lista;
    }

    @Override
    public List<Servicio> listarActivos() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Servicios WHERE activo = 1 ORDER BY nivel_complejidad ASC, codigo ASC";
        List<Servicio> lista = consultarLista(sql, this::mapear);
        for (Servicio s : lista) {
            s.setRepuestosAsociados(listarRepuestosAsociados(s.getId()));
        }
        return lista;
    }

    @Override
    public List<Servicio> listarPorNivel(int nivel) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Servicios WHERE nivel_complejidad = ? AND activo = 1 ORDER BY codigo ASC";
        List<Servicio> lista = consultarLista(sql, this::mapear, nivel);
        for (Servicio s : lista) {
            s.setRepuestosAsociados(listarRepuestosAsociados(s.getId()));
        }
        return lista;
    }

    @Override
    public List<Repuesto> listarRepuestosAsociados(int servicioId) throws PersistenciaException {
        String sql = "SELECT r.repuesto_id, r.codigo, r.nombre, r.categoria, r.marca, r.stock_actual, "
                   + "r.stock_minimo, r.costo_compra, r.precio_venta "
                   + "FROM dbo.Repuestos r "
                   + "INNER JOIN dbo.ServicioRepuestos sr ON sr.repuesto_id = r.repuesto_id "
                   + "WHERE sr.servicio_id = ? AND r.activo = 1 ORDER BY r.nombre";
        return consultarLista(sql, this::mapearRepuestoAsociado, servicioId);
    }

    @Override
    public void asociarRepuesto(int servicioId, int repuestoId, int cantidadSugerida) throws PersistenciaException {
        String sql = "IF EXISTS (SELECT 1 FROM dbo.ServicioRepuestos WHERE servicio_id = ? AND repuesto_id = ?) "
                   + "UPDATE dbo.ServicioRepuestos SET cantidad_sugerida = ? WHERE servicio_id = ? AND repuesto_id = ? "
                   + "ELSE INSERT INTO dbo.ServicioRepuestos (servicio_id, repuesto_id, cantidad_sugerida) VALUES (?, ?, ?)";
        ejecutarUpdate(sql, servicioId, repuestoId, cantidadSugerida, servicioId, repuestoId, servicioId, repuestoId, cantidadSugerida);
    }

    @Override
    public void desasociarRepuestos(int servicioId) throws PersistenciaException {
        String sql = "DELETE FROM dbo.ServicioRepuestos WHERE servicio_id = ?";
        ejecutarUpdate(sql, servicioId);
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        String sql = "UPDATE dbo.Servicios SET activo = 0 WHERE servicio_id = ?";
        return ejecutarUpdate(sql, id) > 0;
    }

    @Override
    public boolean existeCodigo(String codigo, Integer excluirId) throws PersistenciaException {
        String sql = (excluirId != null && excluirId > 0)
                ? "SELECT COUNT(*) FROM dbo.Servicios WHERE codigo = ? AND servicio_id <> ?"
                : "SELECT COUNT(*) FROM dbo.Servicios WHERE codigo = ?";
        String c = codigo != null ? codigo.trim() : "";
        Optional<Integer> count = (excluirId != null && excluirId > 0)
                ? consultarUno(sql, rs -> rs.getInt(1), c, excluirId)
                : consultarUno(sql, rs -> rs.getInt(1), c);
        return count.map(cnt -> cnt > 0).orElse(false);
    }
}
