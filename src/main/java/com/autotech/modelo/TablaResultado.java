package com.autotech.modelo;

import java.util.Collections;
import java.util.List;

public class TablaResultado {

    private final List<String> columnas;
    private final List<Object[]> filas;

    public TablaResultado(List<String> columnas, List<Object[]> filas) {
        this.columnas = columnas != null ? columnas : Collections.emptyList();
        this.filas = filas != null ? filas : Collections.emptyList();
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public List<Object[]> getFilas() {
        return filas;
    }

    public int getCantidadFilas() {
        return filas.size();
    }
}
