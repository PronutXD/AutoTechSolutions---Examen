package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Servicio;

/** Por ahora solo lectura: llena la lista de servicios de las órdenes. */
public class ServicioDAO {

    /** @param activo true = solo activos, false = solo inactivos, null = todos */
    public List<Servicio> listar(Boolean activo) throws SQLException {
        String sql = "SELECT idServicio, codigo, nombre, nivel, manoObra, horasEstimadas, garantiaDias, activo "
                + "FROM Servicios " + (activo == null ? "" : "WHERE activo = ? ") + "ORDER BY nombre";
        List<Servicio> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (activo != null) {
                ps.setBoolean(1, activo);
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
}
