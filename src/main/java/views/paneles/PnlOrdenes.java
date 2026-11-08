package views.paneles;

import java.awt.BorderLayout;
import views.componentes.PnlTabla;

/**
 * Pantalla "Órdenes de trabajo": listado de órdenes con filtro por estado.
 * El detalle de cada orden (datos, diagnóstico y presupuesto, trabajo, calidad,
 * pagos y cambio de motor) se abre desde "Abrir orden" y se diseña aparte.
 */
public class PnlOrdenes extends PnlSeccion {

    private final PnlTabla tblOrdenes = new PnlTabla(
            "N° orden", "Ingreso", "Cliente", "Vehículo", "Responsable", "Estado", "Total");

    public PnlOrdenes() {
        super("Órdenes de trabajo",
                "Ingreso, seguimiento y entrega de cada vehículo");

        tblOrdenes.setPlaceholder("Buscar por número, cliente, placa o responsable...");
        tblOrdenes.addFiltro("Estado", "Todos", "Recibida", "En diagnóstico", "Pendiente de aprobación",
                "En reparación", "En control de calidad", "Finalizada y entregada",
                "Rechazada", "Cancelada");
        tblOrdenes.addAccion("Nueva orden", true, false);
        tblOrdenes.addAccion("Abrir orden", false, true);
        tblOrdenes.setAnchos(90, 100, 190, 190, 160, 170, 100);
        tblOrdenes.alinearDerecha(6);

        getContenido().add(tblOrdenes, BorderLayout.CENTER);
    }
}