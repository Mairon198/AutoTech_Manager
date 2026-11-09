package dao;
import excepciones.PersistenciaException;
import java.util.List;
public interface PagoDAO {
    List<Integer> listarIds() throws PersistenciaException;
    int contar() throws PersistenciaException;
}
