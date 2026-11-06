package com.autotech.modelo;

import com.autotech.modelo.enums.CategoriaRepuesto;
import java.math.BigDecimal;

public class Repuesto extends EntidadBase {

    private String codigo;
    private String nombre;
    private CategoriaRepuesto categoria;
    private String marca;
    private int stock;
    private int stockMinimo = 2;
    private BigDecimal costoUnitario = BigDecimal.ZERO;
    private BigDecimal precioVenta = BigDecimal.ZERO;
    private String ubicacion;
    private Proveedor proveedor;

    public Repuesto() {
    }

    public Repuesto(String codigo, String nombre, CategoriaRepuesto categoria, String marca,
                    int stock, int stockMinimo, BigDecimal costoUnitario, BigDecimal precioVenta,
                    String ubicacion, Proveedor proveedor) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.marca = marca;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.costoUnitario = costoUnitario != null ? costoUnitario : BigDecimal.ZERO;
        this.precioVenta = precioVenta != null ? precioVenta : BigDecimal.ZERO;
        this.ubicacion = ubicacion;
        this.proveedor = proveedor;
    }

    public boolean hayStock(int cantidad) {
        return cantidad > 0 && stock >= cantidad;
    }

    public boolean estaBajoMinimo() {
        return stock <= stockMinimo;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (stock: " + stock + ")";
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaRepuesto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaRepuesto categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
}
