package dao.impl;
import dao.MotorDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class MotorDAOImpl extends BaseDAO implements MotorDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT motor_id FROM dbo.Motores", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.Motores", rs -> rs.getInt("total")).orElse(0);
    }
}
