package dao.ordenes;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dao.Conexion;
import model.ordenes.Pago;

public class PagoDAO {

    public List<Pago> listar(int idOrden) throws SQLException {
        String sql = "SELECT p.idPago, p.codigo, p.fecha, p.monto, p.metodo, p.referencia, u.usuario "
                + "FROM Pagos p JOIN Usuarios u ON u.idUsuario = p.idUsuario "
                + "WHERE p.idOrden = ? ORDER BY p.fecha, p.idPago";
        List<Pago> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Pago(rs.getInt("idPago"), rs.getString("codigo"),
                            rs.getTimestamp("fecha").toLocalDateTime(), rs.getBigDecimal("monto"),
                            rs.getString("metodo"), rs.getString("referencia"), rs.getString("usuario")));
                }
            }
        }
        return lista;
    }

    /** Registra el pago con sp_RegistrarPago, que rechaza montos mayores al saldo pendiente. */
    public void registrar(int idOrden, BigDecimal monto, String metodo, String referencia, int idUsuario)
            throws SQLException {
        try (Connection con = Conexion.abrir();
             CallableStatement cs = con.prepareCall("{call sp_RegistrarPago(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, idOrden);
            cs.setBigDecimal(2, monto);
            cs.setString(3, metodo);
            cs.setString(4, referencia == null || referencia.isBlank() ? null : referencia.trim());
            cs.setInt(5, idUsuario);
            cs.execute();
        }
    }
}
