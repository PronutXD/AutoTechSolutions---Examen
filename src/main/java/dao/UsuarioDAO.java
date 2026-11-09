package dao;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import model.Sesion.Usuario;

/**
 * Acceso a la tabla Usuarios. Las contraseñas se guardan con bcrypt (la sal va
 * dentro del hash): la base nunca guarda ni compara la contraseña en texto.
 */
public class UsuarioDAO {

    private static final int COSTO_BCRYPT = 12;

    /** Valida usuario y clave. Vacío si el usuario no existe, está inactivo o la clave es incorrecta. */
    public Optional<Usuario> validar(String usuario, char[] clave) throws SQLException {
        String sql = "SELECT idUsuario, usuario, hashClave, rol, idMecanico, debeCambiarClave "
                + "FROM Usuarios WHERE usuario = ? AND activo = 1";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                String hash = rs.getString("hashClave");
                if (hash == null || hash.isEmpty() || !BCrypt.verifyer().verify(clave, hash).verified) {
                    return Optional.empty();
                }
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

    public void registrarAcceso(int idUsuario) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Usuarios SET ultimoAcceso = SYSDATETIME() WHERE idUsuario = ?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    /** Crea una cuenta. idMecanico puede ser null (administrador o tercero). */
    public void crear(String usuario, char[] clave, String rol, Integer idMecanico) throws SQLException {
        String sql = "INSERT INTO Usuarios (idMecanico, usuario, hashClave, rol) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (idMecanico == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, idMecanico);
            }
            ps.setString(2, usuario);
            ps.setString(3, hashear(clave));
            ps.setString(4, rol);
            ps.executeUpdate();
        }
    }

    /** Cambia la contraseña de una cuenta existente. Devuelve false si el usuario no existe. */
    public boolean cambiarClave(String usuario, char[] claveNueva) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Usuarios SET hashClave = ?, debeCambiarClave = 0 WHERE usuario = ?")) {
            ps.setString(1, hashear(claveNueva));
            ps.setString(2, usuario);
            return ps.executeUpdate() > 0;
        }
    }

    private String hashear(char[] clave) {
        return BCrypt.withDefaults().hashToString(COSTO_BCRYPT, clave);
    }
}
