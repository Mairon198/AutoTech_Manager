package modelo.enums;

import java.util.List;

/**
 * Registros de catálogo que se pueden eliminar físicamente cuando no tienen historial.
 * "puentes" son tablas intermedias propias del registro que se borran junto con él.
 */
public enum RegistroEliminable {
    CLIENTE("Clientes", "cliente_id", "el cliente"),
    VEHICULO("Vehiculos", "vehiculo_id", "el vehículo"),
    MECANICO("Mecanicos", "mecanico_id", "el mecánico", "MecanicoEspecialidades"),
    SERVICIO("Servicios", "servicio_id", "el servicio", "ServicioRepuestos"),
    REPUESTO("Repuestos", "repuesto_id", "el repuesto", "ServicioRepuestos"),
    PROVEEDOR("Proveedores", "proveedor_id", "el proveedor");

    private final String tabla;
    private final String columnaId;
    private final String descripcion;
    private final List<String> puentes;

    RegistroEliminable(String tabla, String columnaId, String descripcion, String... puentes) {
        this.tabla = tabla;
        this.columnaId = columnaId;
        this.descripcion = descripcion;
        this.puentes = List.of(puentes);
    }

    public String getTabla() { return tabla; }
    public String getColumnaId() { return columnaId; }
    public String getDescripcion() { return descripcion; }
    public List<String> getPuentes() { return puentes; }
}
