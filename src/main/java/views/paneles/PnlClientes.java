package views.paneles;

/**
 * Pantalla "Clientes y vehículos".
 * Su título viene de PnlSeccion; el contenido se arma en getContenido().
 */
public class PnlClientes extends PnlSeccion {

    public PnlClientes() {
        super("Clientes y vehículos",
                "Registro de clientes, sus vehículos y el historial de cada uno");
        // Aquí se construye el contenido de la pantalla: getContenido().add(...)
    }
}