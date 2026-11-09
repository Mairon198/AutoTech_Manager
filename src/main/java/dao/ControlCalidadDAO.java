package dao;
import excepciones.PersistenciaException;
import java.util.List;
public interface ControlCalidadDAO {
    List<Integer> listarIds() throws PersistenciaException;
    int contar() throws PersistenciaException;
}
