package views.paneles;

/**
 * Pantalla "Órdenes de trabajo".
 * Su título viene de PnlSeccion; el contenido se arma en getContenido().
 */
public class PnlOrdenes extends PnlSeccion {

    public PnlOrdenes() {
        super("Órdenes de trabajo",
                "Ingreso, seguimiento y entrega de cada vehículo");
        // Aquí se construye el contenido de la pantalla: getContenido().add(...)
    }
}