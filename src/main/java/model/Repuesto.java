package model;

import java.math.BigDecimal;

/** Repuesto del inventario (tabla Repuestos). */
public record Repuesto(int id, String codigo, String nombre, String categoria, int stock, int stockMinimo,
        BigDecimal costo, BigDecimal precioVenta, boolean activo) {

    /** Texto con el que aparece en las listas desplegables. */
    @Override
    public String toString() {
        return nombre + " · stock " + stock + " · " + String.format("%,.2f", precioVenta);
    }
}
