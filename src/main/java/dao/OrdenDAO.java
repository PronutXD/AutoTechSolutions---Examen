package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Orden;
import model.SaldoOrden;

public class OrdenDAO {

    /**
     * Lista para la tabla de órdenes (vista vw_ListadoOrdenes).
     * @param estado un valor de Orden.ESTADOS, o null para todas
     */
    public List<Orden> listar(String estado) throws SQLException {
        String sql = "SELECT idOrden, codigo, fechaIngreso, cliente, vehiculo, responsable, estado, total "
                + "FROM vw_ListadoOrdenes " + (estado == null ? "" : "WHERE estado = ? ")
                + "ORDER BY fechaIngreso DESC, idOrden DESC";
        List<Orden> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (estado != null) {
                ps.setString(1, estado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Orden o = new Orden();
                    o.setId(rs.getInt("idOrden"));
                    o.setCodigo(rs.getString("codigo"));
                    o.setFechaIngreso(rs.getTimestamp("fechaIngreso").toLocalDateTime());
                    o.setCliente(rs.getString("cliente"));
                    o.setVehiculo(rs.getString("vehiculo"));
                    o.setResponsable(rs.getString("responsable"));
                    o.setEstado(rs.getString("estado"));
                    o.setTotal(rs.getBigDecimal("total"));
                    lista.add(o);
                }
            }
        }
        return lista;
    }

    public Optional<Orden> buscarPorId(int id) throws SQLException {
        String sql = "SELECT o.idOrden, o.codigo, o.idVehiculo, o.idUsuario, o.fechaIngreso, "
                + "o.kilometrajeIngreso, o.motivo, o.estado, o.descuento, o.porcentajeImpuesto, "
                + "o.fechaEntrega, o.version, c.nombres + N' ' + c.apellidos AS cliente, "
                + "v.marca + N' ' + v.modelo + N' (' + v.placa + N')' AS vehiculo "
                + "FROM OrdenesTrabajo o JOIN Vehiculos v ON v.idVehiculo = o.idVehiculo "
                + "JOIN Clientes c ON c.idCliente = v.idCliente WHERE o.idOrden = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Orden o = new Orden();
                o.setId(rs.getInt("idOrden"));
                o.setCodigo(rs.getString("codigo"));
                o.setIdVehiculo(rs.getInt("idVehiculo"));
                o.setIdUsuario(rs.getInt("idUsuario"));
                o.setFechaIngreso(rs.getTimestamp("fechaIngreso").toLocalDateTime());
                o.setKilometrajeIngreso(rs.getInt("kilometrajeIngreso"));
                o.setMotivo(rs.getString("motivo"));
                o.setEstado(rs.getString("estado"));
                o.setDescuento(rs.getBigDecimal("descuento"));
                o.setPorcentajeImpuesto(rs.getBigDecimal("porcentajeImpuesto"));
                Timestamp entrega = rs.getTimestamp("fechaEntrega");
                o.setFechaEntrega(entrega == null ? null : entrega.toLocalDateTime());
                o.setVersion(rs.getBytes("version"));
                o.setCliente(rs.getString("cliente"));
                o.setVehiculo(rs.getString("vehiculo"));
                return Optional.of(o);
            }
        }
    }

    /**
     * Crea la orden y asigna sus mecánicos en una sola transacción: o se guarda todo o nada.
     * Deja el id generado dentro de la orden.
     *
     * @param idResponsable    mecánico responsable de la orden
     * @param idsAdicionales   otros mecánicos asignados (puede ir vacía)
     */
    public void insertar(Orden o, int idResponsable, List<Integer> idsAdicionales) throws SQLException {
        String sqlOrden = "INSERT INTO OrdenesTrabajo (idVehiculo, idUsuario, kilometrajeIngreso, motivo, "
                + "descuento, porcentajeImpuesto) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlMecanico = "INSERT INTO OrdenMecanicos (idOrden, idMecanico, esResponsable) VALUES (?, ?, ?)";
        try (Connection con = Conexion.abrir()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, o.getIdVehiculo());
                    ps.setInt(2, o.getIdUsuario());
                    ps.setInt(3, o.getKilometrajeIngreso());
                    String motivo = o.getMotivo() == null ? null : o.getMotivo().trim();
                    ps.setString(4, motivo == null || motivo.isEmpty() ? null : motivo);
                    ps.setBigDecimal(5, o.getDescuento());
                    ps.setBigDecimal(6, o.getPorcentajeImpuesto());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            o.setId(keys.getInt(1));
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(sqlMecanico)) {
                    ps.setInt(1, o.getId());
                    ps.setInt(2, idResponsable);
                    ps.setBoolean(3, true);
                    ps.addBatch();
                    for (int idMecanico : idsAdicionales) {
                        if (idMecanico != idResponsable) {
                            ps.setInt(1, o.getId());
                            ps.setInt(2, idMecanico);
                            ps.setBoolean(3, false);
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Nombres de los mecánicos asignados, el responsable primero. */
    public List<String> mecanicosDe(int idOrden) throws SQLException {
        String sql = "SELECT m.nombres + N' ' + m.apellidos AS nombre, om.esResponsable "
                + "FROM OrdenMecanicos om JOIN Mecanicos m ON m.idMecanico = om.idMecanico "
                + "WHERE om.idOrden = ? ORDER BY om.esResponsable DESC, m.apellidos";
        List<String> nombres = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nombres.add(rs.getString("nombre") + (rs.getBoolean("esResponsable") ? " (responsable)" : ""));
                }
            }
        }
        return nombres;
    }

    public SaldoOrden saldo(int idOrden) throws SQLException {
        String sql = "SELECT subtotal, descuento, impuesto, total, pagado, saldo FROM vw_SaldoOrdenes WHERE idOrden = ?";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new SaldoOrden(rs.getBigDecimal("subtotal"), rs.getBigDecimal("descuento"),
                        rs.getBigDecimal("impuesto"), rs.getBigDecimal("total"),
                        rs.getBigDecimal("pagado"), rs.getBigDecimal("saldo"));
            }
        }
    }

    /** Cambia el estado. Devuelve false si otro usuario modificó la orden antes (rowversion). */
    public boolean cambiarEstado(int idOrden, String estado, byte[] version) throws SQLException {
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(
                "UPDATE OrdenesTrabajo SET estado = ? WHERE idOrden = ? AND version = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idOrden);
            ps.setBytes(3, version);
            return ps.executeUpdate() > 0;
        }
    }

    /** Finaliza la orden con sp_FinalizarOrden (exige control de calidad aprobado y saldo en cero). */
    public void finalizar(int idOrden, int idUsuario) throws SQLException {
        try (Connection con = Conexion.abrir(); CallableStatement cs = con.prepareCall("{call sp_FinalizarOrden(?, ?)}")) {
            cs.setInt(1, idOrden);
            cs.setInt(2, idUsuario);
            cs.execute();
        }
    }
}
