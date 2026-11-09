package dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Repuesto;

public class RepuestoDAO {

    private static final String SELECT =
            "SELECT r.idRepuesto, r.codigo, r.nombre, r.categoria, r.stock, r.stockMinimo, r.costo, "
            + "r.precioVenta, r.activo, r.idProveedor, p.nombre AS proveedor, r.version "
            + "FROM Repuestos r LEFT JOIN Proveedores p ON p.idProveedor = r.idProveedor ";

    /** Lista simple para las órdenes. @param activo true = solo activos, false = solo inactivos, null = todos */
    public List<Repuesto> listar(Boolean activo) throws SQLException {
        return listar(activo, null, false);
    }

    /**
     * @param activo         true = solo activos, false = solo inactivos, null = todos
     * @param categoria      un valor de Repuesto.CATEGORIAS, o null para todas
     * @param soloStockBajo  true = solo los que están por debajo de su stock mínimo
     */
    public List<Repuesto> listar(Boolean activo, String categoria, boolean soloStockBajo) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1 ");
        if (activo != null) {
            sql.append("AND r.activo = ? ");
        }
        if (categoria != null) {
            sql.append("AND r.categoria = ? ");
        }
        if (soloStockBajo) {
            sql.append("AND r.stock < r.stockMinimo ");
        }
        sql.append("ORDER BY r.nombre");

        List<Repuesto> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (activo != null) {
                ps.setBoolean(i++, activo);
            }
            if (categoria != null) {
                ps.setString(i, categoria);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Optional<Repuesto> buscarPorId(int id) throws SQLException {
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE r.idRepuesto = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Repuestos activos de categoría Motor que todavía no tienen ficha técnica en Motores
     * (más el indicado, si se está editando su ficha). Se usa en el formulario de motor.
     */
    public List<Repuesto> listarMotoresSinFicha(Integer idIncluir) throws SQLException {
        String sql = SELECT + "WHERE r.categoria = ? AND r.activo = 1 AND (r.idRepuesto = ? OR NOT EXISTS "
                + "(SELECT 1 FROM Motores m WHERE m.idRepuesto = r.idRepuesto)) ORDER BY r.nombre";
        List<Repuesto> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Repuesto.CATEGORIA_MOTOR);
            ps.setInt(2, idIncluir == null ? 0 : idIncluir);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Inserta con stock 0 (el stock inicial se carga con una entrada, que deja su movimiento)
     * y devuelve el id generado.
     */
    public int insertar(Repuesto r) throws SQLException {
        String sql = "INSERT INTO Repuestos (idProveedor, nombre, categoria, stock, stockMinimo, costo, "
                + "precioVenta, activo) VALUES (?, ?, ?, 0, ?, ?, ?, ?)";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.idProveedor());
            ps.setString(2, r.nombre());
            ps.setString(3, r.categoria());
            ps.setInt(4, r.stockMinimo());
            ps.setBigDecimal(5, r.costo());
            ps.setBigDecimal(6, r.precioVenta());
            ps.setBoolean(7, r.activo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    /**
     * Actualiza los datos del repuesto (no el stock). "AND version = ?" es el control de
     * concurrencia: devuelve false si otro usuario cambió la fila desde que se leyó.
     */
    public boolean actualizar(Repuesto r) throws SQLException {
        String sql = "UPDATE Repuestos SET idProveedor = ?, nombre = ?, categoria = ?, stockMinimo = ?, "
                + "costo = ?, precioVenta = ? WHERE idRepuesto = ? AND version = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.idProveedor());
            ps.setString(2, r.nombre());
            ps.setString(3, r.categoria());
            ps.setInt(4, r.stockMinimo());
            ps.setBigDecimal(5, r.costo());
            ps.setBigDecimal(6, r.precioVenta());
            ps.setInt(7, r.id());
            ps.setBytes(8, r.version());
            return ps.executeUpdate() > 0;
        }
    }

    /** Activa o desactiva (no se borra nada físicamente). */
    public void cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE Repuestos SET activo = ? WHERE idRepuesto = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Registra una entrada de almacén con sp_RegistrarEntrada (suma el stock y deja el movimiento). */
    public void registrarEntrada(int idRepuesto, int cantidad, BigDecimal costoUnitario, int idProveedor,
            int idUsuario) throws SQLException {
        try (Connection con = Conexion.abrir();
             CallableStatement cs = con.prepareCall("{call sp_RegistrarEntrada(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, idRepuesto);
            cs.setInt(2, cantidad);
            cs.setBigDecimal(3, costoUnitario);
            cs.setInt(4, idProveedor);
            cs.setInt(5, idUsuario);
            cs.execute();
        }
    }

    private Repuesto mapear(ResultSet rs) throws SQLException {
        return new Repuesto(rs.getInt("idRepuesto"), rs.getString("codigo"), rs.getString("nombre"),
                rs.getString("categoria"), rs.getInt("stock"), rs.getInt("stockMinimo"),
                rs.getBigDecimal("costo"), rs.getBigDecimal("precioVenta"), rs.getBoolean("activo"),
                rs.getInt("idProveedor"), rs.getString("proveedor"), rs.getBytes("version"));
    }
}
