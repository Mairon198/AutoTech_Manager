package dao;

import excepciones.PersistenciaException;
import java.util.Optional;
import modelo.Usuario;

/** Contrato de persistencia de los usuarios del sistema. */
public interface UsuarioDAO {

    Optional<Usuario> buscarPorNombre(String nombreUsuario) throws PersistenciaException;

    /** Suma un intento fallido y devuelve cuántos lleva. */
    int registrarFallo(int usuarioId) throws PersistenciaException;

    /** Reinicia los intentos, guarda la fecha de acceso y lo deja en la auditoría. */
    void registrarAcceso(int usuarioId, String nombreUsuario) throws PersistenciaException;

    void registrarSalida(int usuarioId, String nombreUsuario) throws PersistenciaException;
}
