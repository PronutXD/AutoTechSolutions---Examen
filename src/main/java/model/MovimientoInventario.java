package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Movimiento de almacén (tabla MovimientosInventario). Solo lectura: lo generan las
 * entradas de repuestos y los consumos de las órdenes. origen es el proveedor (entrada)
 * o el código de la orden (salida).
 */
public record MovimientoInventario(int id, String codigo, LocalDateTime fecha, String repuesto, String tipo,
        int cantidad, BigDecimal costoUnitario, String origen, String usuario) { }
