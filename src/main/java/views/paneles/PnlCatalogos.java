package views.paneles;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import model.Cuenta;
import model.Servicio;
import views.componentes.PnlTabla;

/**
 * Pantalla "Catálogos": listas que el taller mantiene (servicios, mecánicos)
 * y las cuentas de acceso al sistema (usuarios).
 *
 * Solo diseño: expone sus tablas, filtros y botones para que CatalogosController
 * les conecte los eventos y llene los datos.
 */
public class PnlCatalogos extends PnlSeccion {

    private final PnlTabla tblServicios = new PnlTabla(
            "Código", "Servicio", "Nivel", "Mano de obra", "Horas estimadas", "Garantía (días)", "Estado");

    private final PnlTabla tblMecanicos = new PnlTabla(
            "Documento", "Nombre", "Teléfono", "Especialidad", "Estado", "Disponibilidad");

    private final PnlTabla tblUsuarios = new PnlTabla(
            "Código", "Usuario", "Mecánico asociado", "Rol", "Estado", "Último acceso");

    private JComboBox<String> cmbNivel;
    private JComboBox<String> cmbEstadoServicios;
    private JButton btnNuevoServicio;
    private JButton btnEditarServicio;
    private JButton btnEstadoServicio;

    private JComboBox<String> cmbEstadoMecanicos;
    private JComboBox<String> cmbDisponibilidad;
    private JButton btnNuevoMecanico;
    private JButton btnEditarMecanico;
    private JButton btnEstadoMecanico;

    private JComboBox<String> cmbRol;
    private JComboBox<String> cmbEstadoUsuarios;
    private JButton btnNuevoUsuario;
    private JButton btnEditarUsuario;
    private JButton btnClave;
    private JButton btnEstadoUsuario;

    private final JTabbedPane tabs;

    public PnlCatalogos() {
        super("Catálogos",
                "Servicios del taller, personal mecánico y cuentas de acceso");

        configurarServicios();
        configurarMecanicos();
        configurarUsuarios();

        tabs = crearPestanas();
        agregarPestana(tabs, "Servicios", tblServicios);
        agregarPestana(tabs, "Mecánicos", tblMecanicos);
        agregarPestana(tabs, "Usuarios", tblUsuarios);
    }

    private void configurarServicios() {
        tblServicios.setPlaceholder("Buscar por código o nombre del servicio...");
        cmbNivel = tblServicios.addFiltro("Nivel", conTodos("Todos", Servicio.NIVELES));
        cmbEstadoServicios = tblServicios.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        btnNuevoServicio = tblServicios.addAccion("Nuevo servicio", true, false);
        btnEditarServicio = tblServicios.addAccion("Editar", false, true);
        btnEstadoServicio = tblServicios.addAccion("Desactivar", false, true);
        tblServicios.setAnchos(90, 260, 110, 110, 120, 110, 90);
        tblServicios.alinearDerecha(3, 4, 5);
    }

    private void configurarMecanicos() {
        tblMecanicos.setPlaceholder("Buscar por nombre, documento o especialidad...");
        cmbEstadoMecanicos = tblMecanicos.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        cmbDisponibilidad = tblMecanicos.addFiltro("Disponibilidad", "Todos", "Disponibles", "No disponibles");
        btnNuevoMecanico = tblMecanicos.addAccion("Nuevo mecánico", true, false);
        btnEditarMecanico = tblMecanicos.addAccion("Editar", false, true);
        btnEstadoMecanico = tblMecanicos.addAccion("Desactivar", false, true);
        tblMecanicos.setAnchos(110, 220, 120, 160, 90, 120);
    }

    /**
     * Cada cuenta se asocia a un mecánico (el formulario ofrece solo los que aún
     * no tienen cuenta) o queda sin mecánico, para un administrador o un tercero.
     */
    private void configurarUsuarios() {
        tblUsuarios.setPlaceholder("Buscar por usuario o mecánico...");
        cmbRol = tblUsuarios.addFiltro("Rol", conTodos("Todos", Cuenta.ROLES));
        cmbEstadoUsuarios = tblUsuarios.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        btnNuevoUsuario = tblUsuarios.addAccion("Nuevo usuario", true, false);
        btnEditarUsuario = tblUsuarios.addAccion("Editar", false, true);
        btnClave = tblUsuarios.addAccion("Restablecer contraseña", false, true);
        btnEstadoUsuario = tblUsuarios.addAccion("Desactivar", false, true);
        tblUsuarios.setAnchos(110, 150, 220, 120, 90, 150);
    }

    /** Solo el administrador gestiona cuentas: para los demás roles la pestaña no existe. */
    public void quitarPestanaUsuarios() {
        int i = tabs.indexOfTab("Usuarios");
        if (i >= 0) {
            tabs.removeTabAt(i);
        }
    }

    private static String[] conTodos(String todos, String[] valores) {
        String[] opciones = new String[valores.length + 1];
        opciones[0] = todos;
        System.arraycopy(valores, 0, opciones, 1, valores.length);
        return opciones;
    }

    // ================================================================= acceso para el controlador

    public JTabbedPane getTabs() { return tabs; }

    public PnlTabla getTblServicios() { return tblServicios; }
    public PnlTabla getTblMecanicos() { return tblMecanicos; }
    public PnlTabla getTblUsuarios() { return tblUsuarios; }

    public JComboBox<String> getCmbNivel() { return cmbNivel; }
    public JComboBox<String> getCmbEstadoServicios() { return cmbEstadoServicios; }
    public JButton getBtnNuevoServicio() { return btnNuevoServicio; }
    public JButton getBtnEditarServicio() { return btnEditarServicio; }
    public JButton getBtnEstadoServicio() { return btnEstadoServicio; }

    public JComboBox<String> getCmbEstadoMecanicos() { return cmbEstadoMecanicos; }
    public JComboBox<String> getCmbDisponibilidad() { return cmbDisponibilidad; }
    public JButton getBtnNuevoMecanico() { return btnNuevoMecanico; }
    public JButton getBtnEditarMecanico() { return btnEditarMecanico; }
    public JButton getBtnEstadoMecanico() { return btnEstadoMecanico; }

    public JComboBox<String> getCmbRol() { return cmbRol; }
    public JComboBox<String> getCmbEstadoUsuarios() { return cmbEstadoUsuarios; }
    public JButton getBtnNuevoUsuario() { return btnNuevoUsuario; }
    public JButton getBtnEditarUsuario() { return btnEditarUsuario; }
    public JButton getBtnClave() { return btnClave; }
    public JButton getBtnEstadoUsuario() { return btnEstadoUsuario; }
}
