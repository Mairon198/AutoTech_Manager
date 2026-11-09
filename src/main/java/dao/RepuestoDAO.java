package dao;

import excepciones.PersistenciaException;
import modelo.Repuesto;
import modelo.enums.CategoriaRepuesto;

import java.util.List;
import java.util.Optional;

public interface RepuestoDAO extends CrudDAO<Repuesto, Integer> {

    Optional<Repuesto> buscarPorCodigo(String codigo) throws PersistenciaException;

    List<Repuesto> listarActivos() throws PersistenciaException;

    List<Repuesto> listarBajoStockMinimo() throws PersistenciaException;

    List<Repuesto> listarPorCategoria(CategoriaRepuesto categoria) throws PersistenciaException;

    List<Repuesto> buscarPorCriterio(String filtro) throws PersistenciaException;

    boolean actualizarStock(int repuestoId, int nuevoStock) throws PersistenciaException;

    boolean existeCodigo(String codigo, Integer excluirId) throws PersistenciaException;
}
