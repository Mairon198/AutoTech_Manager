package dao.impl;

import dao.MecanicoDAO;
import excepciones.PersistenciaException;
import modelo.Especialidad;
import modelo.Mecanico;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class MecanicoDAOImpl extends BaseDAO implements MecanicoDAO {

    private Mecanico mapear(ResultSet rs) throws SQLException {
        Mecanico m = new Mecanico();
        m.setId(rs.getInt("mecanico_id"));
        m.setCodigoEmpleado(rs.getString("codigo_empleado"));
        m.setIdentidad(rs.getString("identificacion"));

        String nombreCompleto = rs.getString("nombre");
        if (nombreCompleto != null) {
            String[] partes = nombreCompleto.trim().split("\\s+", 2);
            m.setNombre(partes[0]);
            m.setApellido(partes.length > 1 ? partes[1] : "");
        }
        m.setTelefono(rs.getString("telefono"));
        m.setCorreo(rs.getString("correo"));
        m.setNivelCertificacion(rs.getString("nivel_certificacion"));
        m.setTarifaHora(rs.getBigDecimal("tarifa_hora"));
        m.setDisponible(rs.getBoolean("disponible"));
        m.setActivo(rs.getBoolean("activo"));

        Timestamp ts = rs.getTimestamp("fecha_ingreso");
        if (ts != null) {
            m.setFechaCreacion(ts.toLocalDateTime());
        }
        return m;
    }

    private Especialidad mapearEspecialidad(ResultSet rs) throws SQLException {
        Especialidad e = new Especialidad();
        e.setId(rs.getInt("especialidad_id"));
        e.setNombre(rs.getString("nombre"));
        e.setDescripcion(rs.getString("descripcion"));
        return e;
    }

    @Override
    public Integer insertar(Mecanico entidad) throws PersistenciaException {
        String sql = "INSERT INTO dbo.Mecanicos (codigo_empleado, identificacion, nombre, telefono, correo, "
                   + "nivel_certificacion, tarifa_hora, disponible, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String nombreGuardar = entidad.getNombreCompleto().trim();
        int id = ejecutarInsertConClave(sql,
                entidad.getCodigoEmpleado(),
                entidad.getIdentidad(),
                nombreGuardar,
                entidad.getTelefono(),
                entidad.getCorreo(),
                entidad.getNivelCertificacion() != null ? entidad.getNivelCertificacion() : "SENIOR",
                entidad.getTarifaHora() != null ? entidad.getTarifaHora() : BigDecimal.ZERO,
                entidad.isDisponible(),
                entidad.isActivo());
        entidad.setId(id);
        return id;
    }

    @Override
    public boolean actualizar(Mecanico entidad) throws PersistenciaException {
        String sql = "UPDATE dbo.Mecanicos SET codigo_empleado = ?, identificacion = ?, nombre = ?, telefono = ?, "
                   + "correo = ?, nivel_certificacion = ?, tarifa_hora = ?, disponible = ?, activo = ? "
                   + "WHERE mecanico_id = ?";
        String nombreGuardar = entidad.getNombreCompleto().trim();
        return ejecutarUpdate(sql,
                entidad.getCodigoEmpleado(),
                entidad.getIdentidad(),
                nombreGuardar,
                entidad.getTelefono(),
                entidad.getCorreo(),
                entidad.getNivelCertificacion() != null ? entidad.getNivelCertificacion() : "SENIOR",
                entidad.getTarifaHora() != null ? entidad.getTarifaHora() : BigDecimal.ZERO,
                entidad.isDisponible(),
                entidad.isActivo(),
                entidad.getId()) > 0;
    }

    @Override
    public Optional<Mecanico> buscarPorId(Integer id) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Mecanicos WHERE mecanico_id = ?";
        Optional<Mecanico> opt = consultarUno(sql, this::mapear, id);
        opt.ifPresent(m -> {
            try {
                m.setEspecialidades(listarEspecialidadesMecanico(m.getId()));
            } catch (PersistenciaException ignored) {
            }
        });
        return opt;
    }

    @Override
    public Optional<Mecanico> buscarPorCodigo(String codigoEmpleado) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Mecanicos WHERE codigo_empleado = ?";
        Optional<Mecanico> opt = consultarUno(sql, this::mapear, codigoEmpleado);
        opt.ifPresent(m -> {
            try {
                m.setEspecialidades(listarEspecialidadesMecanico(m.getId()));
            } catch (PersistenciaException ignored) {
            }
        });
        return opt;
    }

    @Override
    public Optional<Mecanico> buscarPorIdentidad(String identidad) throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Mecanicos WHERE identificacion = ?";
        Optional<Mecanico> opt = consultarUno(sql, this::mapear, identidad);
        opt.ifPresent(m -> {
            try {
                m.setEspecialidades(listarEspecialidadesMecanico(m.getId()));
            } catch (PersistenciaException ignored) {
            }
        });
        return opt;
    }

    @Override
    public List<Mecanico> listar() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Mecanicos ORDER BY mecanico_id DESC";
        List<Mecanico> lista = consultarLista(sql, this::mapear);
        for (Mecanico m : lista) {
            m.setEspecialidades(listarEspecialidadesMecanico(m.getId()));
        }
        return lista;
    }

    @Override
    public List<Mecanico> listarActivos() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Mecanicos WHERE activo = 1 ORDER BY nombre ASC";
        List<Mecanico> lista = consultarLista(sql, this::mapear);
        for (Mecanico m : lista) {
            m.setEspecialidades(listarEspecialidadesMecanico(m.getId()));
        }
        return lista;
    }

    @Override
    public List<Mecanico> listarDisponibles() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Mecanicos WHERE activo = 1 AND disponible = 1 ORDER BY nombre ASC";
        List<Mecanico> lista = consultarLista(sql, this::mapear);
        for (Mecanico m : lista) {
            m.setEspecialidades(listarEspecialidadesMecanico(m.getId()));
        }
        return lista;
    }

    @Override
    public boolean cambiarDisponibilidad(int mecanicoId, boolean disponible) throws PersistenciaException {
        String sql = "UPDATE dbo.Mecanicos SET disponible = ? WHERE mecanico_id = ?";
        return ejecutarUpdate(sql, disponible, mecanicoId) > 0;
    }

    @Override
    public boolean desactivar(Integer id) throws PersistenciaException {
        String sql = "UPDATE dbo.Mecanicos SET activo = 0, disponible = 0 WHERE mecanico_id = ?";
        return ejecutarUpdate(sql, id) > 0;
    }

    @Override
    public List<Especialidad> listarEspecialidadesMecanico(int mecanicoId) throws PersistenciaException {
        String sql = "SELECT e.especialidad_id, e.nombre, e.descripcion FROM dbo.Especialidades e "
                   + "INNER JOIN dbo.MecanicoEspecialidades me ON me.especialidad_id = e.especialidad_id "
                   + "WHERE me.mecanico_id = ? ORDER BY e.nombre";
        return consultarLista(sql, this::mapearEspecialidad, mecanicoId);
    }

    @Override
    public void guardarEspecialidades(int mecanicoId, List<Integer> especialidadIds) throws PersistenciaException {
        ejecutarEnTransaccion(cn -> {
            String sqlDel = "DELETE FROM dbo.MecanicoEspecialidades WHERE mecanico_id = ?";
            ejecutarUpdate(cn, sqlDel, mecanicoId);
            if (especialidadIds != null) {
                String sqlIns = "INSERT INTO dbo.MecanicoEspecialidades (mecanico_id, especialidad_id) VALUES (?, ?)";
                for (Integer espId : especialidadIds) {
                    if (espId != null && espId > 0) {
                        ejecutarUpdate(cn, sqlIns, mecanicoId, espId);
                    }
                }
            }
        });
    }

    @Override
    public List<Especialidad> listarTodasEspecialidades() throws PersistenciaException {
        String sql = "SELECT * FROM dbo.Especialidades ORDER BY nombre ASC";
        return consultarLista(sql, this::mapearEspecialidad);
    }

    @Override
    public boolean existeCodigo(String codigoEmpleado, Integer excluirId) throws PersistenciaException {
        String sql = (excluirId != null && excluirId > 0)
                ? "SELECT COUNT(*) FROM dbo.Mecanicos WHERE codigo_empleado = ? AND mecanico_id <> ?"
                : "SELECT COUNT(*) FROM dbo.Mecanicos WHERE codigo_empleado = ?";
        String c = codigoEmpleado != null ? codigoEmpleado.trim() : "";
        Optional<Integer> count = (excluirId != null && excluirId > 0)
                ? consultarUno(sql, rs -> rs.getInt(1), c, excluirId)
                : consultarUno(sql, rs -> rs.getInt(1), c);
        return count.map(cnt -> cnt > 0).orElse(false);
    }

    @Override
    public boolean existeIdentidad(String identidad, Integer excluirId) throws PersistenciaException {
        String sql = (excluirId != null && excluirId > 0)
                ? "SELECT COUNT(*) FROM dbo.Mecanicos WHERE identificacion = ? AND mecanico_id <> ?"
                : "SELECT COUNT(*) FROM dbo.Mecanicos WHERE identificacion = ?";
        String i = identidad != null ? identidad.trim() : "";
        Optional<Integer> count = (excluirId != null && excluirId > 0)
                ? consultarUno(sql, rs -> rs.getInt(1), i, excluirId)
                : consultarUno(sql, rs -> rs.getInt(1), i);
        return count.map(cnt -> cnt > 0).orElse(false);
    }
}
