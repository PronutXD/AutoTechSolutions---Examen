package controller;

import dao.ErroresSQL;
import dao.MecanicoDAO;
import dao.RepuestoDAO;
import dao.ServicioDAO;
import dao.ordenes.ControlCalidadDAO;
import dao.ordenes.DetalleOrdenDAO;
import dao.ordenes.OrdenDAO;
import dao.ordenes.PagoDAO;
import model.Mecanico;
import model.Repuesto;
import model.Servicio;
import model.Sesion;
import model.ordenes.ControlCalidad;
import model.ordenes.LineaOrden;
import model.ordenes.Orden;
import model.ordenes.Pago;
import model.ordenes.SaldoOrden;
import views.componentes.Formato;
import views.componentes.Mensajes;
import views.formularios.ordenes.DlgControlCalidad;
import views.formularios.ordenes.DlgLineaOrden;
import views.formularios.ordenes.DlgOrden;
import views.formularios.ordenes.DlgPago;
import java.awt.Frame;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Controla la ventana de detalle de una orden: cambia su estado, agrega servicios y
 * repuestos, registra pagos y controles de calidad. Después de cada cambio vuelve a
 * leer todo de la base, para que lo que se ve sea siempre lo que está guardado.
 */
public class DetalleOrdenController {

    private final Frame ventana;
    private final int idOrden;
    private final DlgOrden vista;

    private final OrdenDAO ordenDAO = new OrdenDAO();
    private final DetalleOrdenDAO detalleDAO = new DetalleOrdenDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final ControlCalidadDAO controlDAO = new ControlCalidadDAO();
    private final MecanicoDAO mecanicoDAO = new MecanicoDAO();
    private final ServicioDAO servicioDAO = new ServicioDAO();
    private final RepuestoDAO repuestoDAO = new RepuestoDAO();

    private Orden orden;
    private SaldoOrden saldo;

    public DetalleOrdenController(Frame ventana, int idOrden) {
        this.ventana = ventana;
        this.idOrden = idOrden;
        this.vista = new DlgOrden(ventana);
    }

    /** Muestra el detalle y no regresa hasta que se cierra la ventana. */
    public void abrir() {
        if (!refrescar()) {
            return;
        }
        vista.getBtnCambiarEstado().addActionListener(e -> cambiarEstado());
        vista.getBtnFinalizar().addActionListener(e -> finalizar());
        vista.getBtnAgregarServicio().addActionListener(e -> agregarServicio());
        vista.getBtnAgregarRepuesto().addActionListener(e -> agregarRepuesto());
        vista.getBtnRegistrarPago().addActionListener(e -> registrarPago());
        vista.getBtnRegistrarControl().addActionListener(e -> registrarControl());
        vista.setVisible(true);
    }

    // ================================================================= carga

    /** Vuelve a leer la orden y todas sus pestañas. Devuelve false si no se pudo. */
    private boolean refrescar() {
        try {
            orden = ordenDAO.buscarPorId(idOrden).orElse(null);
            if (orden == null) {
                Mensajes.error(ventana, "La orden ya no existe.");
                return false;
            }
            saldo = ordenDAO.saldo(idOrden);
            vista.setCabecera(orden.getCodigo(), orden.getEstado(), orden.getCliente() + " · " + orden.getVehiculo());
            vista.mostrarDatos(orden, ordenDAO.mecanicosDe(idOrden));
            vista.mostrarSaldo(saldo);

            llenarLineas(detalleDAO.listar(idOrden));
            llenarPagos(pagoDAO.listar(idOrden));
            llenarCalidad(controlDAO.listar(idOrden));
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar la orden", "dato"));
            return false;
        }
        String estado = orden.getEstado();
        vista.setEstadosSiguientes(Orden.siguientesEstados(estado));
        vista.setPuedeFinalizar(Orden.EN_CONTROL_CALIDAD.equals(estado));
        vista.setPuedeAgregarLineas(!orden.isCerrada());
        vista.setPuedePagar(!Orden.RECHAZADA.equals(estado) && !Orden.CANCELADA.equals(estado)
                && saldo.saldo().signum() > 0);
        vista.setPuedeRegistrarControl(Orden.EN_REPARACION.equals(estado) || Orden.EN_CONTROL_CALIDAD.equals(estado));
        return true;
    }

    private void llenarLineas(List<LineaOrden> lineas) {
        vista.getTblLineas().getModelo().setRowCount(0);
        for (LineaOrden l : lineas) {
            vista.getTblLineas().getModelo().addRow(new Object[]{
                l.tipo(), l.descripcion(), l.cantidad(), Formato.dinero(l.precioUnitario()),
                Formato.dinero(l.subtotal())});
        }
    }

    private void llenarPagos(List<Pago> pagos) {
        vista.getTblPagos().getModelo().setRowCount(0);
        for (Pago p : pagos) {
            vista.getTblPagos().getModelo().addRow(new Object[]{
                p.codigo(), Formato.fechaHora(p.fecha()), Formato.dinero(p.monto()), p.metodo(),
                p.referencia() == null ? "" : p.referencia(), p.registradoPor()});
        }
    }

