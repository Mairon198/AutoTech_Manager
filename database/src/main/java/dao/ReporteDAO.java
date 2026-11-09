package dao;

import excepciones.PersistenciaException;
import modelo.TablaResultado;

public interface ReporteDAO {

    TablaResultado consultarOrdenesResumen() throws PersistenciaException;

    TablaResultado consultarStockBajo() throws PersistenciaException;

    TablaResultado consultarHistorialVehiculo(String filtro) throws PersistenciaException;

    TablaResultado consultarSaldosFacturas() throws PersistenciaException;

    TablaResultado consultarEstadisticasTaller() throws PersistenciaException;
}
