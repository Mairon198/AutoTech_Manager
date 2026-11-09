package dao;
import excepciones.PersistenciaException;
import java.util.List;
public interface PresupuestoDAO {
    List<Integer> listarIds() throws PersistenciaException;
    int contar() throws PersistenciaException;
}
