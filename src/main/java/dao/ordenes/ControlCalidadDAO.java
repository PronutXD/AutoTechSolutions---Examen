package dao.ordenes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dao.Conexion;
import model.ordenes.ControlCalidad;

public class ControlCalidadDAO {

    public List<ControlCalidad> listar(int idOrden) throws SQLException {
        String sql = "SELECT c.idControl, c.codigo, c.fecha, m.nombres + N' ' + m.apellidos AS inspector, "
                + "c.resultado, c.pruebaRuta, c.observaciones FROM ControlesCalidad c "
                + "JOIN Mecanicos m ON m.idMecanico = c.idMecanico WHERE c.idOrden = ? ORDER BY c.fecha, c.idControl";
        List<ControlCalidad> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ControlCalidad(rs.getInt("idControl"), rs.getString("codigo"),
                            rs.getTimestamp("fecha").toLocalDateTime(), rs.getString("inspector"),
                            rs.getString("resultado"), rs.getBoolean("pruebaRuta"), rs.getString("observaciones")));
                }
            }
        }
        return lista;
    }

    public void registrar(int idOrden, int idMecanico, String resultado, boolean pruebaRuta, String observaciones)
            throws SQLException {
        String sql = "INSERT INTO ControlesCalidad (idOrden, idMecanico, resultado, pruebaRuta, observaciones) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ps.setInt(2, idMecanico);
            ps.setString(3, resultado);
            ps.setBoolean(4, pruebaRuta);
            ps.setString(5, observaciones == null || observaciones.isBlank() ? null : observaciones.trim());
            ps.executeUpdate();
        }
    }
}
