package util;

import excepciones.ValidacionException;
import java.math.BigDecimal;
import java.time.Year;
import java.util.regex.Pattern;

/** Validaciones reutilizables de la capa de negocio. */
public final class Validador {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern VIN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$"); // 17 caracteres, sin I, O, Q
    private static final Pattern PLACA = Pattern.compile("^[A-Z]{3}\\s?\\d{4}$");  // formato Honduras: ABC 1234

    private Validador() {
    }

    public static void requerido(String valor, String campo) throws ValidacionException {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio.");
        }
    }

    public static void correo(String valor) throws ValidacionException {
        if (valor != null && !valor.isBlank() && !CORREO.matcher(valor.trim()).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido.");
        }
    }

    public static void vin(String valor) throws ValidacionException {
        requerido(valor, "VIN");
        if (!VIN.matcher(valor.trim().toUpperCase()).matches()) {
            throw new ValidacionException("El VIN debe tener 17 caracteres (letras y números, sin I, O ni Q).");
        }
    }

    public static void placa(String valor) throws ValidacionException {
        requerido(valor, "Placa");
        if (!PLACA.matcher(valor.trim().toUpperCase()).matches()) {
            throw new ValidacionException("La placa debe tener el formato ABC 1234.");
        }
    }

    public static void anioVehiculo(int anio) throws ValidacionException {
        int max = Year.now().getValue() + 1;
        if (anio < 1950 || anio > max) {
            throw new ValidacionException("El año debe estar entre 1950 y " + max + ".");
        }
    }

    public static void noNegativo(int valor, String campo) throws ValidacionException {
        if (valor < 0) {
            throw new ValidacionException("El campo '" + campo + "' no puede ser negativo.");
        }
    }

    public static void positivo(int valor, String campo) throws ValidacionException {
        if (valor <= 0) {
            throw new ValidacionException("El campo '" + campo + "' debe ser mayor que cero.");
        }
    }

    public static void positivo(BigDecimal valor, String campo) throws ValidacionException {
        if (valor == null || valor.signum() <= 0) {
            throw new ValidacionException("El campo '" + campo + "' debe ser mayor que cero.");
        }
    }

    public static void noNegativo(BigDecimal valor, String campo) throws ValidacionException {
        if (valor == null || valor.signum() < 0) {
            throw new ValidacionException("El campo '" + campo + "' no puede ser negativo.");
        }
    }
}
