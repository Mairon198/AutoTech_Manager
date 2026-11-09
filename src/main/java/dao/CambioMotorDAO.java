package dao;

import excepciones.PersistenciaException;
import java.util.List;
import java.util.Optional;
import modelo.CambioMotor;
import modelo.Motor;
import modelo.TablaResultado;

/** Contrato de persistencia de motores y sustituciones de motor (Módulo 7). */
public interface CambioMotorDAO {

    /** Motores del inventario con su estado y si son compatibles con el vehículo (fn_MotorCompatible). */
    List<Motor> listarMotores(int vehiculoId) throws PersistenciaException;

    /** IDs de los motores compatibles con el vehículo. */
    List<Integer> compatibles(int vehiculoId) throws PersistenciaException;

    Optional<String> serieMotorActual(int vehiculoId) throws PersistenciaException;

    /** Registra la sustitución con sp_RegistrarCambioMotor y devuelve el id. */
    int registrar(CambioMotor c, String usuario) throws PersistenciaException;

    TablaResultado consultarCambio(int ordenId) throws PersistenciaException;

    /** Estado de los 11 pasos del caso Hilux para la orden (true = cumplido). */
    boolean[] avancePasos(int ordenId) throws PersistenciaException;
}
