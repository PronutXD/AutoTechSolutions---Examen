package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Servicio;

/**
 * Servicios del catálogo. La tabla no tiene columna version, así que no hay control de
 * ediciones simultáneas. Cambiar un precio no afecta órdenes ya hechas: cada línea guarda el suyo.
 */
public class ServicioDAO {

    /** @param activo true = solo activos, false = solo inactivos, null = todos */
    public List<Servicio> listar(Boolean activo) throws SQLException {
        return listar(activo, null);
    }

    /**
     * @param activo true = solo activos, false = solo inactivos, null = todos
     * @param nivel  del 1 al 5, o null para todos
     */
    public List<Servicio> listar(Boolean activo, Integer nivel) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT idServicio, codigo, nombre, nivel, manoObra, horasEstimadas, garantiaDias, activo "
                + "FROM Servicios WHERE 1 = 1 ");
        if (activo != null) {
            sql.append("AND activo = ? ");
        }
        if (nivel != null) {
            sql.append("AND nivel = ? ");
        }
        sql.append("ORDER BY nombre");
        List<Servicio> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (activo != null) {
                ps.setBoolean(i++, activo);
            }
            if (nivel != null) {
                ps.setInt(i, nivel);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Servicio(rs.getInt("idServicio"), rs.getString("codigo"), rs.getString("nombre"),
                            rs.getInt("nivel"), rs.getBigDecimal("manoObra"), rs.getBigDecimal("horasEstimadas"),
                            rs.getInt("garantiaDias"), rs.getBoolean("activo")));
                }
            }
        }
        return lista;
    }

    /** Inserta y devuelve el id generado (el id del Servicio recibido se ignora). */
    public int insertar(Servicio s) throws SQLException {
        String sql = "INSERT INTO Servicios (nombre, nivel, manoObra, horasEstimadas, garantiaDias, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.nombre().trim());
            ps.setInt(2, s.nivel());
            ps.setBigDecimal(3, s.manoObra());
            ps.setBigDecimal(4, s.horasEstimadas());
            ps.setInt(5, s.garantiaDias());
            ps.setBoolean(6, s.activo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public void actualizar(Servicio s) throws SQLException {
        String sql = "UPDATE Servicios SET nombre = ?, nivel = ?, manoObra = ?, horasEstimadas = ?, "
                + "garantiaDias = ? WHERE idServicio = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.nombre().trim());
            ps.setInt(2, s.nivel());
            ps.setBigDecimal(3, s.manoObra());
            ps.setBigDecimal(4, s.horasEstimadas());
            ps.setInt(5, s.garantiaDias());
            ps.setInt(6, s.id());
            ps.executeUpdate();
        }
    }

    /** Activa o desactiva (no se borra nada físicamente). */
    public void cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Servicios SET activo = ? WHERE idServicio = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
