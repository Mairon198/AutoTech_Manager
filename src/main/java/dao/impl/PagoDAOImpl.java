package dao.impl;
import dao.PagoDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class PagoDAOImpl extends BaseDAO implements PagoDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT pago_id FROM dbo.Pagos", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.Pagos", rs -> rs.getInt("total")).orElse(0);
    }
}
