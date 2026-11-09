package controller;

import dao.ClienteDAO;
import dao.ErroresSQL;
import dao.MecanicoDAO;
import dao.OrdenDAO;
import dao.VehiculoDAO;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import model.Cliente;
import model.Mecanico;
import model.Orden;
import model.Sesion;
import model.Vehiculo;
import views.componentes.Formato;
import views.componentes.Mensajes;
import views.componentes.PnlTabla;
import views.formularios.DlgNuevaOrden;
import views.paneles.PnlOrdenes;

/**
 * Conecta la pantalla "Órdenes de trabajo" con la base de datos: llena el listado
 * (con el filtro de estado), abre el formulario de nueva orden y el detalle de cada una.
 * El detalle lo maneja DetalleOrdenController.
 */
public class OrdenController {

    private final PnlOrdenes vista;
    private final Frame ventana;
    private final OrdenDAO ordenDAO = new OrdenDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private final MecanicoDAO mecanicoDAO = new MecanicoDAO();

    private List<Orden> ordenes = new ArrayList<>();

    public OrdenController(PnlOrdenes vista, Frame ventana) {
        this.vista = vista;
        this.ventana = ventana;
    }

    public void iniciar() {
        vista.getCmbEstado().addActionListener(e -> recargar());
        vista.getBtnNuevaOrden().addActionListener(e -> nuevaOrden());
        vista.getBtnAbrirOrden().addActionListener(e -> abrirOrden());
        vista.getTblOrdenes().getTabla().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && ordenSeleccionada() != null) {
                    abrirOrden();
                }
            }
        });
        recargar();
    }

    private void recargar() {
        int indice = vista.getCmbEstado().getSelectedIndex();
        String estado = indice <= 0 ? null : Orden.ESTADOS[indice - 1];
        try {
            ordenes = ordenDAO.listar(estado);
        } catch (SQLException ex) {
            ordenes = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar las órdenes", "dato"));
        }
        PnlTabla t = vista.getTblOrdenes();
        t.getModelo().setRowCount(0);
        for (Orden o : ordenes) {
            t.getModelo().addRow(new Object[]{
                o.getCodigo(), Formato.fechaHora(o.getFechaIngreso()), o.getCliente(), o.getVehiculo(),
                o.getResponsable() == null ? "—" : o.getResponsable(), o.getEstado(),
                Formato.dinero(o.getTotal())});
        }
    }

    private void nuevaOrden() {
        if (Sesion.actual() == null) {
            Mensajes.advertencia(ventana, "Inicia sesión para crear órdenes.");
            return;
        }
        List<Cliente> clientes;
        List<Vehiculo> vehiculos;
        List<Mecanico> mecanicos;
        try {
            clientes = clienteDAO.listar(true);
            vehiculos = vehiculoDAO.listar(true, null);
            mecanicos = mecanicoDAO.listar(true);
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los datos del formulario", "dato"));
            return;
        }
        if (vehiculos.isEmpty()) {
            Mensajes.advertencia(ventana, "No hay vehículos activos. Registra uno en Clientes y vehículos.");
            return;
        }
        if (mecanicos.isEmpty()) {
            Mensajes.advertencia(ventana, "No hay mecánicos activos para asignar a la orden.");
            return;
        }

        DlgNuevaOrden dlg = new DlgNuevaOrden(ventana, clientes, vehiculos, mecanicos);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        Orden nueva = dlg.getOrden();
        nueva.setIdUsuario(Sesion.actual().id());
        try {
            ordenDAO.insertar(nueva, dlg.getIdResponsable(), dlg.getIdsAdicionales());
            String codigo = ordenDAO.buscarPorId(nueva.getId()).map(Orden::getCodigo).orElse("");
            recargar();
            Mensajes.info(ventana, "Orden " + codigo + " creada.");
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "crear la orden", "dato"));
        }
    }

    private void abrirOrden() {
        Orden o = ordenSeleccionada();
        if (o == null) {
            return;
        }
        new DetalleOrdenController(ventana, o.getId()).abrir();
        recargar(); // el detalle pudo cambiar el estado o el total
    }

    private Orden ordenSeleccionada() {
        JTable t = vista.getTblOrdenes().getTabla();
        int fila = t.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        int modelo = t.convertRowIndexToModel(fila);
        return modelo < ordenes.size() ? ordenes.get(modelo) : null;
    }
}
