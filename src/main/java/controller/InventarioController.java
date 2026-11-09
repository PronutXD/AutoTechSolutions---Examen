package controller;

import dao.ErroresSQL;
import dao.RepuestoDAO;
import dao.inventario.MotorDAO;
import dao.inventario.MovimientoDAO;
import dao.inventario.ProveedorDAO;
import model.Repuesto;
import model.Sesion;
import model.Vehiculo;
import model.inventario.Motor;
import model.inventario.MovimientoInventario;
import model.inventario.Proveedor;
import views.componentes.Formato;
import views.componentes.Mensajes;
import views.componentes.PnlTabla;
import views.formularios.inventario.DlgEntrada;
import views.formularios.inventario.DlgMotor;
import views.formularios.inventario.DlgMotoresCompatibles;
import views.formularios.inventario.DlgProveedor;
import views.formularios.inventario.DlgRepuesto;
import views.paneles.PnlInventario;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;

/**
 * Controlador de la pantalla "Inventario" (sus cuatro pestañas): llena las tablas,
 * aplica los filtros y abre los formularios. La vista no sabe nada de esto.
 *
 * Igual que ClientesController, conserva la lista de objetos que mostró (en el mismo
 * orden que las filas) para saber qué registro está seleccionado.
 */
public class InventarioController {

    private static final String CAMBIO_AJENO =
            "Otro usuario modificó este registro mientras lo editabas. "
            + "Se recargó la lista para que veas los datos actuales y vuelvas a intentar.";

    private final PnlInventario vista;
    private final Frame ventana;
    private final RepuestoDAO repuestoDAO = new RepuestoDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final MotorDAO motorDAO = new MotorDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    private List<Repuesto> repuestos = new ArrayList<>();
    private List<Proveedor> proveedores = new ArrayList<>();
    private List<MovimientoInventario> movimientos = new ArrayList<>();
    private List<Motor> motores = new ArrayList<>();

    public InventarioController(PnlInventario vista, Frame ventana) {
        this.vista = vista;
        this.ventana = ventana;
    }

