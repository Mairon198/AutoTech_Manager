package dao.impl;

import dao.UsuarioDAO;
import excepciones.PersistenciaException;
import java.sql.Timestamp;
import java.util.Optional;
import modelo.Usuario;

public class UsuarioDAOImpl extends TallerDAOBase implements UsuarioDAO {

    private static final String AUDITAR =
            "INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo) "
            + "VALUES ('Usuarios', ?, ?, 'sesion', NULL, ?)";

    @Override
    public Optional<Usuario> buscarPorNombre(String nombreUsuario) throws PersistenciaException {
        return consultarUno("SELECT usuario_id, nombre_usuario, nombre_completo, rol, sal, hash_contrasena, "
                + "iteraciones, intentos_fallidos, ultimo_acceso, activo FROM dbo.Usuarios WHERE nombre_usuario = ?",
                rs -> {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("usuario_id"));
                    u.setNombreUsuario(rs.getString("nombre_usuario"));
                    u.setNombreCompleto(rs.getString("nombre_completo"));
                    u.setRol(rs.getString("rol"));
                    u.setSal(rs.getString("sal"));
                    u.setHuella(rs.getString("hash_contrasena"));
                    u.setIteraciones(rs.getInt("iteraciones"));
                    u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
                    Timestamp t = rs.getTimestamp("ultimo_acceso");
                    u.setUltimoAcceso(t == null ? null : t.toLocalDateTime());
                    u.setActivo(rs.getBoolean("activo"));
                    return u;
                }, nombreUsuario);
    }

    @Override
    public int registrarFallo(int usuarioId) throws PersistenciaException {
        return ejecutarEnTransaccion(cn -> {
            ejecutarUpdate(cn, "UPDATE dbo.Usuarios SET intentos_fallidos = intentos_fallidos + 1 WHERE usuario_id = ?", usuarioId);
            ejecutarUpdate(cn, AUDITAR, "LOGIN_FAIL", usuarioId, "Contraseña incorrecta");
            return consultarUno(cn, "SELECT intentos_fallidos FROM dbo.Usuarios WHERE usuario_id = ?",
                    rs -> rs.getInt(1), usuarioId).orElse(0);
        });
    }

    @Override
    public void registrarAcceso(int usuarioId, String nombreUsuario) throws PersistenciaException {
        ejecutarEnTransaccion(cn -> {
            ejecutarUpdate(cn, "UPDATE dbo.Usuarios SET intentos_fallidos = 0, ultimo_acceso = SYSDATETIME() "
                    + "WHERE usuario_id = ?", usuarioId);
            ejecutarUpdate(cn, AUDITAR, "LOGIN", usuarioId, "Inicio de sesión de " + nombreUsuario);
        });
    }

    @Override
    public void registrarSalida(int usuarioId, String nombreUsuario) throws PersistenciaException {
        ejecutarUpdate(AUDITAR, "LOGOUT", usuarioId, "Cierre de sesión de " + nombreUsuario);
    }
}
