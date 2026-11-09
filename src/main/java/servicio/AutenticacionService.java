package servicio;

import dao.UsuarioDAO;
import dao.impl.UsuarioDAOImpl;
import excepciones.AutoTechException;
import excepciones.PersistenciaException;
import excepciones.ReglaNegocioException;
import excepciones.ValidacionException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import modelo.Usuario;
import util.Seguridad;
import util.Sesion;

/** Inicio y cierre de sesión. */
public class AutenticacionService {

    public static final int MAXIMO_INTENTOS = 5;
    private static final String GENERICO = "Usuario o contraseña incorrectos.";
    // sal ficticia: si el usuario no existe se hace el mismo cálculo, así no se distingue por el tiempo de respuesta
    private static final String SAL_FICTICIA = "00000000000000000000000000000000";

    private final UsuarioDAO dao = new UsuarioDAOImpl();

    public Usuario iniciarSesion(String nombreUsuario, char[] contrasena) throws AutoTechException {
        try {
            if (nombreUsuario == null || nombreUsuario.isBlank()) {
                throw new ValidacionException("Escriba su usuario.");
            }
            if (contrasena == null || contrasena.length == 0) {
                throw new ValidacionException("Escriba su contraseña.");
            }
            String nombre = nombreUsuario.trim().toLowerCase(Locale.ROOT);
            Optional<Usuario> encontrado = dao.buscarPorNombre(nombre);
            if (encontrado.isEmpty()) {
                Seguridad.huella(contrasena, SAL_FICTICIA, Seguridad.ITERACIONES);
                throw new ReglaNegocioException(GENERICO);
            }
            Usuario u = encontrado.get();
            if (!u.isActivo()) {
                throw new ReglaNegocioException("El usuario " + nombre + " está desactivado.");
            }
            if (u.estaBloqueado(MAXIMO_INTENTOS)) {
                throw new ReglaNegocioException("El usuario " + nombre + " está bloqueado por " + MAXIMO_INTENTOS
                        + " intentos fallidos. Un administrador debe desbloquearlo.");
            }
            if (!Seguridad.verificar(contrasena, u.getSal(), u.getIteraciones(), u.getHuella())) {
                int restantes = MAXIMO_INTENTOS - dao.registrarFallo(u.getId());
                if (restantes <= 0) {
                    throw new ReglaNegocioException("Contraseña incorrecta. El usuario quedó bloqueado.");
                }
                throw new ReglaNegocioException(GENERICO + (restantes <= 2
                        ? " Le queda" + (restantes == 1 ? " 1 intento." : "n " + restantes + " intentos.") : ""));
            }
            dao.registrarAcceso(u.getId(), u.getNombreUsuario());
            Sesion.iniciar(u);
            return u;
        } finally {
            if (contrasena != null) {
                Arrays.fill(contrasena, '\0');
            }
        }
    }

    public void cerrarSesion() {
        Usuario u = Sesion.actual();
        if (u != null) {
            try {
                dao.registrarSalida(u.getId(), u.getNombreUsuario());
            } catch (PersistenciaException ignorada) {
                // si no hay conexión, la sesión se cierra igual
            }
        }
        Sesion.cerrar();
    }
}
