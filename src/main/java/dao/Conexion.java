package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import model.Sesion;

/** Abre conexiones a SQL Server (base AutoTechManager). */
public final class Conexion {

    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=AutoTechManager;encrypt=true;trustServerCertificate=true";
    private static final String USUARIO = "autotech_app";
    private static final String CLAVE = "AutoTech#2026";   // la que pusiste en el script SQL

    private Conexion() { }

    /**
     * Devuelve una conexión nueva. Si ya hay un usuario logueado, lo guarda en
     * SESSION_CONTEXT para que los triggers de auditoría sepan quién hizo el cambio.
     * Usar siempre con try-with-resources.
     */
    public static Connection abrir() throws SQLException {
        Connection con = DriverManager.getConnection(URL, USUARIO, CLAVE);
        if (Sesion.actual() != null) {
            try (CallableStatement cs = con.prepareCall("{call sp_set_session_context(?, ?)}")) {
                cs.setString(1, "idUsuario");
                cs.setInt(2, Sesion.actual().id());
                cs.execute();
            }
        }
        return con;
    }
}