package servicio;

import excepciones.AutoTechException;
import excepciones.MecanicoNoDisponibleException;
import excepciones.MotorIncompatibleException;
import excepciones.PersistenciaException;
import excepciones.ReglaNegocioException;
import excepciones.SobrepagoException;
import excepciones.StockInsuficienteException;
import excepciones.TransicionEstadoInvalidaException;
import java.sql.SQLException;

/**
 * Traduce los errores de negocio que lanzan los procedimientos almacenados
 * (THROW 5xxxx) a las excepciones propias del sistema.
 */
final class ErroresNegocio {

    private ErroresNegocio() {
    }

    static AutoTechException traducir(PersistenciaException e) {
        Throwable c = e.getCause();
        if (!(c instanceof SQLException sql) || sql.getErrorCode() < 50000) {
            return e;
        }
        int codigo = sql.getErrorCode();
        String msg = sql.getMessage();
        if (codigo == 50005) return new StockInsuficienteException(msg, e);
        if (codigo == 50072 || codigo == 50073) return new SobrepagoException(msg, e);
        if (codigo == 50040) return new TransicionEstadoInvalidaException(msg, e);
        if (codigo >= 50066 && codigo <= 50069) return new MotorIncompatibleException(msg, e);
        if (codigo >= 50031 && codigo <= 50033) return new MecanicoNoDisponibleException(msg, e);
        return new ReglaNegocioException(msg, e);
    }
}
