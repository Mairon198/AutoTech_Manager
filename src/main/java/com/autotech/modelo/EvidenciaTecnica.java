package com.autotech.modelo;

public class EvidenciaTecnica extends EntidadBase {

    private Diagnostico diagnostico;
    private String tipo;
    private String descripcion;
    private String valorMedido;
    private String rutaArchivo;

    public EvidenciaTecnica() {
    }

    public Diagnostico getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(Diagnostico diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getValorMedido() {
        return valorMedido;
    }

    public void setValorMedido(String valorMedido) {
        this.valorMedido = valorMedido;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }
}
