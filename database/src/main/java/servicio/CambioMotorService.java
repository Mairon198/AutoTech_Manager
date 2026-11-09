package servicio;

import excepciones.ReglaNegocioException;
import excepciones.MotorIncompatibleException;
import modelo.CambioMotor;
import modelo.enums.EstadoMotor;
import java.time.LocalDateTime;
import java.util.List;

public class CambioMotorService {
    public static final List<String> PASOS = List.of(
        "Verificar autorización del cliente y diagnóstico",
        "Confirmar compatibilidad del motor nuevo",
        "Preparar vehículo, herramientas y área de trabajo",
        "Desconectar batería y sistemas eléctricos",
        "Drenar fluidos y desconectar componentes",
        "Desmontar el motor anterior",
        "Registrar serie y destino del motor retirado",
        "Instalar y asegurar el motor nuevo",
        "Reconectar sistemas y rellenar fluidos",
        "Realizar pruebas de arranque, fugas y funcionamiento",
        "Registrar resultados, aprobación y garantía"
    );
    public void validar(CambioMotor c) throws ReglaNegocioException {
        if (c == null || !c.datosCompletos()) throw new ReglaNegocioException("Complete los datos del cambio de motor.");
        if (!c.isAutorizadoCliente()) throw new ReglaNegocioException("Se requiere autorización del cliente.");
        if (c.getFechaInstalacion().isBefore(c.getFechaDesmontaje())) throw new ReglaNegocioException("La instalación no puede ser anterior al desmontaje.");
        if (c.getMotorNuevo().getEstado() != EstadoMotor.DISPONIBLE) throw new ReglaNegocioException("El motor seleccionado no está disponible.");
        if (!c.isPruebasAprobadas()) throw new ReglaNegocioException("Las pruebas finales deben estar aprobadas.");
    }
    public void registrar(CambioMotor c) throws ReglaNegocioException {
        validar(c);
        c.getMotorNuevo().setEstado(EstadoMotor.INSTALADO);
    }
}
