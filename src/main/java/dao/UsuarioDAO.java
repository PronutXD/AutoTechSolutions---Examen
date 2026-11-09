package dao;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Cuenta;
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

    /**
     * Lista las cuentas para Catálogos (nunca incluye el hash de la clave).
     * @param activo true = solo activas, false = solo inactivas, null = todas
     * @param rol    un valor de Cuenta.ROLES, o null para todos
     */
    public List<Cuenta> listar(Boolean activo, String rol) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT u.idUsuario, u.codigo, u.usuario, u.idMecanico, m.nombres + N' ' + m.apellidos AS mecanico, "
                + "u.rol, u.activo, u.ultimoAcceso FROM Usuarios u "
                + "LEFT JOIN Mecanicos m ON m.idMecanico = u.idMecanico WHERE 1 = 1 ");
        if (activo != null) {
            sql.append("AND u.activo = ? ");
        }
        if (rol != null) {
            sql.append("AND u.rol = ? ");
        }
        sql.append("ORDER BY u.usuario");
        List<Cuenta> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (activo != null) {
                ps.setBoolean(i++, activo);
            }
            if (rol != null) {
                ps.setString(i, rol);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int mec = rs.getInt("idMecanico");
                    Integer idMecanico = rs.wasNull() ? null : mec;
                    Timestamp acceso = rs.getTimestamp("ultimoAcceso");
                    lista.add(new Cuenta(rs.getInt("idUsuario"), rs.getString("codigo"), rs.getString("usuario"),
                            idMecanico, rs.getString("mecanico"), rs.getString("rol"), rs.getBoolean("activo"),
                            acceso == null ? null : acceso.toLocalDateTime()));
                }
            }
        }
        return lista;
    }

    /** Cambia el nombre de usuario, el rol y el mecánico asociado (null = ninguno). No toca la contraseña. */
    public void actualizar(int idUsuario, String usuario, String rol, Integer idMecanico) throws SQLException {
        String sql = "UPDATE Usuarios SET usuario = ?, rol = ?, idMecanico = ? WHERE idUsuario = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, rol);
            if (idMecanico == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, idMecanico);
            }
            ps.setInt(4, idUsuario);
            ps.executeUpdate();
        }
    }

    /** Activa o desactiva una cuenta (no se borra nada físicamente). */
    public void cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Usuarios SET activo = ? WHERE idUsuario = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    /** El administrador pone una clave nueva; la cuenta queda marcada para que su dueño la cambie. */
    public void restablecerClave(int idUsuario, char[] claveNueva) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Usuarios SET hashClave = ?, debeCambiarClave = 1 WHERE idUsuario = ?")) {
            ps.setString(1, hashear(claveNueva));
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    private String hashear(char[] clave) {
        return BCrypt.withDefaults().hashToString(COSTO_BCRYPT, clave);
    }
}
