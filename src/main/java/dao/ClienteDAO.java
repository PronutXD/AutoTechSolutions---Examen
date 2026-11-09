package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Cliente;

public class ClienteDAO {

    private static final String SELECT =
            "SELECT c.idCliente, c.codigo, c.documento, c.nombres, c.apellidos, c.telefono, "
            + "c.correo, c.direccion, c.activo, c.version, "
            + "(SELECT COUNT(*) FROM Vehiculos v WHERE v.idCliente = c.idCliente AND v.activo = 1) AS totalVehiculos "
            + "FROM Clientes c ";

    /** @param activo true = solo activos, false = solo inactivos, null = todos */
    public List<Cliente> listar(Boolean activo) throws SQLException {
        String sql = SELECT + (activo == null ? "" : "WHERE c.activo = ? ") + "ORDER BY c.apellidos, c.nombres";
        List<Cliente> lista = new ArrayList<>();
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

    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE c.idCliente = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** Inserta y deja el id generado dentro del cliente recibido. */
    public void insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO Clientes (documento, nombres, apellidos, telefono, correo, direccion, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, texto(c.getDocumento()));
            ps.setString(2, texto(c.getNombres()));
            ps.setString(3, texto(c.getApellidos()));
            ps.setString(4, texto(c.getTelefono()));
            ps.setString(5, texto(c.getCorreo()));
            ps.setString(6, texto(c.getDireccion()));
            ps.setBoolean(7, c.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Actualiza el cliente. "AND version = ?" es el control de concurrencia: si otro
     * usuario cambió la fila desde que se leyó, no actualiza nada y devuelve false.
     */
    public boolean actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE Clientes SET documento = ?, nombres = ?, apellidos = ?, telefono = ?, "
                + "correo = ?, direccion = ? WHERE idCliente = ? AND version = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, texto(c.getDocumento()));
            ps.setString(2, texto(c.getNombres()));
            ps.setString(3, texto(c.getApellidos()));
            ps.setString(4, texto(c.getTelefono()));
            ps.setString(5, texto(c.getCorreo()));
            ps.setString(6, texto(c.getDireccion()));
            ps.setInt(7, c.getId());
            ps.setBytes(8, c.getVersion());
            return ps.executeUpdate() > 0;
        }
    }

    /** Activa o desactiva (no se borra nada físicamente). */
    public void cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Clientes SET activo = ? WHERE idCliente = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("idCliente"));
        c.setCodigo(rs.getString("codigo"));
        c.setDocumento(rs.getString("documento"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setTelefono(rs.getString("telefono"));
        c.setCorreo(rs.getString("correo"));
        c.setDireccion(rs.getString("direccion"));
        c.setActivo(rs.getBoolean("activo"));
        c.setVersion(rs.getBytes("version"));
        c.setTotalVehiculos(rs.getInt("totalVehiculos"));
        return c;
    }

    /** Texto vacío o con solo espacios se guarda como NULL. */
    private static String texto(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
