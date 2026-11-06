package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Diagnostico extends EntidadBase {

    private OrdenTrabajo orden;
    private Mecanico mecanico;
    private LocalDateTime fecha;
    private String descripcion;
    private String causaRaiz;
    private String recomendacion;
    private boolean requiereCambioMotor;
    private List<EvidenciaTecnica> evidencias = new ArrayList<>();

    public Diagnostico() {
    }

    public void agregarEvidencia(EvidenciaTecnica e) {
        e.setDiagnostico(this);
        evidencias.add(e);
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public Mecanico getMecanico() {
        return mecanico;
    }

    public void setMecanico(Mecanico mecanico) {
        this.mecanico = mecanico;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCausaRaiz() {
        return causaRaiz;
    }

    public void setCausaRaiz(String causaRaiz) {
        this.causaRaiz = causaRaiz;
    }

    public String getRecomendacion() {
        return recomendacion;
    }

    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }

    public boolean isRequiereCambioMotor() {
        return requiereCambioMotor;
    }

    public void setRequiereCambioMotor(boolean requiereCambioMotor) {
        this.requiereCambioMotor = requiereCambioMotor;
    }

    public List<EvidenciaTecnica> getEvidencias() {
        return evidencias;
    }

    public void setEvidencias(List<EvidenciaTecnica> evidencias) {
        this.evidencias = evidencias;
    }
}
