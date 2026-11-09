package views.formularios.ordenes;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import model.ordenes.Orden;
import model.ordenes.SaldoOrden;
import views.componentes.Formato;
import views.componentes.PnlTabla;

/**
 * Detalle de una orden: ventana con pestañas (Resumen, Servicios y repuestos, Pagos y
 * Control de calidad). Es solo vista: DetalleOrdenController le entrega los datos y
 * conecta sus botones.
 */
public class DlgOrden extends JDialog {

    private static final Color TEXTO = new Color(0x1E1E1E);
    private static final Color GRIS = new Color(0x6B6B6B);
    private static final Color LINEA = new Color(0xE8E7E2);
    private static final Color FONDO = new Color(0xFFFEF8);
    private static final String STYLE = "FlatLaf.style";

    private final JLabel lblTitulo = new JLabel(" ");
    private final JLabel lblEstado = new JLabel(" ");
    private final JLabel lblSubtitulo = new JLabel(" ");

    // Resumen: datos
    private final JLabel valCliente = valor();
    private final JLabel valVehiculo = valor();
    private final JLabel valIngreso = valor();
    private final JLabel valKilometraje = valor();
    private final JLabel valMotivo = valor();
    private final JLabel valMecanicos = valor();
    private final JLabel valEntrega = valor();
    // Resumen: totales
    private final JLabel valSubtotal = valor();
    private final JLabel valDescuento = valor();
    private final JLabel valImpuesto = valor();
    private final JLabel valTotal = valor();
    private final JLabel valPagado = valor();
    private final JLabel valSaldo = valor();

    private final JComboBox<String> cmbNuevoEstado = new JComboBox<>();
    private final JButton btnCambiarEstado = new JButton("Aplicar");
    private final JButton btnFinalizar = new JButton("Finalizar y entregar");

    private final PnlTabla tblLineas = new PnlTabla(
            "Tipo", "Descripción", "Cantidad", "Precio unitario", "Subtotal");
    private final JButton btnAgregarServicio;
    private final JButton btnAgregarRepuesto;

    private final PnlTabla tblPagos = new PnlTabla(
            "Código", "Fecha", "Monto", "Método", "Referencia", "Registrado por");
    private final JButton btnRegistrarPago;

    private final PnlTabla tblCalidad = new PnlTabla(
            "Código", "Fecha", "Inspector", "Resultado", "Prueba de ruta", "Observaciones");
    private final JButton btnRegistrarControl;

