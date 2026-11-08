package views.paneles;

import javax.swing.JTabbedPane;
import views.componentes.PnlTabla;

/**
 * Pantalla "Catálogos": listas que el taller mantiene (servicios, mecánicos)
 * y las cuentas de acceso al sistema (usuarios).
 */
public class PnlCatalogos extends PnlSeccion {

    private final PnlTabla tblServicios = new PnlTabla(
            "Código", "Servicio", "Nivel", "Mano de obra", "Horas estimadas", "Estado");

    private final PnlTabla tblMecanicos = new PnlTabla(
            "Documento", "Nombre", "Teléfono", "Especialidad", "Estado", "Disponibilidad");

    private final PnlTabla tblUsuarios = new PnlTabla(
            "Código", "Usuario", "Mecánico asociado", "Rol", "Estado", "Último acceso");

    public PnlCatalogos() {
        super("Catálogos",
                "Servicios del taller, personal mecánico y cuentas de acceso");

        configurarServicios();
        configurarMecanicos();
        configurarUsuarios();

        JTabbedPane tabs = crearPestanas();
        agregarPestana(tabs, "Servicios", tblServicios);
        agregarPestana(tabs, "Mecánicos", tblMecanicos);
        agregarPestana(tabs, "Usuarios", tblUsuarios);
    }

    private void configurarServicios() {
        tblServicios.setPlaceholder("Buscar por código o nombre del servicio...");
        tblServicios.addFiltro("Nivel", "Todos", "1 · Básico", "2 · Preventivo", "3 · Intermedio",
                "4 · Avanzado", "5 · Mayor");
        tblServicios.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        tblServicios.addAccion("Nuevo servicio", true, false);
        tblServicios.addAccion("Editar", false, true);
        tblServicios.addAccion("Desactivar", false, true);
        tblServicios.setAnchos(90, 260, 70, 110, 120, 90);
        tblServicios.alinearDerecha(3, 4);
    }

    private void configurarMecanicos() {
        tblMecanicos.setPlaceholder("Buscar por nombre, documento o especialidad...");
        tblMecanicos.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        tblMecanicos.addFiltro("Disponibilidad", "Todos", "Disponibles", "No disponibles");
        tblMecanicos.addAccion("Nuevo mecánico", true, false);
        tblMecanicos.addAccion("Editar", false, true);
        tblMecanicos.addAccion("Desactivar", false, true);
        tblMecanicos.setAnchos(110, 220, 120, 160, 90, 120);
    }

    /**
     * Cada cuenta se asocia a un mecánico (el formulario ofrece solo los que aún
     * no tienen cuenta) o queda sin mecánico, para un administrador o un tercero.
     */
    private void configurarUsuarios() {
        tblUsuarios.setPlaceholder("Buscar por usuario o mecánico...");
        tblUsuarios.addFiltro("Rol", "Todos", "Administrador", "Mecánico", "Recepción");
        tblUsuarios.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        tblUsuarios.addAccion("Nuevo usuario", true, false);
        tblUsuarios.addAccion("Editar", false, true);
        tblUsuarios.addAccion("Restablecer contraseña", false, true);
        tblUsuarios.addAccion("Desactivar", false, true);
        tblUsuarios.setAnchos(110, 150, 220, 120, 90, 150);
    }
}