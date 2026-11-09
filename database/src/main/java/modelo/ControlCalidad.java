package modelo;

import java.time.LocalDateTime;

public class ControlCalidad extends EntidadBase {

    private OrdenTrabajo orden;
    private Mecanico inspector;
    private LocalDateTime fecha;
    private boolean aprobado;
    private String checklist;
    private String observaciones;

    public ControlCalidad() {
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public Mecanico getInspector() {
        return inspector;
    }

    public void setInspector(Mecanico inspector) {
        this.inspector = inspector;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public String getChecklist() {
        return checklist;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
