package dao.impl;
import dao.DiagnosticoDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class DiagnosticoDAOImpl extends BaseDAO implements DiagnosticoDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT diagnostico_id FROM dbo.Diagnosticos", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.Diagnosticos", rs -> rs.getInt("total")).orElse(0);
    }
}
