package dao;
import excepciones.PersistenciaException;
import java.util.List;
public interface DiagnosticoDAO {
    List<Integer> listarIds() throws PersistenciaException;
    int contar() throws PersistenciaException;
}