    public DlgOrden(Frame propietario) {
        super(propietario, "Orden de trabajo", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        tblLineas.setPlaceholder("Buscar servicio o repuesto...");
        btnAgregarRepuesto = tblLineas.addAccion("Agregar repuesto", false, false);
        btnAgregarServicio = tblLineas.addAccion("Agregar servicio", true, false);
        tblLineas.setAnchos(90, 320, 80, 120, 120);
        tblLineas.alinearDerecha(2, 3, 4);

        tblPagos.setPlaceholder("Buscar pago...");
        btnRegistrarPago = tblPagos.addAccion("Registrar pago", true, false);
        tblPagos.setAnchos(120, 140, 100, 110, 160, 130);
        tblPagos.alinearDerecha(2);

        tblCalidad.setPlaceholder("Buscar control...");
        btnRegistrarControl = tblCalidad.addAccion("Registrar control", true, false);
        tblCalidad.setAnchos(120, 140, 170, 100, 110, 260);

        JPanel raiz = new JPanel(new BorderLayout(0, 14));
        raiz.setBackground(FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(22, 28, 20, 28));
        raiz.add(crearEncabezado(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.putClientProperty("JTabbedPane.tabType", "underlined");
        tabs.setBackground(FONDO);
        tabs.addTab("Resumen", envolver(crearResumen()));
        tabs.addTab("Servicios y repuestos", envolver(tblLineas));
        tabs.addTab("Pagos", envolver(tblPagos));
        tabs.addTab("Control de calidad", envolver(tblCalidad));
        raiz.add(tabs, BorderLayout.CENTER);

        setContentPane(raiz);
        setSize(1000, 660);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(propietario);
    }

    // ================================================================= datos que recibe del controlador

    public void setCabecera(String codigo, String estado, String subtitulo) {
        setTitle("Orden " + codigo);
        lblTitulo.setText("Orden " + codigo);
        lblEstado.setText(estado);
        lblSubtitulo.setText(subtitulo);
    }

    public void mostrarDatos(Orden o, List<String> mecanicos) {
        valCliente.setText(o.getCliente());
        valVehiculo.setText(o.getVehiculo());
        valIngreso.setText(Formato.fechaHora(o.getFechaIngreso()));
        valKilometraje.setText(String.format("%,d km", o.getKilometrajeIngreso()));
        valMotivo.setText(o.getMotivo() == null ? "—" : o.getMotivo());
        valMecanicos.setText(mecanicos.isEmpty() ? "—" : String.join(", ", mecanicos));
        valEntrega.setText(o.getFechaEntrega() == null ? "—" : Formato.fechaHora(o.getFechaEntrega()));
    }

    public void mostrarSaldo(SaldoOrden s) {
        valSubtotal.setText(Formato.dinero(s.subtotal()));
        valDescuento.setText(Formato.dinero(s.descuento()));
        valImpuesto.setText(Formato.dinero(s.impuesto()));
        valTotal.setText(Formato.dinero(s.total()));
        valPagado.setText(Formato.dinero(s.pagado()));
        valSaldo.setText(Formato.dinero(s.saldo()));
    }

    public void setEstadosSiguientes(List<String> estados) {
        cmbNuevoEstado.removeAllItems();
        for (String e : estados) {
            cmbNuevoEstado.addItem(e);
        }
        boolean hay = !estados.isEmpty();
        cmbNuevoEstado.setEnabled(hay);
        btnCambiarEstado.setEnabled(hay);
    }

    public void setPuedeFinalizar(boolean puede) {
        btnFinalizar.setEnabled(puede);
    }

    public void setPuedeAgregarLineas(boolean puede) {
        btnAgregarServicio.setEnabled(puede);
        btnAgregarRepuesto.setEnabled(puede);
    }

    public void setPuedePagar(boolean puede) {
        btnRegistrarPago.setEnabled(puede);
    }

    public void setPuedeRegistrarControl(boolean puede) {
        btnRegistrarControl.setEnabled(puede);
    }

    // ================================================================= acceso para el controlador

    public String getNuevoEstado() {
        return (String) cmbNuevoEstado.getSelectedItem();
    }

    public JButton getBtnCambiarEstado() { return btnCambiarEstado; }
    public JButton getBtnFinalizar() { return btnFinalizar; }
    public PnlTabla getTblLineas() { return tblLineas; }
    public JButton getBtnAgregarServicio() { return btnAgregarServicio; }
    public JButton getBtnAgregarRepuesto() { return btnAgregarRepuesto; }
    public PnlTabla getTblPagos() { return tblPagos; }
    public JButton getBtnRegistrarPago() { return btnRegistrarPago; }
    public PnlTabla getTblCalidad() { return tblCalidad; }
    public JButton getBtnRegistrarControl() { return btnRegistrarControl; }

    // ================================================================= construcción

    private JPanel crearEncabezado() {
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 22f));
        lblTitulo.setForeground(TEXTO);
        lblSubtitulo.setFont(lblSubtitulo.getFont().deriveFont(13f));
        lblSubtitulo.setForeground(GRIS);

        lblEstado.setOpaque(true);
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setBackground(new Color(0xE45E16));
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 12f));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        lblEstado.putClientProperty(STYLE, "arc: 999");

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(lblSubtitulo);

