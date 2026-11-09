package model;

/** Una fila de vw_RepuestosBajoMinimo. */
public record RepuestoBajoMinimo(String codigo, String nombre, String categoria, int stock, int stockMinimo,
        int faltante, String proveedor) { }
