package modelo.enums;

public enum NivelComplejidad {
    BASICO(1, "Mantenimiento básico"),
    PREVENTIVO(2, "Mantenimiento preventivo"),
    INTERMEDIO(3, "Reparación intermedia"),
    AVANZADO(4, "Reparación avanzada"),
    MAYOR(5, "Reparación mayor");

    private final int nivel;
    private final String categoria;

    NivelComplejidad(int nivel, String categoria) {
        this.nivel = nivel;
        this.categoria = categoria;
    }

    public int getNivel() {
        return nivel;
    }

    public String getCategoria() {
        return categoria;
    }

    public static NivelComplejidad desdeNivel(int nivel) {
        for (NivelComplejidad n : values()) {
            if (n.nivel == nivel) {
                return n;
            }
        }
        throw new IllegalArgumentException("Nivel de complejidad inválido: " + nivel);
    }

    @Override
    public String toString() {
        return nivel + " - " + categoria;
    }
}
