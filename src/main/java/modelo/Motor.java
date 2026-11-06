package modelo;

import modelo.enums.EstadoMotor;
import modelo.enums.TipoCombustible;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Motor con trazabilidad por número de serie (módulo 7). */
public class Motor extends EntidadBase {

    private String numeroSerie;
    private String marca;
    private String modeloMotor;
    private String cilindrada;
    private TipoCombustible combustible;
    private EstadoMotor estado = EstadoMotor.DISPONIBLE;
    private boolean esNuevo = true;
    private BigDecimal costo = BigDecimal.ZERO;
    private Repuesto repuesto;
    private Proveedor proveedor;
    private List<CompatibilidadMotor> compatibilidades = new ArrayList<>();

    public Motor() {
    }

    public boolean estaDisponible() {
        return activo && estado == EstadoMotor.DISPONIBLE;
    }

    /** Compatible si alguna regla coincide con marca, modelo, año y combustible del vehículo. */
    public boolean esCompatibleCon(Vehiculo v) {
        if (combustible != null && v.getCombustible() != null && combustible != v.getCombustible()) {
            return false;
        }
        for (CompatibilidadMotor c : compatibilidades) {
            if (c.aplicaA(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return numeroSerie + " - " + marca + " " + modeloMotor + " " + cilindrada;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModeloMotor() {
        return modeloMotor;
    }

    public void setModeloMotor(String modeloMotor) {
        this.modeloMotor = modeloMotor;
    }

    public String getCilindrada() {
        return cilindrada;
    }

    public void setCilindrada(String cilindrada) {
        this.cilindrada = cilindrada;
    }

    public TipoCombustible getCombustible() {
        return combustible;
    }

    public void setCombustible(TipoCombustible combustible) {
        this.combustible = combustible;
    }

    public EstadoMotor getEstado() {
        return estado;
    }

    public void setEstado(EstadoMotor estado) {
        this.estado = estado;
    }

    public boolean isEsNuevo() {
        return esNuevo;
    }

    public void setEsNuevo(boolean esNuevo) {
        this.esNuevo = esNuevo;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public Repuesto getRepuesto() {
        return repuesto;
    }

    public void setRepuesto(Repuesto repuesto) {
        this.repuesto = repuesto;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public List<CompatibilidadMotor> getCompatibilidades() {
        return compatibilidades;
    }

    public void setCompatibilidades(List<CompatibilidadMotor> compatibilidades) {
        this.compatibilidades = compatibilidades;
    }
}
