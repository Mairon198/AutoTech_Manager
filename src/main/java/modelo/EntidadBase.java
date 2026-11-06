package modelo;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class EntidadBase {

    protected int id;
    protected boolean activo = true;
    protected LocalDateTime fechaCreacion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean esNueva() {
        return id <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntidadBase otra = (EntidadBase) o;
        return id > 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
