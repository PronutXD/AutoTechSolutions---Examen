package controller;

import dao.ConsultaDAO;
import dao.ErroresSQL;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import model.HistorialVehicular;
import model.RegistroAuditoria;
import model.Repuesto;
import model.RepuestoBajoMinimo;
import model.SaldoPorOrden;
import views.componentes.Formato;
import views.componentes.Mensajes;
import views.componentes.PnlTabla;
import views.paneles.PnlConsultas;

/**
 * Controlador de la pantalla "Consultas" (sus cuatro pestañas, todas de solo lectura):
 * llena las tablas desde las vistas SQL y aplica los filtros. La vista no sabe nada de esto.
 */
public class ConsultasController {

    /** Tablas del filtro de Auditoría, en el mismo orden que sus opciones (la 0 es "Todas"). */
    private static final String[] TABLAS_AUDITORIA = {"OrdenesTrabajo", "Repuestos", "Pagos", "Usuarios"};
    /** Acciones del filtro de Auditoría, en el mismo orden que sus opciones (la 0 es "Todas"). */
    private static final String[] ACCIONES_AUDITORIA = {"Alta", "Modificación", "Eliminación"};

    private final PnlConsultas vista;
    private final Frame ventana;
    private final ConsultaDAO dao = new ConsultaDAO();

    public ConsultasController(PnlConsultas vista, Frame ventana) {
        this.vista = vista;
        this.ventana = ventana;
    }

    public void iniciar() {
        vista.getCmbCategoria().addActionListener(e -> recargarBajoMinimo());
        vista.getChkSoloPendientes().addActionListener(e -> recargarSaldos());
        vista.getCmbTablaAuditoria().addActionListener(e -> recargarAuditoria());
        vista.getCmbAccionAuditoria().addActionListener(e -> recargarAuditoria());
        recargarTodo();

        // Los datos cambian en otras pantallas: al volver aquí o a una pestaña se vuelven a leer.
        vista.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                recargarTodo();
            }
        });
        vista.getTabs().addChangeListener(e -> recargarPestanaActual());
    }

    private void recargarTodo() {
        recargarHistorial();
        recargarBajoMinimo();
        recargarSaldos();
        recargarAuditoria();
    }

    private void recargarPestanaActual() {
        switch (vista.getTabs().getSelectedIndex()) {
            case 0 -> recargarHistorial();
            case 1 -> recargarBajoMinimo();
            case 2 -> recargarSaldos();
            case 3 -> recargarAuditoria();
            default -> { }
        }
    }

    private void recargarHistorial() {
        PnlTabla t = vista.getTblHistorial();
        t.getModelo().setRowCount(0);
        try {
            for (HistorialVehicular h : dao.historial()) {
                t.getModelo().addRow(new Object[]{
                    h.fecha() == null ? "—" : Formato.fechaHora(h.fecha()), h.orden(), h.vehiculo(),
                    texto(h.servicios()), texto(h.mecanicos()), texto(h.repuestos()),
                    Formato.dinero(h.costo())});
            }
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar el historial vehicular", "dato"));
        }
    }

    private void recargarBajoMinimo() {
        int combo = vista.getCmbCategoria().getSelectedIndex();
        PnlTabla t = vista.getTblBajoMinimo();
        t.getModelo().setRowCount(0);
        try {
            for (RepuestoBajoMinimo r : dao.bajoMinimo(combo <= 0 ? null : Repuesto.CATEGORIAS[combo - 1])) {
                t.getModelo().addRow(new Object[]{
                    r.codigo(), r.nombre(), r.categoria(), r.stock(), r.stockMinimo(), r.faltante(),
                    texto(r.proveedor())});
            }
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los repuestos bajo mínimo", "dato"));
        }
    }

    private void recargarSaldos() {
        PnlTabla t = vista.getTblSaldos();
        t.getModelo().setRowCount(0);
        try {
            for (SaldoPorOrden s : dao.saldos(vista.getChkSoloPendientes().isSelected())) {
                t.getModelo().addRow(new Object[]{
                    s.orden(), texto(s.cliente()), texto(s.vehiculo()), Formato.dinero(s.total()),
                    Formato.dinero(s.pagado()), Formato.dinero(s.saldo()), texto(s.estado())});
            }
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los saldos por orden", "dato"));
        }
    }

    private void recargarAuditoria() {
        int tabla = vista.getCmbTablaAuditoria().getSelectedIndex();
        int accion = vista.getCmbAccionAuditoria().getSelectedIndex();
        PnlTabla t = vista.getTblAuditoria();
        t.getModelo().setRowCount(0);
        try {
            for (RegistroAuditoria a : dao.auditoria(
                    tabla <= 0 ? null : TABLAS_AUDITORIA[tabla - 1],
                    accion <= 0 ? null : ACCIONES_AUDITORIA[accion - 1])) {
                t.getModelo().addRow(new Object[]{
                    a.fecha() == null ? "—" : Formato.fechaHora(a.fecha()), texto(a.usuario()),
                    nombreTabla(a.tabla()), a.idRegistro(), a.accion(),
                    texto(a.valorAnterior()), texto(a.valorNuevo())});
            }
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar la auditoría", "dato"));
        }
    }

    /** Texto vacío o nulo (por ejemplo una orden sin repuestos) se muestra como raya. */
    private static String texto(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    /** OrdenesTrabajo se muestra como "Órdenes", igual que en el filtro. */
    private static String nombreTabla(String tabla) {
        return "OrdenesTrabajo".equals(tabla) ? "Órdenes" : tabla;
    }
}