    private void llenarCalidad(List<ControlCalidad> controles) {
        vista.getTblCalidad().getModelo().setRowCount(0);
        for (ControlCalidad c : controles) {
            vista.getTblCalidad().getModelo().addRow(new Object[]{
                c.codigo(), Formato.fechaHora(c.fecha()), c.inspector(), c.resultado(),
                c.pruebaRuta() ? "Sí" : "No", c.observaciones() == null ? "" : c.observaciones()});
        }
    }

    // ================================================================= acciones

    private void cambiarEstado() {
        String nuevo = vista.getNuevoEstado();
        if (nuevo == null) {
            return;
        }
        if ((Orden.RECHAZADA.equals(nuevo) || Orden.CANCELADA.equals(nuevo))
                && !Mensajes.confirmar(vista, "Cambiar estado",
                        "¿Pasar la orden a «" + nuevo + "»? Ya no se podrá modificar.")) {
            return;
        }
        try {
            if (!ordenDAO.cambiarEstado(idOrden, nuevo, orden.getVersion())) {
                Mensajes.advertencia(vista, "Otro usuario modificó esta orden mientras la veías. "
                        + "Se mostrarán los datos actuales.");
            }
        } catch (SQLException ex) {
            Mensajes.error(vista, ErroresSQL.mensaje(ex, "cambiar el estado", "dato"));
        }
        refrescar();
    }

    private void finalizar() {
        if (!haySesion()
                || !Mensajes.confirmar(vista, "Finalizar orden",
                        "¿Finalizar y entregar la orden " + orden.getCodigo() + "?")) {
            return;
        }
        try {
            ordenDAO.finalizar(idOrden, Sesion.actual().id());
            Mensajes.info(vista, "Orden finalizada y entregada.");
        } catch (SQLException ex) {
            Mensajes.error(vista, ErroresSQL.mensaje(ex, "finalizar la orden", "dato"));
        }
        refrescar();
    }

    private void agregarServicio() {
        List<Servicio> servicios;
        try {
            servicios = servicioDAO.listar(true);
        } catch (SQLException ex) {
            Mensajes.error(vista, ErroresSQL.mensaje(ex, "cargar los servicios", "dato"));
            return;
        }
        DlgLineaOrden dlg = new DlgLineaOrden(ventana, false, servicios);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        Servicio s = (Servicio) dlg.getSeleccion();
        try {
            if (!detalleDAO.agregarServicio(idOrden, s.id(), dlg.getCantidad())) {
                Mensajes.advertencia(vista, "La orden ya está cerrada; no se puede agregar el servicio.");
            }
        } catch (SQLException ex) {
            Mensajes.error(vista, ErroresSQL.mensaje(ex, "agregar el servicio", "dato"));
        }
        refrescar();
    }

    private void agregarRepuesto() {
        if (!haySesion()) {
            return;
        }
        List<Repuesto> repuestos;
        try {
            repuestos = repuestoDAO.listar(true).stream().filter(r -> r.stock() > 0).toList();
        } catch (SQLException ex) {
            Mensajes.error(vista, ErroresSQL.mensaje(ex, "cargar los repuestos", "dato"));
            return;
        }
        if (repuestos.isEmpty()) {
            Mensajes.advertencia(vista, "No hay repuestos con existencia en el inventario.");
            return;
        }
        DlgLineaOrden dlg = new DlgLineaOrden(ventana, true, repuestos);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        Repuesto r = (Repuesto) dlg.getSeleccion();
        try {
            detalleDAO.agregarRepuesto(idOrden, r.id(), dlg.getCantidad(), Sesion.actual().id());
        } catch (SQLException ex) {
            Mensajes.error(vista, ErroresSQL.mensaje(ex, "agregar el repuesto", "dato"));
        }
        refrescar();
    }

    private void registrarPago() {
        if (!haySesion()) {
            return;
        }
        BigDecimal pendiente = saldo.saldo();
        DlgPago dlg = new DlgPago(ventana, pendiente);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            pagoDAO.registrar(idOrden, dlg.getMonto(), dlg.getMetodo(), dlg.getReferencia(), Sesion.actual().id());
        } catch (SQLException ex) {
            Mensajes.error(vista, ErroresSQL.mensaje(ex, "registrar el pago", "dato"));
        }
        refrescar();
    }

    private void registrarControl() {
        List<Mecanico> mecanicos;
        try {
            mecanicos = mecanicoDAO.listar(true);
        } catch (SQLException ex) {
            Mensajes.error(vista, ErroresSQL.mensaje(ex, "cargar los mecánicos", "dato"));
            return;
        }
        if (mecanicos.isEmpty()) {
            Mensajes.advertencia(vista, "No hay mecánicos activos que puedan hacer la revisión.");
            return;
        }
        DlgControlCalidad dlg = new DlgControlCalidad(ventana, mecanicos);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            controlDAO.registrar(idOrden, dlg.getIdInspector(), dlg.getResultado(),
                    dlg.isPruebaRuta(), dlg.getObservaciones());
        } catch (SQLException ex) {
            Mensajes.error(vista, ErroresSQL.mensaje(ex, "registrar el control de calidad", "dato"));
        }
        refrescar();
    }

    private boolean haySesion() {
        if (Sesion.actual() == null) {
            Mensajes.advertencia(vista, "Inicia sesión para hacer esta operación.");
            return false;
        }
        return true;
    }
}
