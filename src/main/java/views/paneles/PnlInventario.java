package views.paneles;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import model.Repuesto;
import model.Vehiculo;
import model.inventario.Motor;
import views.componentes.PnlTabla;

/**
 * Pantalla "Inventario": repuestos, proveedores, movimientos de almacén y motores.
 *
 * Los motores también son repuestos: aparecen en la lista de Repuestos con su
 * nombre y número de serie, y desde ahí (clic derecho) se salta a la pestaña
 * Motores, que muestra la ficha técnica y permite buscar motores compatibles.
 *
 * Solo diseño: expone sus tablas, filtros y botones para que InventarioController
 * les conecte los eventos y llene los datos.
 */
public class PnlInventario extends PnlSeccion {

    private static final int COL_REPUESTO = 1;
    private static final int COL_CATEGORIA = 2;

    private final PnlTabla tblRepuestos = new PnlTabla(
            "Código", "Repuesto", "Categoría", "Proveedor", "Stock", "Mínimo", "Costo", "Precio venta", "Estado");

    private final PnlTabla tblProveedores = new PnlTabla(
            "Nombre", "Contacto", "Teléfono", "Correo", "Estado");

    private final PnlTabla tblMovimientos = new PnlTabla(
            "Código", "Fecha", "Repuesto", "Tipo", "Cantidad", "Costo unitario", "Proveedor / Orden", "Usuario");

    private final PnlTabla tblMotores = new PnlTabla(
            "Motor (nombre / serie)", "Marca", "Modelo compatible", "Años", "Cilindraje", "Combustible", "Estado");

    private JComboBox<String> cmbCategoria;
    private JCheckBox chkStockBajo;
    private JComboBox<String> cmbEstadoRepuestos;
    private JButton btnNuevoRepuesto;
    private JButton btnEntrada;
    private JButton btnEditarRepuesto;
    private JButton btnEstadoRepuesto;

    private JComboBox<String> cmbEstadoProveedores;
    private JButton btnNuevoProveedor;
    private JButton btnEditarProveedor;
    private JButton btnEstadoProveedor;

    private JComboBox<String> cmbTipoMovimiento;

    private JComboBox<String> cmbCombustibleMotores;
    private JComboBox<String> cmbEstadoMotores;
    private JButton btnNuevoMotor;
    private JButton btnCompatibles;
    private JButton btnEditarMotor;

    private final JTabbedPane tabs;
    private int indiceMotores;

    public PnlInventario() {
        super("Inventario",
                "Repuestos, proveedores, movimientos de almacén y motores");

        configurarRepuestos();
        configurarProveedores();
        configurarMovimientos();
        configurarMotores();

        tabs = crearPestanas();
        agregarPestana(tabs, "Repuestos", tblRepuestos);
        agregarPestana(tabs, "Proveedores", tblProveedores);
        agregarPestana(tabs, "Movimientos", tblMovimientos);
        agregarPestana(tabs, "Motores", tblMotores);
        indiceMotores = 3;
    }

    private void configurarRepuestos() {
        tblRepuestos.setPlaceholder("Buscar por código, nombre o proveedor...");
        cmbCategoria = tblRepuestos.addFiltro("Categoría", conTodos("Todas", Repuesto.CATEGORIAS));
        cmbEstadoRepuestos = tblRepuestos.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        chkStockBajo = tblRepuestos.addFiltroCheck("Solo stock bajo");
        btnNuevoRepuesto = tblRepuestos.addAccion("Nuevo repuesto", true, false);
        btnEntrada = tblRepuestos.addAccion("Registrar entrada", false, true);
        btnEditarRepuesto = tblRepuestos.addAccion("Editar", false, true);
        btnEstadoRepuesto = tblRepuestos.addAccion("Desactivar", false, true);
        tblRepuestos.setAnchos(110, 230, 110, 150, 60, 70, 80, 100, 80);
        tblRepuestos.alinearDerecha(4, 5, 6, 7);

        tblRepuestos.addMenuContextual("Ver detalles del motor")
                .addActionListener(e -> verDetalleMotor());
    }

