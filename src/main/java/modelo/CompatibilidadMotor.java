package modelo;

public class CompatibilidadMotor extends EntidadBase {

    private Motor motor;
    private String marca;
    private String modelo;
    private int anioDesde;
    private int anioHasta;

    public CompatibilidadMotor() {
    }

    public boolean aplicaA(Vehiculo v) {
        return marca.equalsIgnoreCase(v.getMarca())
                && modelo.equalsIgnoreCase(v.getModelo())
                && v.getAnio() >= anioDesde && v.getAnio() <= anioHasta;
    }

    public Motor getMotor() {
        return motor;
    }

    public void setMotor(Motor motor) {
        this.motor = motor;
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

    public int getAnioDesde() {
        return anioDesde;
    }

    public void setAnioDesde(int anioDesde) {
        this.anioDesde = anioDesde;
    }

    public int getAnioHasta() {
        return anioHasta;
    }

    public void setAnioHasta(int anioHasta) {
        this.anioHasta = anioHasta;
    }
}
