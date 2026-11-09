package model;

import java.math.BigDecimal;

/** Servicio del catálogo (tabla Servicios). */
public record Servicio(int id, String codigo, String nombre, int nivel, BigDecimal manoObra,
        BigDecimal horasEstimadas, int garantiaDias, boolean activo) {

    /** Nombre de cada nivel de dificultad (el CHECK de la columna nivel acepta del 1 al 5). */
    public static final String[] NIVELES = {"1 · Básico", "2 · Preventivo", "3 · Intermedio", "4 · Avanzado", "5 · Mayor"};

    public String nivelTexto() {
        return NIVELES[nivel - 1];
    }

    public String estadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }

    /** Texto con el que aparece en las listas desplegables. */
    @Override
    public String toString() {
        return nombre + " · " + String.format("%,.2f", manoObra);
    }
}
