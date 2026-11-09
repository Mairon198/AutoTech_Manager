package dao.impl;
import dao.OrdenTrabajoDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class OrdenTrabajoDAOImpl extends BaseDAO implements OrdenTrabajoDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT orden_id FROM dbo.OrdenesTrabajo", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.OrdenesTrabajo", rs -> rs.getInt("total")).orElse(0);
    }
}
