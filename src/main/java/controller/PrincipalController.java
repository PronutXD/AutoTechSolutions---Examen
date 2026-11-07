package controller;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.EventQueue;
import java.util.Collections;
import javax.swing.UIManager;
import views.FrmLogin;
import views.FrmPrincipal;
import views.FrmPrincipal.Icono;
import views.paneles.*;

/**
 * Controlador de la ventana principal.
 *
 * Decide qué secciones existen (etiqueta del menú, icono y panel de cada una) y
 * qué pasa cuando el usuario navega o cierra sesión. La vista (FrmPrincipal) solo
 * dibuja, y cada panel pone su propio título y contenido.
 */
public class PrincipalController {

    private static final String CLIENTES = "clientes";
    private static final String ORDENES = "ordenes";
    private static final String INVENTARIO = "inventario";
    private static final String CATALOGOS = "catalogos";
    private static final String CONSULTAS = "consultas";

    private final FrmPrincipal vista;
    private final String usuario;

    public PrincipalController(FrmPrincipal vista, String usuario) {
        this.vista = vista;
        this.usuario = usuario;
    }

    /** Arma la ventana, conecta los eventos y la muestra. */
    public void iniciar() {
        vista.setUsuario(usuario);

        vista.agregarSeccion(CLIENTES, "Clientes y vehículos", Icono.CLIENTES, new PnlClientes());
        vista.agregarSeccion(ORDENES, "Órdenes de trabajo", Icono.ORDENES, new PnlOrdenes());
        vista.agregarSeccion(INVENTARIO, "Inventario", Icono.INVENTARIO, new PnlInventario());
        vista.agregarSeccion(CATALOGOS, "Catálogos", Icono.CATALOGOS, new PnlCatalogos());
        vista.agregarSeccion(CONSULTAS, "Consultas", Icono.CONSULTAS, new PnlConsultas());

        vista.setAlNavegar(this::navegar);
        vista.setAlCerrarSesion(this::cerrarSesion);

        vista.mostrarSeccion(CLIENTES);
        vista.setVisible(true);
    }

    private void navegar(String clave) {
        vista.mostrarSeccion(clave);
    }

    private void cerrarSesion() {
        if (vista.confirmar("Cerrar sesión", "¿Deseas cerrar la sesión?")) {
            new FrmLogin().setVisible(true);
            vista.dispose();
        }
    }

    // ================================================================= main de prueba

    /**
     * Solo para probar la ventana principal sin pasar por el login
     * (clic derecho sobre este archivo, Run File). Se puede borrar.
     */
    public static void main(String[] args) {
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#E45E16"));

        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("CheckBox.arc", 5);
        UIManager.put("Component.focusWidth", 1);

        FlatLightLaf.setup();

        EventQueue.invokeLater(() -> new PrincipalController(new FrmPrincipal(), "admin").iniciar());
    }
}