package dao.impl;
import dao.CambioMotorDAO;
import excepciones.PersistenciaException;
import java.util.List;
public class CambioMotorDAOImpl extends BaseDAO implements CambioMotorDAO {
    @Override public List<Integer> listarIds() throws PersistenciaException {
        return consultarLista("SELECT cambio_motor_id FROM dbo.CambiosMotor", rs -> rs.getInt(1));
    }
    @Override public int contar() throws PersistenciaException {
        return consultarUno("SELECT COUNT(*) total FROM dbo.CambiosMotor", rs -> rs.getInt("total")).orElse(0);
    }
}
