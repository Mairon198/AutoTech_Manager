package dao.impl;
import dao.ControlCalidadDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class ControlCalidadDAOImpl extends BaseDAO implements ControlCalidadDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT control_id FROM dbo.ControlesCalidad", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.ControlesCalidad", rs -> rs.getInt("total")).orElse(0);
    }
}
