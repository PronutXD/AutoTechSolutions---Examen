package views.paneles;

import javax.swing.JTabbedPane;
import views.componentes.PnlTabla;

/**
 * Pantalla "Consultas": información de solo lectura que sale de vistas SQL.
 * No tiene botones de edición.
 */
public class PnlConsultas extends PnlSeccion {

    private final PnlTabla tblHistorial = new PnlTabla(
            "Fecha", "Orden", "Vehículo", "Servicios", "Mecánicos", "Repuestos", "Costo total");

    private final PnlTabla tblBajoMinimo = new PnlTabla(
            "Código", "Repuesto", "Categoría", "Stock", "Mínimo", "Faltante", "Proveedor");

    private final PnlTabla tblSaldos = new PnlTabla(
            "Orden", "Cliente", "Vehículo", "Total", "Pagado", "Saldo", "Estado");

    private final PnlTabla tblAuditoria = new PnlTabla(
            "Fecha", "Usuario", "Tabla", "Registro", "Acción", "Valor anterior", "Valor nuevo");

    public PnlConsultas() {
        super("Consultas",
                "Información de solo lectura sobre el taller");

        configurarHistorial();
        configurarBajoMinimo();
        configurarSaldos();
        configurarAuditoria();

        JTabbedPane tabs = crearPestanas();
        agregarPestana(tabs, "Historial vehicular", tblHistorial);
        agregarPestana(tabs, "Repuestos bajo mínimo", tblBajoMinimo);
        agregarPestana(tabs, "Saldos por orden", tblSaldos);
        agregarPestana(tabs, "Auditoría", tblAuditoria);
    }

    private void configurarHistorial() {
        tblHistorial.setPlaceholder("Buscar por placa, cliente, servicio o mecánico...");
        tblHistorial.setAnchos(100, 80, 190, 200, 160, 180, 110);
        tblHistorial.alinearDerecha(6);
    }

    private void configurarBajoMinimo() {
        tblBajoMinimo.setPlaceholder("Buscar por código, repuesto o proveedor...");
        tblBajoMinimo.addFiltro("Categoría", "Todas", "Aceite", "Filtro", "Bujía", "Batería", "Freno",
                "Radiador", "Transmisión", "Motor", "Otro");
        tblBajoMinimo.setAnchos(90, 220, 110, 70, 70, 80, 160);
        tblBajoMinimo.alinearDerecha(3, 4, 5);
    }

    private void configurarSaldos() {
        tblSaldos.setPlaceholder("Buscar por orden, cliente o placa...");
        tblSaldos.addFiltroCheck("Solo con saldo pendiente");
        tblSaldos.setAnchos(80, 190, 190, 100, 100, 100, 150);
        tblSaldos.alinearDerecha(3, 4, 5);
    }

    private void configurarAuditoria() {
        tblAuditoria.setPlaceholder("Buscar por usuario, registro o valor...");
        tblAuditoria.addFiltro("Tabla", "Todas", "Órdenes", "Repuestos", "Pagos", "Usuarios");
        tblAuditoria.addFiltro("Acción", "Todas", "Inserción", "Modificación", "Eliminación");
        tblAuditoria.setAnchos(140, 120, 100, 130, 100, 200, 200);
    }
}