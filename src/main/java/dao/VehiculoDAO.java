package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Vehiculo;

public class VehiculoDAO {

    private static final String SELECT =
            "SELECT v.idVehiculo, v.codigo, v.idCliente, v.placa, v.vin, v.marca, v.modelo, v.anio, "
            + "v.color, v.kilometraje, v.combustible, v.activo, v.version, "
            + "c.nombres + N' ' + c.apellidos AS propietario "
            + "FROM Vehiculos v JOIN Clientes c ON c.idCliente = v.idCliente ";

    /**
     * @param activo      true = solo activos, false = solo inactivos, null = todos
     * @param combustible un valor de Vehiculo.COMBUSTIBLES, o null para todos
     */
    public List<Vehiculo> listar(Boolean activo, String combustible) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1 ");
        if (activo != null) {
            sql.append("AND v.activo = ? ");
        }
        if (combustible != null) {
            sql.append("AND v.combustible = ? ");
        }
        sql.append("ORDER BY v.placa");

        List<Vehiculo> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (activo != null) {
                ps.setBoolean(i++, activo);
            }
            if (combustible != null) {
                ps.setString(i, combustible);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Vehículos activos de un cliente (se usará al crear una orden). */
    public List<Vehiculo> listarPorCliente(int idCliente) throws SQLException {
        List<Vehiculo> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                SELECT + "WHERE v.idCliente = ? AND v.activo = 1 ORDER BY v.placa")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Optional<Vehiculo> buscarPorId(int id) throws SQLException {
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE v.idVehiculo = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** Inserta y deja el id generado dentro del vehículo recibido. */
    public void insertar(Vehiculo v) throws SQLException {
        String sql = "INSERT INTO Vehiculos (idCliente, placa, vin, marca, modelo, anio, color, "
                + "kilometraje, combustible, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getIdCliente());
            ps.setString(2, texto(v.getPlaca()));
            ps.setString(3, texto(v.getVin()));
            ps.setString(4, texto(v.getMarca()));
            ps.setString(5, texto(v.getModelo()));
            ps.setInt(6, v.getAnio());
            ps.setString(7, texto(v.getColor()));
            ps.setInt(8, v.getKilometraje());
            ps.setString(9, v.getCombustible());
            ps.setBoolean(10, v.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    v.setId(keys.getInt(1));
                }
            }
        }
    }

    /** Devuelve false si otro usuario modificó el vehículo desde que se leyó (control con version). */
    public boolean actualizar(Vehiculo v) throws SQLException {
        String sql = "UPDATE Vehiculos SET idCliente = ?, placa = ?, vin = ?, marca = ?, modelo = ?, "
                + "anio = ?, color = ?, kilometraje = ?, combustible = ? WHERE idVehiculo = ? AND version = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, v.getIdCliente());
            ps.setString(2, texto(v.getPlaca()));
            ps.setString(3, texto(v.getVin()));
            ps.setString(4, texto(v.getMarca()));
            ps.setString(5, texto(v.getModelo()));
            ps.setInt(6, v.getAnio());
            ps.setString(7, texto(v.getColor()));
            ps.setInt(8, v.getKilometraje());
            ps.setString(9, v.getCombustible());
            ps.setInt(10, v.getId());
            ps.setBytes(11, v.getVersion());
            return ps.executeUpdate() > 0;
        }
    }

    /** Activa o desactiva (no se borra nada físicamente). */
    public void cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Vehiculos SET activo = ? WHERE idVehiculo = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setId(rs.getInt("idVehiculo"));
        v.setCodigo(rs.getString("codigo"));
        v.setIdCliente(rs.getInt("idCliente"));
        v.setPropietario(rs.getString("propietario"));
        v.setPlaca(rs.getString("placa"));
        v.setVin(rs.getString("vin"));
        v.setMarca(rs.getString("marca"));
        v.setModelo(rs.getString("modelo"));
        v.setAnio(rs.getInt("anio"));
        v.setColor(rs.getString("color"));
        v.setKilometraje(rs.getInt("kilometraje"));
        v.setCombustible(rs.getString("combustible"));
        v.setActivo(rs.getBoolean("activo"));
        v.setVersion(rs.getBytes("version"));
        return v;
    }

    /** Texto vacío o con solo espacios se guarda como NULL. */
    private static String texto(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
