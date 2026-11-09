package modelo;

import modelo.enums.TipoCombustible;

public class Vehiculo extends EntidadBase {

    private Cliente cliente;
    private String placa;
    private String vin;
    private String marca;
    private String modelo;
    private int anio;
    private String color;
    private int kilometraje;
    private TipoCombustible combustible;
    private String serieMotorActual;

    public Vehiculo() {
    }

    public Vehiculo(String placa, String vin, String marca, String modelo, int anio,
            String color, int kilometraje, TipoCombustible combustible) {
        this.placa = placa;
        this.vin = vin;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.kilometraje = kilometraje;
        this.combustible = combustible;
    }

    public String getDescripcion() {
        return marca + " " + modelo + " " + anio + " (" + placa + ")";
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getKilometraje() {
        return kilometraje;
    }

    public void setKilometraje(int kilometraje) {
        this.kilometraje = kilometraje;
    }

    public TipoCombustible getCombustible() {
        return combustible;
    }

    public void setCombustible(TipoCombustible combustible) {
        this.combustible = combustible;
    }

    public String getSerieMotorActual() {
        return serieMotorActual;
    }

    public void setSerieMotorActual(String serieMotorActual) {
        this.serieMotorActual = serieMotorActual;
    }

    @Override
    public String toString() {
        return getDescripcion();
    }
}
