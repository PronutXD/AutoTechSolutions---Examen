package model;

import java.math.BigDecimal;

/** Servicio del catálogo (tabla Servicios). */
public record Servicio(int id, String codigo, String nombre, int nivel, BigDecimal manoObra,
        BigDecimal horasEstimadas, int garantiaDias, boolean activo) {

    /** Texto con el que aparece en las listas desplegables. */
    @Override
    public String toString() {
        return nombre + " · " + String.format("%,.2f", manoObra);
    }
}
