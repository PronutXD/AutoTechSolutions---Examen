package views.formularios;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * Base de todos los formularios (Nuevo / Editar). Dibuja el encabezado, la
 * columna de campos y los botones Cancelar / Guardar; cada formulario concreto
 * solo agrega sus campos y dice cómo validarlos y cómo volcarlos al modelo.
 *
 * La ventana NO toca la base de datos: el controlador abre el formulario, y si
 * isAceptado() es true toma el objeto ya llenado y se lo entrega al DAO.
 */
public abstract class DlgFormulario extends JDialog {

    private static final Color TEXTO = new Color(0x1E1E1E);
    private static final Color GRIS = new Color(0x6B6B6B);
    private static final Color ROJO = new Color(0xD32F2F);
    private static final String STYLE = "FlatLaf.style";

    private final JPanel campos = new JPanel(new GridBagLayout());
    private final JLabel lblError = new JLabel(" ");
    private final JButton btnGuardar = new JButton("Guardar");
    private int fila = 0;
    private boolean aceptado = false;

    protected DlgFormulario(Frame propietario, String titulo, String subtitulo) {
        super(propietario, titulo, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        raiz.add(crearEncabezado(titulo, subtitulo), BorderLayout.NORTH);
        campos.setOpaque(false);
        raiz.add(campos, BorderLayout.CENTER);
        raiz.add(crearPie(), BorderLayout.SOUTH);
        setContentPane(raiz);

        getRootPane().setDefaultButton(btnGuardar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ================================================================= para los formularios concretos

    /** Texto de validación; null si todo está bien. */
    protected abstract String validar();

    /** Copia lo escrito en los campos al objeto del modelo (solo se llama si validar() pasó). */
    protected abstract void aplicar();

    protected final void agregarCampo(String etiqueta, JComponent campo, boolean obligatorio) {
        JLabel lbl = new JLabel(obligatorio ? etiqueta + " *" : etiqueta);
        lbl.setForeground(GRIS);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 12, 16);
        campos.add(lbl, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = fila;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 12, 0);
        campos.add(campo, c);
        fila++;
    }

    /** Campo de texto con el ancho común de los formularios. */
    protected static JTextField campoTexto() {
        return new JTextField(24);
    }

    /** Llamar al final del constructor del formulario concreto, ya con todos los campos puestos. */
    protected final void armar() {
        pack();
        setMinimumSize(getSize());
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    public boolean isAceptado() {
        return aceptado;
    }

    // ================================================================= construcción

    private JPanel crearEncabezado(String titulo, String subtitulo) {
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 20f));
        lblTitulo.setForeground(TEXTO);

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(lblSub.getFont().deriveFont(12f));
        lblSub.setForeground(GRIS);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblTitulo);
        p.add(Box.createVerticalStrut(4));
        p.add(lblSub);
        return p;
    }

    private JPanel crearPie() {
        lblError.setForeground(ROJO);
        lblError.setFont(lblError.getFont().deriveFont(12f));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFocusPainted(false);
        btnCancelar.setMargin(new Insets(7, 18, 7, 18));
        btnCancelar.addActionListener(e -> dispose());

        btnGuardar.setFocusPainted(false);
        btnGuardar.setMargin(new Insets(7, 22, 7, 22));
        btnGuardar.setFont(btnGuardar.getFont().deriveFont(Font.BOLD));
        btnGuardar.putClientProperty(STYLE,
                "arc: 12; background: #E45E16; foreground: #FFFFFF; "
                + "hoverBackground: #C84F0F; pressedBackground: #A84008; "
                + "borderWidth: 0; focusWidth: 0");
        btnGuardar.addActionListener(this::alGuardar);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        pie.add(lblError, BorderLayout.NORTH);
        pie.add(botones, BorderLayout.SOUTH);
        return pie;
    }

    private void alGuardar(ActionEvent e) {
        String problema = validar();
        if (problema != null) {
            lblError.setText(problema);
            return;
        }
        aplicar();
        aceptado = true;
        dispose();
    }
}
