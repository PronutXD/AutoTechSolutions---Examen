package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.LineaOrden;

/** Servicios y repuestos cobrados dentro de una orden (tabla DetalleOrden). */
public class DetalleOrdenDAO {

    public List<LineaOrden> listar(int idOrden) throws SQLException {
        String sql = "SELECT d.idDetalle, d.tipo, COALESCE(s.nombre, r.nombre) AS descripcion, "
                + "d.cantidad, d.precioUnitario FROM DetalleOrden d "
                + "LEFT JOIN Servicios s ON s.idServicio = d.idServicio "
                + "LEFT JOIN Repuestos r ON r.idRepuesto = d.idRepuesto "
                + "WHERE d.idOrden = ? ORDER BY d.idDetalle";
        List<LineaOrden> lista = new ArrayList<>();
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new LineaOrden(rs.getInt("idDetalle"), rs.getString("tipo"),
                            rs.getString("descripcion"), rs.getInt("cantidad"), rs.getBigDecimal("precioUnitario")));
                }
            }
        }
        return lista;
    }

    /**
     * Agrega un servicio a la orden con el precio de mano de obra de este momento (snapshot).
     * Devuelve false si la orden ya está cerrada y no se agregó nada.
     */
    public boolean agregarServicio(int idOrden, int idServicio, int cantidad) throws SQLException {
        String sql = "INSERT INTO DetalleOrden (idOrden, tipo, idServicio, cantidad, precioUnitario) "
                + "SELECT ?, N'Servicio', s.idServicio, ?, s.manoObra FROM Servicios s "
                + "WHERE s.idServicio = ? AND EXISTS (SELECT 1 FROM OrdenesTrabajo o WHERE o.idOrden = ? "
                + "AND o.estado NOT IN (N'Finalizada y entregada', N'Rechazada', N'Cancelada'))";
        try (Connection con = Conexion.abrir(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ps.setInt(2, cantidad);
            ps.setInt(3, idServicio);
            ps.setInt(4, idOrden);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Agrega un repuesto con sp_RegistrarConsumo: descuenta el stock, deja el movimiento de
     * inventario y agrega la línea con el precio de venta de este momento, todo en una transacción.
     */
    public void agregarRepuesto(int idOrden, int idRepuesto, int cantidad, int idUsuario) throws SQLException {
        try (Connection con = Conexion.abrir();
             CallableStatement cs = con.prepareCall("{call sp_RegistrarConsumo(?, ?, ?, ?)}")) {
            cs.setInt(1, idOrden);
            cs.setInt(2, idRepuesto);
            cs.setInt(3, cantidad);
            cs.setInt(4, idUsuario);
            cs.execute();
        }
    }
}
