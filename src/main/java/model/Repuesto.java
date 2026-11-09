package model;

import java.math.BigDecimal;

/**
 * Repuesto del inventario (tabla Repuestos). El stock no se edita a mano: sube con
 * sp_RegistrarEntrada y baja con sp_RegistrarConsumo, para que cada cambio deje su movimiento.
 * idProveedor, proveedor y version son los que usa la pantalla de Inventario (version = ROWVERSION).
 */
public record Repuesto(int id, String codigo, String nombre, String categoria, int stock, int stockMinimo,
        BigDecimal costo, BigDecimal precioVenta, boolean activo,
        int idProveedor, String proveedor, byte[] version) {

    /** Mismas categorías que el filtro de la pantalla de Inventario. */
    public static final String[] CATEGORIAS = {"Aceite", "Filtro", "Bujía", "Batería", "Freno",
        "Radiador", "Transmisión", "Motor", "Otro"};

    public static final String CATEGORIA_MOTOR = "Motor";

    /** Texto para mostrar en las tablas. */
    public String estadoTexto() { return activo ? "Activo" : "Inactivo"; }

    /** Texto con el que aparece en las listas desplegables. */
    @Override
    public String toString() {
        return nombre + " · stock " + stock + " · " + String.format("%,.2f", precioVenta);
    }
}
