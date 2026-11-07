package views.paneles;

/**
 * Pantalla "Inventario".
 * Su título viene de PnlSeccion; el contenido se arma en getContenido().
 */
public class PnlInventario extends PnlSeccion {

    public PnlInventario() {
        super("Inventario",
                "Repuestos, proveedores y movimientos de almacén");
        // Aquí se construye el contenido de la pantalla: getContenido().add(...)
    }
}