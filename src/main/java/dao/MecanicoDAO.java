package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Mecanico;

/** Por ahora solo lectura: llena las listas de mecánicos. La pantalla de Mecánicos agregará el resto. */
public class MecanicoDAO {

    /** @param activo true = solo activos, false = solo inactivos, null = todos */
    public List<Mecanico> listar(Boolean activo) throws SQLException {
        String sql = "SELECT idMecanico, codigo, documento, nombres, apellidos, telefono, especialidad, "
                + "activo, disponible FROM Mecanicos " + (activo == null ? "" : "WHERE activo = ? ")
                + "ORDER BY apellidos, nombres";
        List<Mecanico> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (activo != null) {
                ps.setBoolean(1, activo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Mecanico m = new Mecanico();
                    m.setId(rs.getInt("idMecanico"));
                    m.setCodigo(rs.getString("codigo"));
                    m.setDocumento(rs.getString("documento"));
                    m.setNombres(rs.getString("nombres"));
                    m.setApellidos(rs.getString("apellidos"));
                    m.setTelefono(rs.getString("telefono"));
                    m.setEspecialidad(rs.getString("especialidad"));
                    m.setActivo(rs.getBoolean("activo"));
                    m.setDisponible(rs.getBoolean("disponible"));
                    lista.add(m);
                }
            }
        }
        return lista;
    }
}
