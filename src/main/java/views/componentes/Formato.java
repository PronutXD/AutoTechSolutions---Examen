package views.componentes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Cómo se escriben los montos y las fechas en las tablas y etiquetas. */
public final class Formato {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formato() { }

    public static String dinero(BigDecimal monto) {
        return monto == null ? "" : String.format("%,.2f", monto);
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FECHA_HORA);
    }
}
