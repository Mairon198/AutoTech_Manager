package dao.impl;
import dao.FacturaDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class FacturaDAOImpl extends BaseDAO implements FacturaDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT factura_id FROM dbo.Facturas", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.Facturas", rs -> rs.getInt("total")).orElse(0);
    }
}