        JPanel estado = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        estado.setOpaque(false);
        estado.add(lblEstado);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINEA));
        p.add(textos, BorderLayout.WEST);
        p.add(estado, BorderLayout.EAST);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINEA),
                BorderFactory.createEmptyBorder(0, 0, 14, 0)));
        return p;
    }

    private JPanel crearResumen() {
        JPanel datos = bloque("Datos de la orden",
                new String[]{"Cliente", "Vehículo", "Ingreso", "Kilometraje", "Motivo", "Mecánicos", "Entrega"},
                new JLabel[]{valCliente, valVehiculo, valIngreso, valKilometraje, valMotivo, valMecanicos, valEntrega});
        JPanel totales = bloque("Totales",
                new String[]{"Subtotal", "Descuento", "Impuesto", "Total", "Pagado", "Saldo"},
                new JLabel[]{valSubtotal, valDescuento, valImpuesto, valTotal, valPagado, valSaldo});
        valTotal.setFont(valTotal.getFont().deriveFont(Font.BOLD));
        valSaldo.setFont(valSaldo.getFont().deriveFont(Font.BOLD));

        JPanel arriba = new JPanel(new GridLayout(1, 2, 32, 0));
        arriba.setOpaque(false);
        arriba.add(datos);
        arriba.add(totales);

        btnCambiarEstado.setFocusPainted(false);
        btnFinalizar.setFocusPainted(false);
        btnFinalizar.setFont(btnFinalizar.getFont().deriveFont(Font.BOLD));
        btnFinalizar.putClientProperty(STYLE,
                "arc: 12; background: #E45E16; foreground: #FFFFFF; "
                + "hoverBackground: #C84F0F; pressedBackground: #A84008; borderWidth: 0; focusWidth: 0");
        btnFinalizar.setMargin(new Insets(7, 16, 7, 16));

        JLabel lblPasar = new JLabel("Pasar a:");
        lblPasar.setForeground(GRIS);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(lblPasar);
        acciones.add(cmbNuevoEstado);
        acciones.add(btnCambiarEstado);
        acciones.add(Box.createHorizontalStrut(24));
        acciones.add(btnFinalizar);

        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setOpaque(false);
        p.add(arriba, BorderLayout.NORTH);
        p.add(acciones, BorderLayout.CENTER);
        return p;
    }

    private JPanel bloque(String titulo, String[] etiquetas, JLabel[] valores) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 10, 0);
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 15f));
        p.add(lbl, c);
        for (int i = 0; i < etiquetas.length; i++) {
            GridBagConstraints a = new GridBagConstraints();
            a.gridx = 0;
            a.gridy = i + 1;
            a.anchor = GridBagConstraints.NORTHWEST;
            a.insets = new Insets(0, 0, 8, 16);
            JLabel e = new JLabel(etiquetas[i]);
            e.setForeground(GRIS);
            p.add(e, a);

            GridBagConstraints b = new GridBagConstraints();
            b.gridx = 1;
            b.gridy = i + 1;
            b.weightx = 1;
            b.fill = GridBagConstraints.HORIZONTAL;
            b.anchor = GridBagConstraints.NORTHWEST;
            b.insets = new Insets(0, 0, 8, 0);
            p.add(valores[i], b);
        }
        // empuja todo hacia arriba
        GridBagConstraints relleno = new GridBagConstraints();
        relleno.gridx = 0;
        relleno.gridy = etiquetas.length + 1;
        relleno.weighty = 1;
        p.add(Box.createGlue(), relleno);
        return p;
    }

    private static JLabel valor() {
        JLabel l = new JLabel(" ");
        l.setForeground(TEXTO);
        return l;
    }

    private static JPanel envolver(JComponent c) {
        JPanel marco = new JPanel(new BorderLayout());
        marco.setOpaque(true);
        marco.setBackground(FONDO);
        marco.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        marco.add(c, BorderLayout.CENTER);
        return marco;
    }
}
