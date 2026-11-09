package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.HistorialVehicular;
import model.RegistroAuditoria;
import model.RepuestoBajoMinimo;
import model.SaldoPorOrden;

/** Solo lectura: las cuatro consultas de la pantalla Consultas (vistas SQL y Auditoria). */
public class ConsultaDAO {

    /** Cuántos registros recientes de auditoría se muestran como máximo. */
    private static final int LIMITE_AUDITORIA = 500;

    /** Historial de todos los vehículos, lo más reciente primero (vw_HistorialVehiculo). */
    public List<HistorialVehicular> historial() throws SQLException {
        String sql = "SELECT fechaIngreso, codigoOrden, marca, modelo, placa, servicios, mecanicos, repuestos, costo "
                + "FROM vw_HistorialVehiculo ORDER BY fechaIngreso DESC, idOrden DESC";
        List<HistorialVehicular> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new HistorialVehicular(fecha(rs, "fechaIngreso"), rs.getString("codigoOrden"),
                        rs.getString("marca") + " " + rs.getString("modelo") + " · " + rs.getString("placa"),
                        rs.getString("servicios"), rs.getString("mecanicos"), rs.getString("repuestos"),
                        rs.getBigDecimal("costo")));
            }
        }
        return lista;
    }

    /** @param categoria un valor de Repuesto.CATEGORIAS, o null para todas */
    public List<RepuestoBajoMinimo> bajoMinimo(String categoria) throws SQLException {
        String sql = "SELECT codigo, nombre, categoria, stock, stockMinimo, faltante, proveedor "
                + "FROM vw_RepuestosBajoMinimo " + (categoria == null ? "" : "WHERE categoria = ? ")
                + "ORDER BY faltante DESC, nombre";
        List<RepuestoBajoMinimo> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (categoria != null) {
                ps.setString(1, categoria);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new RepuestoBajoMinimo(rs.getString("codigo"), rs.getString("nombre"),
                            rs.getString("categoria"), rs.getInt("stock"), rs.getInt("stockMinimo"),
                            rs.getInt("faltante"), rs.getString("proveedor")));
                }
            }
        }
        return lista;
    }

    /** @param soloPendientes true = solo las órdenes con saldo mayor que cero */
    public List<SaldoPorOrden> saldos(boolean soloPendientes) throws SQLException {
        String sql = "SELECT s.codigo, l.cliente, l.vehiculo, s.total, s.pagado, s.saldo, l.estado "
                + "FROM vw_SaldoOrdenes s LEFT JOIN vw_ListadoOrdenes l ON l.idOrden = s.idOrden "
                + (soloPendientes ? "WHERE s.saldo > 0 " : "")
                + "ORDER BY l.fechaIngreso DESC, s.idOrden DESC";
        List<SaldoPorOrden> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new SaldoPorOrden(rs.getString("codigo"), rs.getString("cliente"),
                        rs.getString("vehiculo"), rs.getBigDecimal("total"), rs.getBigDecimal("pagado"),
                        rs.getBigDecimal("saldo"), rs.getString("estado")));
            }
        }
        return lista;
    }

    /**
     * Últimos registros de auditoría.
     * @param tabla  nombre real de la tabla (por ejemplo OrdenesTrabajo), o null para todas
     * @param accion Alta, Modificación o Eliminación, o null para todas
     */
    public List<RegistroAuditoria> auditoria(String tabla, String accion) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT TOP (" + LIMITE_AUDITORIA + ") a.fecha, u.usuario, a.tabla, "
                + "a.idRegistro, a.accion, a.valorAnterior, a.valorNuevo FROM Auditoria a "
                + "LEFT JOIN Usuarios u ON u.idUsuario = a.idUsuario WHERE 1 = 1 ");
        if (tabla != null) {
            sql.append("AND a.tabla = ? ");
        }
        if (accion != null) {
            sql.append("AND a.accion = ? ");
        }
        sql.append("ORDER BY a.fecha DESC, a.idAuditoria DESC");

        List<RegistroAuditoria> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (tabla != null) {
                ps.setString(i++, tabla);
            }
            if (accion != null) {
                ps.setString(i, accion);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new RegistroAuditoria(fecha(rs, "fecha"), rs.getString("usuario"),
                            rs.getString("tabla"), rs.getInt("idRegistro"), rs.getString("accion"),
                            rs.getString("valorAnterior"), rs.getString("valorNuevo")));
                }
            }
        }
        return lista;
    }

    private static java.time.LocalDateTime fecha(ResultSet rs, String columna) throws SQLException {
        Timestamp t = rs.getTimestamp(columna);
        return t == null ? null : t.toLocalDateTime();
    }
}
