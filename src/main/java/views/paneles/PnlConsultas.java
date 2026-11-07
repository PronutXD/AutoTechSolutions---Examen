package views.paneles;

/**
 * Pantalla "Consultas".
 * Su título viene de PnlSeccion; el contenido se arma en getContenido().
 */
public class PnlConsultas extends PnlSeccion {

    public PnlConsultas() {
        super("Consultas",
                "Información de solo lectura sobre el taller");
        // Aquí se construye el contenido de la pantalla: getContenido().add(...)
    }
}