package model;

import java.time.LocalDateTime;

/** Revisión de calidad de una orden (tabla ControlesCalidad). */
public record ControlCalidad(int id, String codigo, LocalDateTime fecha, String inspector, String resultado,
        boolean pruebaRuta, String observaciones) {

    public static final String APROBADO = "Aprobado";
    public static final String RECHAZADO = "Rechazado";
}
