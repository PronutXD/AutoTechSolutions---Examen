package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Mecanico;

/** Mecánicos del taller. La tabla no tiene columna version, así que no hay control de ediciones simultáneas. */
public class MecanicoDAO {

    private static final String SELECT =
            "SELECT m.idMecanico, m.codigo, m.documento, m.nombres, m.apellidos, m.telefono, "
            + "m.especialidad, m.activo, m.disponible FROM Mecanicos m ";

    /** @param activo true = solo activos, false = solo inactivos, null = todos */
    public List<Mecanico> listar(Boolean activo) throws SQLException {
        return listar(activo, null);
    }

    /**
     * @param activo      true = solo activos, false = solo inactivos, null = todos
     * @param disponible  true = solo disponibles, false = solo no disponibles, null = todos
     */
    public List<Mecanico> listar(Boolean activo, Boolean disponible) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1 ");
        if (activo != null) {
            sql.append("AND m.activo = ? ");
        }
        if (disponible != null) {
            sql.append("AND m.disponible = ? ");
        }
        sql.append("ORDER BY m.apellidos, m.nombres");
        List<Mecanico> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (activo != null) {
                ps.setBoolean(i++, activo);
            }
            if (disponible != null) {
                ps.setBoolean(i, disponible);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Mecánicos activos que todavía no tienen cuenta de acceso (los que se ofrecen al crear un usuario).
     * @param incluirId un mecánico que debe aparecer aunque ya tenga cuenta (el de la cuenta que se edita), o null
     */
    public List<Mecanico> listarSinCuenta(Integer incluirId) throws SQLException {
        String sql = SELECT + "WHERE m.activo = 1 AND (NOT EXISTS (SELECT 1 FROM Usuarios u WHERE u.idMecanico = m.idMecanico) "
                + "OR m.idMecanico = ?) ORDER BY m.apellidos, m.nombres";
        List<Mecanico> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (incluirId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, incluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Inserta y deja el id generado dentro del mecánico recibido. */
    public void insertar(Mecanico m) throws SQLException {
        String sql = "INSERT INTO Mecanicos (documento, nombres, apellidos, telefono, especialidad, activo, disponible) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, m);
            ps.setBoolean(6, m.isActivo());
            ps.setBoolean(7, m.isDisponible());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setId(keys.getInt(1));
                }
            }
        }
    }

    public void actualizar(Mecanico m) throws SQLException {
        String sql = "UPDATE Mecanicos SET documento = ?, nombres = ?, apellidos = ?, telefono = ?, "
                + "especialidad = ?, disponible = ? WHERE idMecanico = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            asignar(ps, m);
            ps.setBoolean(6, m.isDisponible());
            ps.setInt(7, m.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Activa o desactiva. Al desactivar, la cuenta de acceso del mecánico (si tiene) se desactiva
     * en la misma transacción, para que no pueda seguir entrando al sistema.
     *
     * @return cuántas cuentas de acceso se desactivaron (0 o 1)
     */
    public int cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection con = Conexion.abrir()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE Mecanicos SET activo = ? WHERE idMecanico = ?")) {
                    ps.setBoolean(1, activo);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                int cuentas = 0;
                if (!activo) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE Usuarios SET activo = 0 WHERE idMecanico = ? AND activo = 1")) {
                        ps.setInt(1, id);
                        cuentas = ps.executeUpdate();
                    }
                }
                con.commit();
                return cuentas;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static void asignar(PreparedStatement ps, Mecanico m) throws SQLException {
        ps.setString(1, texto(m.getDocumento()));
        ps.setString(2, texto(m.getNombres()));
        ps.setString(3, texto(m.getApellidos()));
        ps.setString(4, texto(m.getTelefono()));
        ps.setString(5, texto(m.getEspecialidad()));
    }

    private static String texto(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Mecanico mapear(ResultSet rs) throws SQLException {
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
        return m;
    }
}
