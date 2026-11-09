package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Proveedor;

public class ProveedorDAO {

    private static final String SELECT =
            "SELECT idProveedor, codigo, nombre, contacto, telefono, correo, activo FROM Proveedores ";

    /** @param activo true = solo activos, false = solo inactivos, null = todos */
    public List<Proveedor> listar(Boolean activo) throws SQLException {
        String sql = SELECT + (activo == null ? "" : "WHERE activo = ? ") + "ORDER BY nombre";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (activo != null) {
                ps.setBoolean(1, activo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Inserta y deja el id generado dentro del proveedor recibido. */
    public void insertar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO Proveedores (nombre, contacto, telefono, correo, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, texto(p.getNombre()));
            ps.setString(2, texto(p.getContacto()));
            ps.setString(3, texto(p.getTelefono()));
            ps.setString(4, texto(p.getCorreo()));
            ps.setBoolean(5, p.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        }
    }

    /** La tabla no tiene columna version, así que no hay control de ediciones simultáneas. */
    public void actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE Proveedores SET nombre = ?, contacto = ?, telefono = ?, correo = ? WHERE idProveedor = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, texto(p.getNombre()));
            ps.setString(2, texto(p.getContacto()));
            ps.setString(3, texto(p.getTelefono()));
            ps.setString(4, texto(p.getCorreo()));
            ps.setInt(5, p.getId());
            ps.executeUpdate();
        }
    }

    /** Activa o desactiva (no se borra nada físicamente). */
    public void cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Proveedores SET activo = ? WHERE idProveedor = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("idProveedor"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setContacto(rs.getString("contacto"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }

    /** Texto vacío o con solo espacios se guarda como NULL. */
    private static String texto(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
