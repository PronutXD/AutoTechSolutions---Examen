package views.paneles;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import model.ordenes.Orden;
import views.componentes.PnlTabla;

/**
 * Pantalla "Órdenes de trabajo": listado de órdenes con filtro por estado.
 * Las columnas son las de vw_ListadoOrdenes y los estados son los de la base.
 *
 * Solo diseño: OrdenController le conecta los datos y los botones.
 */
public class PnlOrdenes extends PnlSeccion {

    private final PnlTabla tblOrdenes = new PnlTabla(
            "N° orden", "Ingreso", "Cliente", "Vehículo", "Responsable", "Estado", "Total");

    private JComboBox<String> cmbEstado;
    private JButton btnNuevaOrden;
    private JButton btnAbrirOrden;

    public PnlOrdenes() {
        super("Órdenes de trabajo",
                "Seguimiento de las órdenes de servicio del taller");

        tblOrdenes.setPlaceholder("Buscar por número, cliente, placa o responsable...");
        String[] estados = new String[Orden.ESTADOS.length + 1];
        estados[0] = "Todos";
        System.arraycopy(Orden.ESTADOS, 0, estados, 1, Orden.ESTADOS.length);
        cmbEstado = tblOrdenes.addFiltro("Estado", estados);

        btnNuevaOrden = tblOrdenes.addAccion("Nueva orden", true, false);
        btnAbrirOrden = tblOrdenes.addAccion("Abrir orden", false, true);

        tblOrdenes.setAnchos(130, 130, 190, 220, 170, 170, 100);
        tblOrdenes.alinearDerecha(6);

        JTabbedPane tabs = crearPestanas();
        agregarPestana(tabs, "Órdenes", tblOrdenes);
    }

    public PnlTabla getTblOrdenes() {
        return tblOrdenes;
    }

    public JComboBox<String> getCmbEstado() {
        return cmbEstado;
    }

    public JButton getBtnNuevaOrden() {
        return btnNuevaOrden;
    }

    public JButton getBtnAbrirOrden() {
        return btnAbrirOrden;
    }
}
