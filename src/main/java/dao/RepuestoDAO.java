package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Repuesto;

/** Por ahora solo lectura: llena la lista de repuestos de las órdenes. */
public class RepuestoDAO {

    /** @param activo true = solo activos, false = solo inactivos, null = todos */
    public List<Repuesto> listar(Boolean activo) throws SQLException {
        String sql = "SELECT idRepuesto, codigo, nombre, categoria, stock, stockMinimo, costo, precioVenta, activo "
                + "FROM Repuestos " + (activo == null ? "" : "WHERE activo = ? ") + "ORDER BY nombre";
        List<Repuesto> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (activo != null) {
                ps.setBoolean(1, activo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Repuesto(rs.getInt("idRepuesto"), rs.getString("codigo"), rs.getString("nombre"),
                            rs.getString("categoria"), rs.getInt("stock"), rs.getInt("stockMinimo"),
                            rs.getBigDecimal("costo"), rs.getBigDecimal("precioVenta"), rs.getBoolean("activo")));
                }
            }
        }
        return lista;
    }
}