    private void configurarProveedores() {
        tblProveedores.setPlaceholder("Buscar por nombre, contacto o correo...");
        cmbEstadoProveedores = tblProveedores.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        btnNuevoProveedor = tblProveedores.addAccion("Nuevo proveedor", true, false);
        btnEditarProveedor = tblProveedores.addAccion("Editar", false, true);
        btnEstadoProveedor = tblProveedores.addAccion("Desactivar", false, true);
        tblProveedores.setAnchos(200, 160, 120, 220, 90);
    }

    /** Solo lectura: los movimientos los generan las entradas y los consumos de las órdenes. */
    private void configurarMovimientos() {
        tblMovimientos.setPlaceholder("Buscar por código, repuesto, proveedor u orden...");
        cmbTipoMovimiento = tblMovimientos.addFiltro("Tipo", "Todos", "Entrada", "Salida");
        tblMovimientos.setAnchos(110, 130, 220, 80, 80, 110, 180, 110);
        tblMovimientos.alinearDerecha(4, 5);
    }

    private void configurarMotores() {
        tblMotores.setPlaceholder("Buscar por serie, marca, modelo o cilindraje...");
        cmbCombustibleMotores = tblMotores.addFiltro("Combustible", conTodos("Todos", Vehiculo.COMBUSTIBLES));
        cmbEstadoMotores = tblMotores.addFiltro("Estado", conTodos("Todos", Motor.ESTADOS));
        btnNuevoMotor = tblMotores.addAccion("Nuevo motor", true, false);
        btnCompatibles = tblMotores.addAccion("Buscar compatibles", false, true);
        btnEditarMotor = tblMotores.addAccion("Editar", false, true);
        tblMotores.setAnchos(220, 110, 160, 110, 90, 110, 100);
        tblMotores.alinearDerecha(4);
    }

    /** Clic derecho en Repuestos: salta a Motores y deja filtrado ese motor. */
    private void verDetalleMotor() {
        Object categoria = tblRepuestos.getValorSeleccionado(COL_CATEGORIA);
        if (!Repuesto.CATEGORIA_MOTOR.equals(categoria)) {
            JOptionPane.showMessageDialog(this, "Esta opción solo aplica a repuestos de categoría Motor.",
                    "Ver detalles del motor", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object nombre = tblRepuestos.getValorSeleccionado(COL_REPUESTO);
        tblMotores.getBuscador().setText(nombre == null ? "" : nombre.toString());
        tabs.setSelectedIndex(indiceMotores);
    }

    private static String[] conTodos(String todos, String[] valores) {
        String[] opciones = new String[valores.length + 1];
        opciones[0] = todos;
        System.arraycopy(valores, 0, opciones, 1, valores.length);
        return opciones;
    }

    // ================================================================= acceso para el controlador

    public JTabbedPane getTabs() { return tabs; }

    public PnlTabla getTblRepuestos() { return tblRepuestos; }
    public PnlTabla getTblProveedores() { return tblProveedores; }
    public PnlTabla getTblMovimientos() { return tblMovimientos; }
    public PnlTabla getTblMotores() { return tblMotores; }

    public JComboBox<String> getCmbCategoria() { return cmbCategoria; }
    public JCheckBox getChkStockBajo() { return chkStockBajo; }
    public JComboBox<String> getCmbEstadoRepuestos() { return cmbEstadoRepuestos; }
    public JButton getBtnNuevoRepuesto() { return btnNuevoRepuesto; }
    public JButton getBtnEntrada() { return btnEntrada; }
    public JButton getBtnEditarRepuesto() { return btnEditarRepuesto; }
    public JButton getBtnEstadoRepuesto() { return btnEstadoRepuesto; }

    public JComboBox<String> getCmbEstadoProveedores() { return cmbEstadoProveedores; }
    public JButton getBtnNuevoProveedor() { return btnNuevoProveedor; }
    public JButton getBtnEditarProveedor() { return btnEditarProveedor; }
    public JButton getBtnEstadoProveedor() { return btnEstadoProveedor; }

    public JComboBox<String> getCmbTipoMovimiento() { return cmbTipoMovimiento; }

    public JComboBox<String> getCmbCombustibleMotores() { return cmbCombustibleMotores; }
    public JComboBox<String> getCmbEstadoMotores() { return cmbEstadoMotores; }
    public JButton getBtnNuevoMotor() { return btnNuevoMotor; }
    public JButton getBtnCompatibles() { return btnCompatibles; }
    public JButton getBtnEditarMotor() { return btnEditarMotor; }
}
