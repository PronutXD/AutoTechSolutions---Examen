package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import model.Sesion.Usuario;

public class UsuarioDAO {

    /** Valida usuario y clave con sp_ValidarLogin. Vacío si son incorrectos. */
    public Optional<Usuario> validar(String usuario, char[] clave) throws SQLException {
        try (Connection con = Conexion.abrir();
             CallableStatement cs = con.prepareCall("{call sp_ValidarLogin(?, ?)}")) {
            cs.setString(1, usuario);
            cs.setString(2, new String(clave));
            if (!cs.execute()) {
                return Optional.empty();
            }
            try (ResultSet rs = cs.getResultSet()) {
                if (rs.next()) {
                    int idMec = rs.getInt("idMecanico");
                    Integer idMecanico = rs.wasNull() ? null : idMec;
                    return Optional.of(new Usuario(
                            rs.getInt("idUsuario"),
                            rs.getString("usuario"),
                            rs.getString("rol"),
                            idMecanico,
                            rs.getBoolean("debeCambiarClave")));
                }
            }
        }
        return Optional.empty();
    }
}