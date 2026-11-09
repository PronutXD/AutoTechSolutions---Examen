package dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.Motor;

public class MotorDAO {

    private static final String SELECT =
            "SELECT m.idMotor, m.codigo, m.idRepuesto, m.numeroSerie, m.marca, m.modeloCompatible, "
            + "m.anioDesde, m.anioHasta, m.cilindraje, m.combustible, m.estado, "
            + "r.nombre AS repuesto, r.precioVenta "
            + "FROM Motores m JOIN Repuestos r ON r.idRepuesto = m.idRepuesto ";

    /**
     * @param combustible un valor de Vehiculo.COMBUSTIBLES, o null para todos
     * @param estado      un valor de Motor.ESTADOS, o null para todos
     */
    public List<Motor> listar(String combustible, String estado) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1 ");
        if (combustible != null) {
            sql.append("AND m.combustible = ? ");
        }
        if (estado != null) {
            sql.append("AND m.estado = ? ");
        }
        sql.append("ORDER BY m.marca, m.modeloCompatible, m.cilindraje");

        List<Motor> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (combustible != null) {
                ps.setString(i++, combustible);
            }
            if (estado != null) {
                ps.setString(i, estado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Inserta la ficha (estado Disponible) y deja el id generado dentro del motor recibido. */
    public void insertar(Motor m) throws SQLException {
        String sql = "INSERT INTO Motores (idRepuesto, numeroSerie, marca, modeloCompatible, anioDesde, "
                + "anioHasta, cilindraje, combustible, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getIdRepuesto());
            ps.setString(2, texto(m.getNumeroSerie()));
            ps.setString(3, texto(m.getMarca()));
            ps.setString(4, texto(m.getModeloCompatible()));
            ps.setInt(5, m.getAnioDesde());
            ps.setInt(6, m.getAnioHasta());
            ps.setBigDecimal(7, m.getCilindraje());
            ps.setString(8, m.getCombustible());
            ps.setString(9, Motor.DISPONIBLE);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setId(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Actualiza la ficha técnica. No toca el estado (Instalado / Retirado los maneja
     * sp_CambiarMotor) ni el repuesto enlazado. La tabla no tiene columna version.
     */
    public void actualizar(Motor m) throws SQLException {
        String sql = "UPDATE Motores SET numeroSerie = ?, marca = ?, modeloCompatible = ?, anioDesde = ?, "
                + "anioHasta = ?, cilindraje = ?, combustible = ? WHERE idMotor = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, texto(m.getNumeroSerie()));
            ps.setString(2, texto(m.getMarca()));
            ps.setString(3, texto(m.getModeloCompatible()));
            ps.setInt(4, m.getAnioDesde());
            ps.setInt(5, m.getAnioHasta());
            ps.setBigDecimal(6, m.getCilindraje());
            ps.setString(7, m.getCombustible());
            ps.setInt(8, m.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Motores disponibles compatibles con los datos dados (sp_BuscarMotoresCompatibles).
     * Cualquier criterio puede ir en null y entonces no filtra.
     */
    public List<Motor> buscarCompatibles(String marca, Integer anio, BigDecimal cilindraje, String combustible)
            throws SQLException {
        List<Motor> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir();
             CallableStatement cs = con.prepareCall("{call sp_BuscarMotoresCompatibles(?, ?, ?, ?)}")) {
            if (marca == null) {
                cs.setNull(1, Types.NVARCHAR);
            } else {
                cs.setString(1, marca);
            }
            if (anio == null) {
                cs.setNull(2, Types.SMALLINT);
            } else {
                cs.setShort(2, anio.shortValue());
            }
            if (cilindraje == null) {
                cs.setNull(3, Types.DECIMAL);
            } else {
                cs.setBigDecimal(3, cilindraje);
            }
            if (combustible == null) {
                cs.setNull(4, Types.NVARCHAR);
            } else {
                cs.setString(4, combustible);
            }
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Motor m = new Motor();
                    m.setId(rs.getInt("idMotor"));
                    m.setCodigo(rs.getString("codigo"));
                    m.setNumeroSerie(rs.getString("numeroSerie"));
                    m.setMarca(rs.getString("marca"));
                    m.setModeloCompatible(rs.getString("modeloCompatible"));
                    m.setAnioDesde(rs.getInt("anioDesde"));
                    m.setAnioHasta(rs.getInt("anioHasta"));
                    m.setCilindraje(rs.getBigDecimal("cilindraje"));
                    m.setCombustible(rs.getString("combustible"));
                    m.setRepuesto(rs.getString("repuesto"));
                    m.setPrecioVenta(rs.getBigDecimal("precioVenta"));
                    m.setEstado("Disponible");
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    private Motor mapear(ResultSet rs) throws SQLException {
        Motor m = new Motor();
        m.setId(rs.getInt("idMotor"));
        m.setCodigo(rs.getString("codigo"));
        m.setIdRepuesto(rs.getInt("idRepuesto"));
        m.setNumeroSerie(rs.getString("numeroSerie"));
        m.setMarca(rs.getString("marca"));
        m.setModeloCompatible(rs.getString("modeloCompatible"));
        m.setAnioDesde(rs.getInt("anioDesde"));
        m.setAnioHasta(rs.getInt("anioHasta"));
        m.setCilindraje(rs.getBigDecimal("cilindraje"));
        m.setCombustible(rs.getString("combustible"));
        m.setEstado(rs.getString("estado"));
        m.setRepuesto(rs.getString("repuesto"));
        m.setPrecioVenta(rs.getBigDecimal("precioVenta"));
        return m;
    }

    /** Texto vacío o con solo espacios se guarda como NULL. */
    private static String texto(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
