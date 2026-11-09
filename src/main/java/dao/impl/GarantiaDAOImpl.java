package dao.impl;
import dao.GarantiaDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class GarantiaDAOImpl extends BaseDAO implements GarantiaDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT garantia_id FROM dbo.Garantias", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.Garantias", rs -> rs.getInt("total")).orElse(0);
    }
}
