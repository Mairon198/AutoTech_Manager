package dao;

import excepciones.PersistenciaException;
import modelo.MovimientoInventario;

import java.util.List;

public interface MovimientoInventarioDAO extends CrudDAO<MovimientoInventario, Integer> {

    List<MovimientoInventario> listarPorRepuesto(int repuestoId) throws PersistenciaException;

    List<MovimientoInventario> listarPorOrden(int ordenId) throws PersistenciaException;

    List<MovimientoInventario> listarRecientes(int limite) throws PersistenciaException;
}
