package views.paneles;

import javax.swing.JTabbedPane;
import views.componentes.PnlTabla;

/**
 * Pantalla "Clientes y vehículos": dos pestañas con su tabla, buscador y filtros.
 */
public class PnlClientes extends PnlSeccion {

    private final PnlTabla tblClientes = new PnlTabla(
            "Documento", "Nombres", "Apellidos", "Teléfono", "Correo", "Vehículos", "Estado");

    private final PnlTabla tblVehiculos = new PnlTabla(
            "Placa", "VIN / Chasis", "Marca", "Modelo", "Año", "Color", "Kilometraje",
            "Combustible", "Propietario");

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
        tblClientes.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        tblClientes.addAccion("Nuevo cliente", true, false);
        tblClientes.addAccion("Editar", false, true);
        tblClientes.addAccion("Desactivar", false, true);
        tblClientes.setAnchos(110, 160, 160, 110, 210, 80, 90);
        tblClientes.alinearDerecha(5);
    }

    private void configurarVehiculos() {
        tblVehiculos.setPlaceholder("Buscar por placa, VIN, marca o propietario...");
        tblVehiculos.addFiltro("Combustible", "Todos", "Gasolina", "Diésel", "Híbrido", "Eléctrico", "Gas");
        tblVehiculos.addAccion("Nuevo vehículo", true, false);
        tblVehiculos.addAccion("Editar", false, true);
        tblVehiculos.setAnchos(90, 170, 100, 110, 60, 90, 100, 100, 170);
        tblVehiculos.alinearDerecha(4, 6);
    }
}