package model;

import java.math.BigDecimal;

/** Totales de una orden (vista vw_SaldoOrdenes). */
public record SaldoOrden(BigDecimal subtotal, BigDecimal descuento, BigDecimal impuesto,
        BigDecimal total, BigDecimal pagado, BigDecimal saldo) { }
