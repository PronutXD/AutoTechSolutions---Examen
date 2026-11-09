package model.consultas;

import java.math.BigDecimal;

/** Total, pagado y saldo de una orden (vw_SaldoOrdenes), con cliente, vehículo y estado de vw_ListadoOrdenes. */
public record SaldoPorOrden(String orden, String cliente, String vehiculo, BigDecimal total, BigDecimal pagado,
        BigDecimal saldo, String estado) { }