    public void iniciar() {
        conectarRepuestos();
        conectarProveedores();
        conectarMovimientos();
        conectarMotores();
        recargarTodo();

        // Las órdenes también mueven el stock: al volver a esta pantalla o a una pestaña se relee.
        vista.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                recargarTodo();
            }
        });
        vista.getTabs().addChangeListener(e -> recargarPestanaActual());
    }

    private void recargarTodo() {
        recargarRepuestos();
        recargarProveedores();
        recargarMovimientos();
        recargarMotores();
    }

    private void recargarPestanaActual() {
        switch (vista.getTabs().getSelectedIndex()) {
            case 0 -> recargarRepuestos();
            case 1 -> recargarProveedores();
            case 2 -> recargarMovimientos();
            case 3 -> recargarMotores();
            default -> { }
        }
    }

    // ================================================================= repuestos

    private void conectarRepuestos() {
        vista.getCmbCategoria().addActionListener(e -> recargarRepuestos());
        vista.getCmbEstadoRepuestos().addActionListener(e -> recargarRepuestos());
        vista.getChkStockBajo().addActionListener(e -> recargarRepuestos());
        vista.getBtnNuevoRepuesto().addActionListener(e -> nuevoRepuesto());
        vista.getBtnEntrada().addActionListener(e -> registrarEntrada());
        vista.getBtnEditarRepuesto().addActionListener(e -> editarRepuesto());
        vista.getBtnEstadoRepuesto().addActionListener(e -> cambiarEstadoRepuesto());
        vista.getTblRepuestos().getTabla().getSelectionModel().addListSelectionListener(e -> {
            Repuesto r = repuestoSeleccionado();
            if (r != null) {
                vista.getBtnEstadoRepuesto().setText(r.activo() ? "Desactivar" : "Reactivar");
                vista.getBtnEntrada().setEnabled(r.activo());
            }
        });
    }

    private void recargarRepuestos() {
        int combo = vista.getCmbCategoria().getSelectedIndex();
        String categoria = combo <= 0 ? null : Repuesto.CATEGORIAS[combo - 1];
        try {
            repuestos = repuestoDAO.listar(filtroEstado(vista.getCmbEstadoRepuestos().getSelectedIndex()),
                    categoria, vista.getChkStockBajo().isSelected());
        } catch (SQLException ex) {
            repuestos = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los repuestos", "dato"));
        }
        PnlTabla t = vista.getTblRepuestos();
        t.getModelo().setRowCount(0);
        for (Repuesto r : repuestos) {
            t.getModelo().addRow(new Object[]{
                r.codigo(), r.nombre(), r.categoria(), r.proveedor() == null ? "—" : r.proveedor(),
                r.stock(), r.stockMinimo(), Formato.dinero(r.costo()), Formato.dinero(r.precioVenta()),
                r.estadoTexto()});
        }
    }

    private void nuevoRepuesto() {
        List<Proveedor> disponibles = proveedoresParaElegir(null);
        if (disponibles == null) {
            return;
        }
        DlgRepuesto dlg = new DlgRepuesto(ventana, null, disponibles);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            repuestoDAO.insertar(dlg.getRepuesto());
            Mensajes.info(ventana, "Repuesto registrado con stock 0. Usa \"Registrar entrada\" para cargar el stock.");
            recargarRepuestos();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "guardar el repuesto", "nombre"));
        }
    }

    private void editarRepuesto() {
        Repuesto seleccionado = repuestoSeleccionado();
        if (seleccionado == null) {
            return;
        }
        List<Proveedor> disponibles = proveedoresParaElegir(seleccionado.idProveedor());
        if (disponibles == null) {
            return;
        }
        // El record es inmutable: el formulario arma uno nuevo, así que si se cancela la lista no cambia.
        DlgRepuesto dlg = new DlgRepuesto(ventana, seleccionado, disponibles);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            if (repuestoDAO.actualizar(dlg.getRepuesto())) {
                Mensajes.info(ventana, "Repuesto actualizado.");
            } else {
                Mensajes.advertencia(ventana, CAMBIO_AJENO);
            }
            recargarRepuestos();
            recargarMotores(); // el nombre del repuesto aparece en la tabla de motores
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "actualizar el repuesto", "nombre"));
        }
    }

    private void cambiarEstadoRepuesto() {
        Repuesto r = repuestoSeleccionado();
        if (r == null) {
            return;
        }
        boolean activar = !r.activo();
        if (!activar && !Mensajes.confirmar(ventana, "Desactivar repuesto",
                "¿Desactivar \"" + r.nombre() + "\"? Ya no se podrá agregar a órdenes ni recibir entradas.")) {
            return;
        }
        try {
            repuestoDAO.cambiarEstado(r.id(), activar);
            recargarRepuestos();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cambiar el estado del repuesto", "dato"));
        }
    }

    private void registrarEntrada() {
        Repuesto r = repuestoSeleccionado();
        if (r == null) {
            return;
        }
        if (Sesion.actual() == null) {
            Mensajes.advertencia(ventana, "Inicia sesión para registrar entradas.");
            return;
        }
        List<Proveedor> disponibles = proveedoresParaElegir(r.idProveedor());
        if (disponibles == null) {
            return;
        }
        DlgEntrada dlg = new DlgEntrada(ventana, r, disponibles);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            repuestoDAO.registrarEntrada(r.id(), dlg.getCantidad(), dlg.getCostoUnitario(),
                    dlg.getIdProveedor(), Sesion.actual().id());
            Mensajes.info(ventana, "Entrada registrada: " + dlg.getCantidad() + " × " + r.nombre() + ".");
            recargarRepuestos();
            recargarMovimientos();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "registrar la entrada", "dato"));
        }
    }

    private Repuesto repuestoSeleccionado() {
        return seleccionado(vista.getTblRepuestos(), repuestos);
    }

    // ================================================================= proveedores

    private void conectarProveedores() {
        vista.getCmbEstadoProveedores().addActionListener(e -> recargarProveedores());
        vista.getBtnNuevoProveedor().addActionListener(e -> nuevoProveedor());
        vista.getBtnEditarProveedor().addActionListener(e -> editarProveedor());
        vista.getBtnEstadoProveedor().addActionListener(e -> cambiarEstadoProveedor());
        vista.getTblProveedores().getTabla().getSelectionModel().addListSelectionListener(e -> {
            Proveedor p = proveedorSeleccionado();
            if (p != null) {
                vista.getBtnEstadoProveedor().setText(p.isActivo() ? "Desactivar" : "Reactivar");
            }
        });
    }

    private void recargarProveedores() {
        try {
            proveedores = proveedorDAO.listar(filtroEstado(vista.getCmbEstadoProveedores().getSelectedIndex()));
        } catch (SQLException ex) {
            proveedores = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los proveedores", "dato"));
        }
        PnlTabla t = vista.getTblProveedores();
        t.getModelo().setRowCount(0);
        for (Proveedor p : proveedores) {
            t.getModelo().addRow(new Object[]{
                p.getNombre(), p.getContacto(), p.getTelefono(), p.getCorreo(), p.getEstadoTexto()});
        }
    }

    private void nuevoProveedor() {
        DlgProveedor dlg = new DlgProveedor(ventana, null);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            proveedorDAO.insertar(dlg.getProveedor());
            Mensajes.info(ventana, "Proveedor registrado.");
            recargarProveedores();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "guardar el proveedor", "nombre"));
        }
    }

    private void editarProveedor() {
        Proveedor seleccionado = proveedorSeleccionado();
        if (seleccionado == null) {
            return;
        }
        // Se edita una copia: si el usuario cancela o falla el guardado, la lista no cambia.
        DlgProveedor dlg = new DlgProveedor(ventana, copiar(seleccionado));
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            proveedorDAO.actualizar(dlg.getProveedor());
            Mensajes.info(ventana, "Proveedor actualizado.");
            recargarProveedores();
            recargarRepuestos(); // el nombre del proveedor aparece en la tabla de repuestos
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "actualizar el proveedor", "nombre"));
        }
    }

    private void cambiarEstadoProveedor() {
        Proveedor p = proveedorSeleccionado();
        if (p == null) {
            return;
        }
        boolean activar = !p.isActivo();
        if (!activar && !Mensajes.confirmar(ventana, "Desactivar proveedor",
                "¿Desactivar a " + p.getNombre() + "? Sus repuestos se conservan, pero ya no se podrá elegir en formularios nuevos.")) {
            return;
        }
        try {
            proveedorDAO.cambiarEstado(p.getId(), activar);
            recargarProveedores();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cambiar el estado del proveedor", "dato"));
        }
    }

    private Proveedor proveedorSeleccionado() {
        return seleccionado(vista.getTblProveedores(), proveedores);
    }

    /**
     * Proveedores activos para los combos (más el actual del repuesto si está inactivo).
     * Devuelve null si no hay ninguno.
     */
    private List<Proveedor> proveedoresParaElegir(Integer idActual) {
        try {
            List<Proveedor> lista = new ArrayList<>(proveedorDAO.listar(true));
            if (idActual != null && idActual > 0 && lista.stream().noneMatch(p -> p.getId() == idActual)) {
                proveedorDAO.listar(false).stream().filter(p -> p.getId() == idActual).findFirst().ifPresent(lista::add);
            }
            if (lista.isEmpty()) {
                Mensajes.advertencia(ventana, "Primero registra un proveedor (pestaña Proveedores): todo repuesto necesita uno.");
                return null;
            }
            return lista;
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los proveedores", "dato"));
            return null;
        }
    }

    // ================================================================= movimientos (solo lectura)

    private void conectarMovimientos() {
        vista.getCmbTipoMovimiento().addActionListener(e -> recargarMovimientos());
    }

    private void recargarMovimientos() {
        int combo = vista.getCmbTipoMovimiento().getSelectedIndex();
        String tipo = combo <= 0 ? null : (String) vista.getCmbTipoMovimiento().getSelectedItem();
        try {
            movimientos = movimientoDAO.listar(tipo);
        } catch (SQLException ex) {
            movimientos = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los movimientos", "dato"));
        }
        PnlTabla t = vista.getTblMovimientos();
        t.getModelo().setRowCount(0);
        for (MovimientoInventario m : movimientos) {
            t.getModelo().addRow(new Object[]{
                m.codigo(), Formato.fechaHora(m.fecha()), m.repuesto(), m.tipo(), m.cantidad(),
                Formato.dinero(m.costoUnitario()), m.origen() == null ? "—" : m.origen(),
                m.usuario() == null ? "—" : m.usuario()});
        }
    }

    // ================================================================= motores

    private void conectarMotores() {
        vista.getCmbCombustibleMotores().addActionListener(e -> recargarMotores());
        vista.getCmbEstadoMotores().addActionListener(e -> recargarMotores());
        vista.getBtnNuevoMotor().addActionListener(e -> nuevoMotor());
        vista.getBtnEditarMotor().addActionListener(e -> editarMotor());
        vista.getBtnCompatibles().addActionListener(e -> buscarCompatibles());
    }

    private void recargarMotores() {
        int comb = vista.getCmbCombustibleMotores().getSelectedIndex();
        int est = vista.getCmbEstadoMotores().getSelectedIndex();
        try {
            motores = motorDAO.listar(comb <= 0 ? null : Vehiculo.COMBUSTIBLES[comb - 1],
                    est <= 0 ? null : Motor.ESTADOS[est - 1]);
        } catch (SQLException ex) {
            motores = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los motores", "dato"));
        }
        PnlTabla t = vista.getTblMotores();
        t.getModelo().setRowCount(0);
        for (Motor m : motores) {
            t.getModelo().addRow(new Object[]{
                m.getNombreSerie(), m.getMarca(), m.getModeloCompatible(), m.getAnios(),
                m.getCilindraje() == null ? "" : m.getCilindraje().toPlainString() + " L",
                m.getCombustible(), m.getEstado()});
        }
    }

    private void nuevoMotor() {
        List<Repuesto> sinFicha = repuestosMotorParaElegir(null);
        if (sinFicha == null) {
            return;
        }
        DlgMotor dlg = new DlgMotor(ventana, null, sinFicha);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            motorDAO.insertar(dlg.getMotor());
            Mensajes.info(ventana, "Motor registrado.");
            recargarMotores();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "guardar el motor", "número de serie o repuesto"));
        }
    }

    private void editarMotor() {
        Motor seleccionado = motorSeleccionado();
        if (seleccionado == null) {
            return;
        }
        List<Repuesto> lista = repuestosMotorParaElegir(seleccionado.getIdRepuesto());
        if (lista == null) {
            return;
        }
        // Se edita una copia: si el usuario cancela o falla el guardado, la lista no cambia.
        DlgMotor dlg = new DlgMotor(ventana, copiar(seleccionado), lista);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            motorDAO.actualizar(dlg.getMotor());
            Mensajes.info(ventana, "Motor actualizado.");
            recargarMotores();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "actualizar el motor", "número de serie"));
        }
    }

    /** Abre la búsqueda de compatibles con los datos del motor seleccionado como punto de partida. */
    private void buscarCompatibles() {
        Motor base = motorSeleccionado();
        DlgMotoresCompatibles dlg = new DlgMotoresCompatibles(ventana, base);
        dlg.getBtnBuscar().addActionListener(e -> {
            String problema = dlg.validarCriterios();
            dlg.mostrarError(problema);
            if (problema != null) {
                return;
            }
            try {
                List<Motor> resultado = motorDAO.buscarCompatibles(
                        dlg.getMarca(), dlg.getAnio(), dlg.getCilindraje(), dlg.getCombustible());
                PnlTabla t = dlg.getTblResultados();
                t.getModelo().setRowCount(0);
                for (Motor m : resultado) {
                    t.getModelo().addRow(new Object[]{
                        m.getNombreSerie(), m.getMarca(), m.getModeloCompatible(), m.getAnios(),
                        m.getCilindraje().toPlainString() + " L", m.getCombustible(),
                        Formato.dinero(m.getPrecioVenta())});
                }
                if (resultado.isEmpty()) {
                    dlg.mostrarError("No hay motores disponibles con esos criterios.");
                }
            } catch (SQLException ex) {
                dlg.mostrarError(ErroresSQL.mensaje(ex, "buscar los motores", "dato"));
            }
        });
        dlg.getBtnBuscar().doClick(0); // muestra de entrada los resultados para los datos propuestos
        dlg.setVisible(true);
    }

    private Motor motorSeleccionado() {
        return seleccionado(vista.getTblMotores(), motores);
    }

    /** Repuestos de categoría Motor sin ficha (más el actual al editar). Devuelve null si no hay ninguno. */
    private List<Repuesto> repuestosMotorParaElegir(Integer idActual) {
        try {
            List<Repuesto> lista = repuestoDAO.listarMotoresSinFicha(idActual);
            if (lista.isEmpty()) {
                Mensajes.advertencia(ventana, "No hay repuestos de categoría Motor sin ficha técnica. "
                        + "Primero registra el repuesto en la pestaña Repuestos con categoría Motor.");
                return null;
            }
            return lista;
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los repuestos", "dato"));
            return null;
        }
    }

    // ================================================================= comunes

    /** 0 = Todos, 1 = Activos, 2 = Inactivos. */
    private static Boolean filtroEstado(int indice) {
        return indice == 1 ? Boolean.TRUE : indice == 2 ? Boolean.FALSE : null;
    }

    private static <T> T seleccionado(PnlTabla tabla, List<T> datos) {
        JTable t = tabla.getTabla();
        int fila = t.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        int modelo = t.convertRowIndexToModel(fila);
        return modelo < datos.size() ? datos.get(modelo) : null;
    }

    private static Proveedor copiar(Proveedor o) {
        Proveedor p = new Proveedor();
        p.setId(o.getId());
        p.setCodigo(o.getCodigo());
        p.setActivo(o.isActivo());
        p.setNombre(o.getNombre());
        p.setContacto(o.getContacto());
        p.setTelefono(o.getTelefono());
        p.setCorreo(o.getCorreo());
        return p;
    }

    private static Motor copiar(Motor o) {
        Motor m = new Motor();
        m.setId(o.getId());
        m.setCodigo(o.getCodigo());
        m.setIdRepuesto(o.getIdRepuesto());
        m.setNumeroSerie(o.getNumeroSerie());
        m.setMarca(o.getMarca());
        m.setModeloCompatible(o.getModeloCompatible());
        m.setAnioDesde(o.getAnioDesde());
        m.setAnioHasta(o.getAnioHasta());
        m.setCilindraje(o.getCilindraje());
        m.setCombustible(o.getCombustible());
        m.setEstado(o.getEstado());
        m.setRepuesto(o.getRepuesto());
        m.setPrecioVenta(o.getPrecioVenta());
        return m;
    }
}
