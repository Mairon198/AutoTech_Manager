package modelo;

/** Datos comunes de clientes y mecánicos. */
public abstract class Persona extends EntidadBase {

    protected String identidad;
    protected String nombre;
    protected String apellido;
    protected String telefono;
    protected String correo;
    protected String direccion;

    public String getNombreCompleto() {
        return nombre + " " + (apellido == null ? "" : apellido);
    }

    /** Cada subclase indica qué rol cumple la persona en el taller. */
    public abstract String getTipoPersona();

    public String getIdentidad() {
        return identidad;
    }

    public void setIdentidad(String identidad) {
        this.identidad = identidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String toString() {
        return getNombreCompleto().trim();
    }
}
