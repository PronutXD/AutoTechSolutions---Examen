package views.paneles;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import model.Repuesto;
import views.componentes.Json;
import views.componentes.PnlTabla;

/**
 * Pantalla "Consultas": información de solo lectura que sale de vistas SQL.
 * No tiene botones de edición.
 *
 * Solo diseño: expone sus tablas y filtros para que ConsultasController les conecte
 * los eventos y llene los datos.
 */
public class PnlConsultas extends PnlSeccion {

    private static final int COL_VALOR_ANTERIOR = 5;
    private static final int COL_VALOR_NUEVO = 6;

    private final PnlTabla tblHistorial = new PnlTabla(
            "Fecha", "Orden", "Vehículo", "Servicios", "Mecánicos", "Repuestos", "Costo total");

    private final PnlTabla tblBajoMinimo = new PnlTabla(
            "Código", "Repuesto", "Categoría", "Stock", "Mínimo", "Faltante", "Proveedor");

    private final PnlTabla tblSaldos = new PnlTabla(
            "Orden", "Cliente", "Vehículo", "Total", "Pagado", "Saldo", "Estado");

    private final PnlTabla tblAuditoria = new PnlTabla(
            "Fecha", "Usuario", "Tabla", "Registro", "Acción", "Valor anterior", "Valor nuevo");

    private JComboBox<String> cmbCategoria;
    private JCheckBox chkSoloPendientes;
    private JComboBox<String> cmbTablaAuditoria;
    private JComboBox<String> cmbAccionAuditoria;

    private final JTabbedPane tabs;

    public PnlConsultas() {
        super("Consultas",
                "Información de solo lectura sobre el taller");

        configurarHistorial();
        configurarBajoMinimo();
        configurarSaldos();
        configurarAuditoria();

        tabs = crearPestanas();
        agregarPestana(tabs, "Historial vehicular", tblHistorial);
        agregarPestana(tabs, "Repuestos bajo mínimo", tblBajoMinimo);
        agregarPestana(tabs, "Saldos por orden", tblSaldos);
        agregarPestana(tabs, "Auditoría", tblAuditoria);
    }

    private void configurarHistorial() {
        tblHistorial.setPlaceholder("Buscar por placa, vehículo, servicio o mecánico...");
        tblHistorial.setAnchos(130, 100, 200, 200, 160, 180, 110);
        tblHistorial.alinearDerecha(6);
    }

    private void configurarBajoMinimo() {
        tblBajoMinimo.setPlaceholder("Buscar por código, repuesto o proveedor...");
        String[] categorias = new String[Repuesto.CATEGORIAS.length + 1];
        categorias[0] = "Todas";
        System.arraycopy(Repuesto.CATEGORIAS, 0, categorias, 1, Repuesto.CATEGORIAS.length);
        cmbCategoria = tblBajoMinimo.addFiltro("Categoría", categorias);
        tblBajoMinimo.setAnchos(110, 220, 110, 70, 70, 80, 160);
        tblBajoMinimo.alinearDerecha(3, 4, 5);
    }

    private void configurarSaldos() {
        tblSaldos.setPlaceholder("Buscar por orden, cliente o placa...");
        chkSoloPendientes = tblSaldos.addFiltroCheck("Solo con saldo pendiente");
        tblSaldos.setAnchos(100, 190, 190, 100, 100, 100, 150);
        tblSaldos.alinearDerecha(3, 4, 5);
    }

    private void configurarAuditoria() {
        tblAuditoria.setPlaceholder("Buscar por usuario, registro o valor...");
        cmbTablaAuditoria = tblAuditoria.addFiltro("Tabla", "Todas", "Órdenes", "Repuestos", "Pagos", "Usuarios");
        cmbAccionAuditoria = tblAuditoria.addFiltro("Acción", "Todas", "Alta", "Modificación", "Eliminación");
        tblAuditoria.setAnchos(140, 110, 100, 80, 110, 260, 260);
        tblAuditoria.alinearDerecha(3);

        tblAuditoria.addMenuContextual("Ver detalle del cambio").addActionListener(e -> verDetalleCambio());
    }

    /** Clic derecho en Auditoría: muestra el valor anterior y el nuevo con un campo por línea. */
    private void verDetalleCambio() {
        Object anterior = tblAuditoria.getValorSeleccionado(COL_VALOR_ANTERIOR);
        Object nuevo = tblAuditoria.getValorSeleccionado(COL_VALOR_NUEVO);
        if (anterior == null && nuevo == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro de la tabla.",
                    "Detalle del cambio", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.add(columnaTexto("Valor anterior", anterior));
        panel.add(columnaTexto("Valor nuevo", nuevo));
        panel.setPreferredSize(new Dimension(720, 380));
        JOptionPane.showMessageDialog(this, panel, "Detalle del cambio", JOptionPane.PLAIN_MESSAGE);
    }

    private static JScrollPane columnaTexto(String titulo, Object valor) {
        JTextArea area = new JTextArea(Json.ordenar(valor == null ? null : valor.toString()));
        area.setEditable(false);
        area.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder(titulo));
        return scroll;
    }

    // ================================================================= acceso para el controlador

    public JTabbedPane getTabs() { return tabs; }

    public PnlTabla getTblHistorial() { return tblHistorial; }
    public PnlTabla getTblBajoMinimo() { return tblBajoMinimo; }
    public PnlTabla getTblSaldos() { return tblSaldos; }
    public PnlTabla getTblAuditoria() { return tblAuditoria; }

    public JComboBox<String> getCmbCategoria() { return cmbCategoria; }
    public JCheckBox getChkSoloPendientes() { return chkSoloPendientes; }
    public JComboBox<String> getCmbTablaAuditoria() { return cmbTablaAuditoria; }
    public JComboBox<String> getCmbAccionAuditoria() { return cmbAccionAuditoria; }
}
