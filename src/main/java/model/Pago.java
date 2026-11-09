package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pago registrado en una orden (tabla Pagos). */
public record Pago(int id, String codigo, LocalDateTime fecha, BigDecimal monto, String metodo,
        String referencia, String registradoPor) {

    public static final String[] METODOS = {"Efectivo", "Tarjeta", "Transferencia"};
}
