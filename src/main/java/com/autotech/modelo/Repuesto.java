package com.autotech.modelo;

import com.autotech.modelo.enums.CategoriaRepuesto;
import java.math.BigDecimal;

/** Artículo del inventario (aceites, filtros, baterías, transmisiones, etc.). */
public class Repuesto extends EntidadBase {

    private String codigo;
    private String nombre;
    private CategoriaRepuesto categoria;
    private String marca;
    private int stock;
    private int stockMinimo;
    private BigDecimal costoUnitario = BigDecimal.ZERO;
    private BigDecimal precioVenta = BigDecimal.ZERO;
    private Proveedor proveedor;

    public Repuesto() {
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

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
}
