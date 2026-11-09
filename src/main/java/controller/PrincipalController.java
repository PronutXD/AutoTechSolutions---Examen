
package controller;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.EventQueue;
import java.util.Collections;
import model.Sesion;
import model.Sesion.Usuario;
import javax.swing.UIManager;
import views.FrmLogin;
import views.FrmPrincipal;
import views.FrmPrincipal.Icono;
import views.paneles.*;

/**
 * Controlador de la ventana principal.
 *
 * Decide qué secciones existen (etiqueta del menú, icono y panel de cada una) y
 * qué pasa cuando el usuario navega o cierra sesión.
 */
public class PrincipalController {

    private static final String CLIENTES = "clientes";
    private static final String ORDENES = "ordenes";
    private static final String INVENTARIO = "inventario";
    private static final String CATALOGOS = "catalogos";
    private static final String CONSULTAS = "consultas";

    private final FrmPrincipal vista;
    private final Usuario usuario;

    public PrincipalController(FrmPrincipal vista, Usuario usuario) {
        this.vista = vista;
        this.usuario = usuario;
    }

    /** Arma la ventana, conecta los eventos y la muestra. */
    public void iniciar() {
        vista.setUsuario(usuario.usuario());

        // ======================== CLIENTES ========================
        PnlClientes pnlClientes = new PnlClientes();
        vista.agregarSeccion(
                CLIENTES,
                "Clientes y vehículos",
                Icono.CLIENTES,
                pnlClientes
        );
        new ClientesController(pnlClientes, vista).iniciar();

        // ======================== ÓRDENES ========================
        PnlOrdenes pnlOrdenes = new PnlOrdenes();
        vista.agregarSeccion(
                ORDENES,
                "Órdenes de trabajo",
                Icono.ORDENES,
                pnlOrdenes
        );
        new OrdenController(pnlOrdenes, vista).iniciar();

        // ======================== INVENTARIO ========================
        PnlInventario pnlInventario = new PnlInventario();
        vista.agregarSeccion(
                INVENTARIO,
                "Inventario",
                Icono.INVENTARIO,
                pnlInventario
        );
        new InventarioController(pnlInventario, vista).iniciar();

        // ======================== CATÁLOGOS ========================
        PnlCatalogos pnlCatalogos = new PnlCatalogos();
        vista.agregarSeccion(
                CATALOGOS,
                "Catálogos",
                Icono.CATALOGOS,
                pnlCatalogos
        );
        new CatalogosController(pnlCatalogos, vista).iniciar();

        // ======================== CONSULTAS ========================
        PnlConsultas pnlConsultas = new PnlConsultas();
        vista.agregarSeccion(
                CONSULTAS,
                "Consultas",
                Icono.CONSULTAS,
                pnlConsultas
        );
        new ConsultasController(pnlConsultas, vista).iniciar();

        // ======================== EVENTOS ========================
        vista.setAlNavegar(this::navegar);
        vista.setAlCerrarSesion(this::cerrarSesion);

        // Mostrar sección inicial
        vista.mostrarSeccion(CLIENTES);

        // Mostrar ventana principal
        vista.setVisible(true);
    }

    /** Navegación entre las secciones. */
    private void navegar(String clave) {
        vista.mostrarSeccion(clave);
    }

    /** Cierre de sesión. */
    private void cerrarSesion() {
        if (vista.confirmar(
                "Cerrar sesión",
                "¿Deseas cerrar la sesión?")) {

            Sesion.cerrar();

            new FrmLogin().setVisible(true);

            vista.dispose();
        }
    }

    // =================================================================
    // MAIN DE PRUEBA
    // =================================================================

    /**
     * Permite probar la ventana principal sin pasar por el login.
     * Se puede ejecutar directamente desde el IDE.
     */
    public static void main(String[] args) {

        FlatLaf.setGlobalExtraDefaults(
                Collections.singletonMap("@accentColor", "#E45E16")
        );

        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("CheckBox.arc", 5);
        UIManager.put("Component.focusWidth", 1);

        FlatLightLaf.setup();

        EventQueue.invokeLater(() -> {

            Usuario prueba = new Usuario(
                    0,
                    "admin",
                    "Administrador",
                    null,
                    false
            );

            new PrincipalController(
                    new FrmPrincipal(),
                    prueba
            ).iniciar();
        });
    }
}
