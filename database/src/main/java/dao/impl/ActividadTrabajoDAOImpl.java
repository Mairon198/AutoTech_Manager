package dao.impl;
import dao.ActividadTrabajoDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class ActividadTrabajoDAOImpl extends BaseDAO implements ActividadTrabajoDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT actividad_id FROM dbo.ActividadesTrabajo", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.ActividadesTrabajo", rs -> rs.getInt("total")).orElse(0);
    }
}
