package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Huellas de contraseña con PBKDF2-HMAC-SHA256. La contraseña nunca se guarda:
 * se guarda la sal (aleatoria) y la huella; al iniciar sesión se recalcula y se compara.
 */
public final class Seguridad {

    public static final int ITERACIONES = 120_000;
    private static final int BITS = 256;
    private static final HexFormat HEX = HexFormat.of().withUpperCase();

    private Seguridad() {
    }

    public static String huella(char[] contrasena, String salHex, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(contrasena, HEX.parseHex(salHex), iteraciones, BITS);
        try {
            byte[] h = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return HEX.formatHex(h);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 no está disponible en esta JVM", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Compara en tiempo constante (no revela por cuánto se equivocó). */
    public static boolean verificar(char[] contrasena, String salHex, int iteraciones, String huellaGuardada) {
        String calculada = huella(contrasena, salHex, iteraciones);
        return MessageDigest.isEqual(calculada.getBytes(), huellaGuardada.trim().toUpperCase().getBytes());
    }

    public static String nuevaSal() {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return HEX.formatHex(sal);
    }
}
