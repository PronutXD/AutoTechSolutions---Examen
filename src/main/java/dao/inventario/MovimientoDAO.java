package dao.inventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dao.Conexion;
import model.inventario.MovimientoInventario;

/** Solo lectura: los movimientos los generan las entradas y los consumos de las órdenes. */
public class MovimientoDAO {

    /** Cuántos movimientos recientes se muestran como máximo. */
    private static final int LIMITE = 500;

    /** @param tipo "Entrada", "Salida" o null para todos */
    public List<MovimientoInventario> listar(String tipo) throws SQLException {
        String sql = "SELECT TOP (" + LIMITE + ") mi.idMovimiento, mi.codigo, mi.fecha, r.nombre AS repuesto, "
                + "mi.tipo, mi.cantidad, mi.costoUnitario, COALESCE(p.nombre, o.codigo) AS origen, u.usuario "
                + "FROM MovimientosInventario mi JOIN Repuestos r ON r.idRepuesto = mi.idRepuesto "
                + "LEFT JOIN Proveedores p ON p.idProveedor = mi.idProveedor "
                + "LEFT JOIN OrdenesTrabajo o ON o.idOrden = mi.idOrden "
                + "LEFT JOIN Usuarios u ON u.idUsuario = mi.idUsuario "
                + (tipo == null ? "" : "WHERE mi.tipo = ? ")
                + "ORDER BY mi.fecha DESC, mi.idMovimiento DESC";
        List<MovimientoInventario> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (tipo != null) {
                ps.setString(1, tipo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new MovimientoInventario(rs.getInt("idMovimiento"), rs.getString("codigo"),
                            rs.getTimestamp("fecha").toLocalDateTime(), rs.getString("repuesto"),
                            rs.getString("tipo"), rs.getInt("cantidad"), rs.getBigDecimal("costoUnitario"),
                            rs.getString("origen"), rs.getString("usuario")));
                }
            }
        }
        return lista;
    }
}
