package model.ordenes;

import java.math.BigDecimal;

/** Una línea de DetalleOrden: un servicio o un repuesto cobrado en la orden (con el precio de ese momento). */
public record LineaOrden(int id, String tipo, String descripcion, int cantidad, BigDecimal precioUnitario) {

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
