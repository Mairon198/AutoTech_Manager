package dao.impl;
import dao.PresupuestoDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class PresupuestoDAOImpl extends BaseDAO implements PresupuestoDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT presupuesto_id FROM dbo.Presupuestos", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.Presupuestos", rs -> rs.getInt("total")).orElse(0);
    }
}
