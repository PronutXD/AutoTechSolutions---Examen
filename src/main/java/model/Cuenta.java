package model;

import java.time.LocalDateTime;

/** Cuenta de acceso al sistema tal como se lista en Catálogos (tabla Usuarios, sin el hash de la clave). */
public record Cuenta(int id, String codigo, String usuario, Integer idMecanico, String mecanico,
        String rol, boolean activo, LocalDateTime ultimoAcceso) {

    public static final String ADMINISTRADOR = "Administrador";
    public static final String MECANICO = "Mecánico";
    public static final String RECEPCION = "Recepción";

    /** Mismos valores que el CHECK de la columna rol en SQL Server. */
    public static final String[] ROLES = {ADMINISTRADOR, MECANICO, RECEPCION};

    public String estadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }
}
