package dao;

import java.sql.SQLException;

/** Convierte los errores de SQL Server en mensajes que se le puedan mostrar al usuario. */
public final class ErroresSQL {

    private ErroresSQL() { }

    /**
     * @param accion      lo que se intentaba hacer ("guardar el cliente")
     * @param campoUnico  dato que no puede repetirse ("documento"), para el error de duplicado
     */
    public static String mensaje(SQLException e, String accion, String campoUnico) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        int codigo = e.getErrorCode();

        if (estado.startsWith("08")) {
            return "No se pudo conectar con la base de datos. Revise que SQL Server esté en ejecución.";
        }
        if (codigo == 18456 || codigo == 4060) {
            return "SQL Server rechazó la conexión (usuario, clave o base de datos incorrectos en Conexion.java).";
        }
        if (codigo == 2627 || codigo == 2601) {
            return "No se pudo " + accion + ": ya existe un registro con ese " + campoUnico + ".";
        }
        if (codigo == 547) {
            return "No se pudo " + accion + ": el dato está relacionado con otros registros o no es válido.";
        }
        return "No se pudo " + accion + ": " + e.getMessage();
    }
}
