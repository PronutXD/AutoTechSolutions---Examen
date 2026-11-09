package views.formularios.inventario;

import java.awt.*;
import java.math.BigDecimal;
import java.time.Year;
import javax.swing.*;
import model.Vehiculo;
import model.inventario.Motor;
import views.componentes.Numeros;
import views.componentes.PnlTabla;

/**
 * Búsqueda de motores compatibles: criterios arriba y resultados abajo. Cualquier criterio
 * se puede dejar vacío. Como los demás diálogos, no toca la base de datos: el controlador
 * conecta el botón Buscar (getBtnBuscar), lee los criterios y llena la tabla de resultados.
 */
public class DlgMotoresCompatibles extends JDialog {

    private static final Color TEXTO = new Color(0x1E1E1E);
    private static final Color GRIS = new Color(0x6B6B6B);
    private static final Color ROJO = new Color(0xD32F2F);
    private static final String STYLE = "FlatLaf.style";

    private final JTextField txtMarca = new JTextField(10);
    private final JTextField txtAnio = new JTextField(5);
    private final JTextField txtCilindraje = new JTextField(5);
    private final JComboBox<String> cmbCombustible;
    private final JButton btnBuscar = new JButton("Buscar");
    private final JLabel lblError = new JLabel(" ");
    private final PnlTabla tblResultados = new PnlTabla(
            "Motor (nombre / serie)", "Marca", "Modelo compatible", "Años", "Cilindraje", "Combustible", "Precio venta");

    /** @param base motor seleccionado: sus datos se proponen como criterios (puede ser null) */
    public DlgMotoresCompatibles(Frame propietario, Motor base) {
        super(propietario, "Motores compatibles", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String[] combustibles = new String[Vehiculo.COMBUSTIBLES.length + 1];
        combustibles[0] = "Todos";
        System.arraycopy(Vehiculo.COMBUSTIBLES, 0, combustibles, 1, Vehiculo.COMBUSTIBLES.length);
        cmbCombustible = new JComboBox<>(combustibles);

        tblResultados.setAnchos(230, 100, 160, 90, 90, 100, 100);
        tblResultados.alinearDerecha(4, 6);
        tblResultados.setPlaceholder("Filtrar los resultados...");

        if (base != null) {
            txtMarca.setText(base.getMarca());
            txtAnio.setText(String.valueOf(base.getAnioDesde()));
            txtCilindraje.setText(base.getCilindraje() == null ? "" : base.getCilindraje().toPlainString());
            cmbCombustible.setSelectedItem(base.getCombustible());
        }

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(tblResultados, BorderLayout.CENTER);
        raiz.add(crearPie(), BorderLayout.SOUTH);
        setContentPane(raiz);

        getRootPane().setDefaultButton(btnBuscar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setSize(860, 560);
        setMinimumSize(new Dimension(760, 460));
        setLocationRelativeTo(propietario);
    }

    // ================================================================= para el controlador

    public JButton getBtnBuscar() { return btnBuscar; }

    public PnlTabla getTblResultados() { return tblResultados; }

    /** Texto de validación de los criterios; null si están bien. */
    public String validarCriterios() {
        String anio = txtAnio.getText().trim();
        if (!anio.isEmpty()) {
            Integer a = Numeros.entero(anio);
            if (a == null || a < 1950 || a > Year.now().getValue() + 1) {
                return "El año debe ser un número entre 1950 y " + (Year.now().getValue() + 1) + ".";
            }
        }
        String cil = txtCilindraje.getText().trim();
        if (!cil.isEmpty() && Numeros.decimal(cil, 1) == null) {
            return "El cilindraje debe ser un número con un decimal (por ejemplo 1.8).";
        }
        return null;
    }

    public void mostrarError(String texto) {
        lblError.setText(texto == null || texto.isEmpty() ? " " : texto);
    }

    /** null = sin filtrar por marca. */
    public String getMarca() {
        String m = txtMarca.getText().trim();
        return m.isEmpty() ? null : m;
    }

    /** null = sin filtrar por año (llamar solo si validarCriterios() devolvió null). */
    public Integer getAnio() {
        return txtAnio.getText().isBlank() ? null : Numeros.entero(txtAnio.getText());
    }

    /** null = sin filtrar por cilindraje (llamar solo si validarCriterios() devolvió null). */
    public BigDecimal getCilindraje() {
        return txtCilindraje.getText().isBlank() ? null : Numeros.decimal(txtCilindraje.getText(), 1);
    }

    /** null = todos. */
    public String getCombustible() {
        return cmbCombustible.getSelectedIndex() <= 0 ? null : (String) cmbCombustible.getSelectedItem();
    }

    // ================================================================= construcción

    private JPanel crearEncabezado() {
        JLabel lblTitulo = new JLabel("Motores compatibles");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 20f));
        lblTitulo.setForeground(TEXTO);

        JLabel lblSub = new JLabel("Solo motores disponibles. Deja vacío lo que no quieras filtrar.");
        lblSub.setFont(lblSub.getFont().deriveFont(12f));
        lblSub.setForeground(GRIS);

        btnBuscar.setFocusPainted(false);
        btnBuscar.setMargin(new Insets(7, 22, 7, 22));
        btnBuscar.setFont(btnBuscar.getFont().deriveFont(Font.BOLD));
        btnBuscar.putClientProperty(STYLE,
                "arc: 12; background: #E45E16; foreground: #FFFFFF; "
                + "hoverBackground: #C84F0F; pressedBackground: #A84008; "
                + "borderWidth: 0; focusWidth: 0");

        JPanel criterios = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        criterios.setOpaque(false);
        agregarCriterio(criterios, "Marca", txtMarca);
        agregarCriterio(criterios, "Año", txtAnio);
        agregarCriterio(criterios, "Cilindraje (L)", txtCilindraje);
        agregarCriterio(criterios, "Combustible", cmbCombustible);
        criterios.add(btnBuscar);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        criterios.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblTitulo);
        p.add(Box.createVerticalStrut(4));
        p.add(lblSub);
        p.add(Box.createVerticalStrut(14));
        p.add(criterios);
        return p;
    }

    private static void agregarCriterio(JPanel panel, String etiqueta, JComponent campo) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setForeground(GRIS);
        panel.add(lbl);
        panel.add(Box.createHorizontalStrut(6));
        panel.add(campo);
        panel.add(Box.createHorizontalStrut(18));
    }

    private JPanel crearPie() {
        lblError.setForeground(ROJO);
        lblError.setFont(lblError.getFont().deriveFont(12f));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFocusPainted(false);
        btnCerrar.setMargin(new Insets(7, 18, 7, 18));
        btnCerrar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnCerrar);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        pie.add(lblError, BorderLayout.NORTH);
        pie.add(botones, BorderLayout.SOUTH);
        return pie;
    }
}
