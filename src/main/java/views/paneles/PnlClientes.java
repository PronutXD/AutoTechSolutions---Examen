package views.paneles;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import model.Vehiculo;
import views.componentes.PnlTabla;

/**
 * Pantalla "Clientes y vehículos": dos pestañas con su tabla, buscador y filtros.
 *
 * Solo diseño: expone sus tablas, filtros y botones para que ClientesController
 * les conecte los eventos y llene los datos.
 */
public class PnlClientes extends PnlSeccion {

    private final PnlTabla tblClientes = new PnlTabla(
            "Documento", "Nombres", "Apellidos", "Teléfono", "Correo", "Vehículos", "Estado");

    private final PnlTabla tblVehiculos = new PnlTabla(
            "Placa", "VIN / Chasis", "Marca", "Modelo", "Año", "Color", "Kilometraje",
            "Combustible", "Propietario", "Estado");

    private JComboBox<String> cmbEstadoClientes;
    private JButton btnNuevoCliente;
    private JButton btnEditarCliente;
    private JButton btnEstadoCliente;

    private JComboBox<String> cmbCombustible;
    private JComboBox<String> cmbEstadoVehiculos;
    private JButton btnNuevoVehiculo;
    private JButton btnEditarVehiculo;
    private JButton btnEstadoVehiculo;

    public PnlClientes() {
        super("Clientes y vehículos",
                "Registro de clientes y de los vehículos de cada uno");

        configurarClientes();
        configurarVehiculos();

        JTabbedPane tabs = crearPestanas();
        agregarPestana(tabs, "Clientes", tblClientes);
        agregarPestana(tabs, "Vehículos", tblVehiculos);
    }

    private void configurarClientes() {
        tblClientes.setPlaceholder("Buscar por nombre, documento o teléfono...");
        cmbEstadoClientes = tblClientes.addFiltro("Estado", "Todos", "Activos", "Inactivos");

        btnNuevoCliente = tblClientes.addAccion("Nuevo cliente", true, false);
        btnEditarCliente = tblClientes.addAccion("Editar", false, true);
        btnEstadoCliente = tblClientes.addAccion("Desactivar", false, true);

        tblClientes.setAnchos(110, 160, 160, 110, 210, 80, 90);
        tblClientes.alinearDerecha(5);
    }

    private void configurarVehiculos() {
        tblVehiculos.setPlaceholder("Buscar por placa, VIN, marca o propietario...");
        String[] combustibles = new String[Vehiculo.COMBUSTIBLES.length + 1];
        combustibles[0] = "Todos";
        System.arraycopy(Vehiculo.COMBUSTIBLES, 0, combustibles, 1, Vehiculo.COMBUSTIBLES.length);
        cmbCombustible = tblVehiculos.addFiltro("Combustible", combustibles);
        cmbEstadoVehiculos = tblVehiculos.addFiltro("Estado", "Todos", "Activos", "Inactivos");

        btnNuevoVehiculo = tblVehiculos.addAccion("Nuevo vehículo", true, false);
        btnEditarVehiculo = tblVehiculos.addAccion("Editar", false, true);
        btnEstadoVehiculo = tblVehiculos.addAccion("Desactivar", false, true);

        tblVehiculos.setAnchos(90, 170, 100, 110, 60, 90, 100, 100, 170, 80);
        tblVehiculos.alinearDerecha(4, 6);
    }

    // ================================================================= acceso para el controlador

    public PnlTabla getTblClientes() {
        return tblClientes;
    }

    public PnlTabla getTblVehiculos() {
        return tblVehiculos;
    }

    public JComboBox<String> getCmbEstadoClientes() {
        return cmbEstadoClientes;
    }

    public JButton getBtnNuevoCliente() {
        return btnNuevoCliente;
    }

    public JButton getBtnEditarCliente() {
        return btnEditarCliente;
    }

    public JButton getBtnEstadoCliente() {
        return btnEstadoCliente;
    }

    public JComboBox<String> getCmbCombustible() {
        return cmbCombustible;
    }

    public JComboBox<String> getCmbEstadoVehiculos() {
        return cmbEstadoVehiculos;
    }

    public JButton getBtnNuevoVehiculo() {
        return btnNuevoVehiculo;
    }

    public JButton getBtnEditarVehiculo() {
        return btnEditarVehiculo;
    }

    public JButton getBtnEstadoVehiculo() {
        return btnEstadoVehiculo;
    }
}
