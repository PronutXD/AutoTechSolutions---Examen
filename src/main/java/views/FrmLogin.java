package views;

import controller.PrincipalController;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Pantalla de inicio de sesión de AutoTechSolutions (FlatLaf).
 *
 * Clase Java normal, sin .form: todo el diseño está en el código.
 * Imágenes esperadas en: src/main/resources/assets/img/
 *   - logo_completo_blanco.svg  (logo principal, vectorial)
 *   - logo_completo_blanco.png  (respaldo si falta el SVG)
 *   - app_icono_16.png, app_icono_32.png, app_icono_64.png, app_icono_256.png
 * Requiere las dependencias flatlaf y flatlaf-extras (misma versión).
 * Si alguna imagen falta, la ventana igual abre (con texto en lugar del logo).
 */
public class FrmLogin extends JFrame {

    // ----------------------------------------------------------- paleta
    private static final Color NARANJA = new Color(0xE45E16);
    private static final Color NARANJA_OSCURO = new Color(0xB8480E);
    private static final Color TEXTO = new Color(0x1E1E1E);
    private static final Color GRIS = new Color(0x6B6B6B);
    private static final Color GRIS_ICONO = new Color(0x8C8C8C);
    private static final Color ROJO = new Color(0xD32F2F);
    private static final Color VERDE = new Color(0x2E7D32);
    private static final Color FONDO = new Color(0xFFFEF8);

    private static final String STYLE = "FlatLaf.style";
    private static final String RUTA_IMG = "/assets/img/";   // para PNG (getResourceAsStream)
    private static final String RUTA_SVG = "assets/img/";    // para FlatSVGIcon (sin "/" inicial)

    // ----------------------------------------------------------- componentes
    private JTextField txtUsuario;
    private JPasswordField txtClave;
    private JCheckBox chkRecordar;
    private JButton btnIngresar;
    private JLabel lblMensaje;

    public FrmLogin() {
        initUI();
    }

    // ================================================================= UI

    private void initUI() {
        setTitle("AutoTechSolutions - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setIconImages(cargarIconos());

        setLayout(new BorderLayout());
        add(crearPanelMarca(), BorderLayout.WEST);
        add(crearPanelFormulario(), BorderLayout.CENTER);

        getRootPane().setDefaultButton(btnIngresar);
        pack();
        setLocationRelativeTo(null);
    }

    /** Panel izquierdo: degradado naranja con el logo en blanco. */
    private JPanel crearPanelMarca() {
        PanelMarca panel = new PanelMarca();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(400, 580));

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;

        // Logo vectorial (SVG): se redibuja nítido con cualquier escala de pantalla.
        // Si el SVG no se encuentra, usa el PNG y, en último caso, el texto.
        JLabel logo = new JLabel();
        FlatSVGIcon svg = new FlatSVGIcon(RUTA_SVG + "logo_completo_blanco.svg").derive(330, 138);
        BufferedImage img = cargarImagen("logo_completo_blanco.png");
        if (svg.hasFound()) {
            logo.setIcon(svg);
        } else if (img != null) {
            logo.setIcon(escalar(img, 330));
        } else {
            logo.setText("AUTOTECHSOLUTIONS");
            logo.setForeground(Color.WHITE);
            logo.setFont(logo.getFont().deriveFont(Font.BOLD, 24f));
        }
        c.gridy = 0;
        centro.add(logo, c);

        JLabel lema = new JLabel("<html><div style='text-align:center'>Todo tu negocio,<br>en un solo lugar.</div></html>",
                SwingConstants.CENTER);
        lema.setForeground(Color.WHITE);
        lema.setFont(lema.getFont().deriveFont(Font.BOLD, 20f));
        c.gridy = 1;
        c.insets = new Insets(26, 0, 0, 0);
        centro.add(lema, c);

        JLabel sub = new JLabel("Gestión rápida, clara y segura.", SwingConstants.CENTER);
        sub.setForeground(new Color(255, 255, 255, 215));
        sub.setFont(sub.getFont().deriveFont(13f));
        c.gridy = 2;
        c.insets = new Insets(8, 0, 0, 0);
        centro.add(sub, c);

        JLabel pie = new JLabel("© 2026 AutoTechSolutions", SwingConstants.CENTER);
        pie.setForeground(new Color(255, 255, 255, 190));
        pie.setFont(pie.getFont().deriveFont(11f));
        pie.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        panel.add(centro, BorderLayout.CENTER);
        panel.add(pie, BorderLayout.SOUTH);
        return panel;
    }

    /** Panel derecho: formulario de acceso. */
    private JPanel crearPanelFormulario() {
        JPanel lado = new JPanel(new GridBagLayout());
        lado.setBackground(FONDO);
        lado.setPreferredSize(new Dimension(520, 580));

        JPanel f = new JPanel(new GridBagLayout());
        f.setOpaque(false);
        f.setPreferredSize(new Dimension(340, 420));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        int fila = 0;

        // Titulo
        JLabel titulo = new JLabel("Bienvenido");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 30f));
        titulo.setForeground(TEXTO);
        c.gridy = fila++;
        f.add(titulo, c);

        JLabel subtitulo = new JLabel("Ingresa tus credenciales para continuar");
        subtitulo.setForeground(GRIS);
        subtitulo.setFont(subtitulo.getFont().deriveFont(13f));
        c.gridy = fila++;
        c.insets = new Insets(4, 0, 26, 0);
        f.add(subtitulo, c);

        // Usuario
        c.gridy = fila++;
        c.insets = new Insets(0, 0, 6, 0);
        f.add(etiqueta("Usuario"), c);

        txtUsuario = new JTextField();
        estilizarCampo(txtUsuario, "Nombre de usuario", new IconoCampo(IconoCampo.USUARIO));
        c.gridy = fila++;
        c.insets = new Insets(0, 0, 16, 0);
        f.add(txtUsuario, c);

        // Contrasena
        c.gridy = fila++;
        c.insets = new Insets(0, 0, 6, 0);
        f.add(etiqueta("Contraseña"), c);

        txtClave = new JPasswordField();
        estilizarCampo(txtClave, "Tu contraseña", new IconoCampo(IconoCampo.CANDADO));
        txtClave.putClientProperty("JPasswordField.showRevealButton", Boolean.TRUE);
        c.gridy = fila++;
        c.insets = new Insets(0, 0, 14, 0);
        f.add(txtClave, c);

        // Recordarme + olvide
        JPanel opciones = new JPanel(new BorderLayout());
        opciones.setOpaque(false);
        chkRecordar = new JCheckBox("Recordarme");
        chkRecordar.setOpaque(false);
        chkRecordar.setForeground(GRIS);
        opciones.add(chkRecordar, BorderLayout.WEST);
        opciones.add(crearEnlaceOlvido(), BorderLayout.EAST);
        c.gridy = fila++;
        c.insets = new Insets(0, 0, 10, 0);
        f.add(opciones, c);

        // Mensaje (error / exito)
        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(lblMensaje.getFont().deriveFont(12f));
        c.gridy = fila++;
        c.insets = new Insets(0, 0, 10, 0);
        f.add(lblMensaje, c);

        // Boton
        btnIngresar = new JButton("Iniciar sesión");
        btnIngresar.setFont(btnIngresar.getFont().deriveFont(Font.BOLD, 14f));
        btnIngresar.setMargin(new Insets(12, 16, 12, 16));
        btnIngresar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIngresar.setFocusPainted(false);
        btnIngresar.putClientProperty(STYLE,
                "arc: 12; background: #E45E16; foreground: #FFFFFF; "
                + "hoverBackground: #C84F0F; pressedBackground: #A84008; "
                + "borderWidth: 0; focusWidth: 0");
        btnIngresar.addActionListener(e -> iniciarSesion());
        c.gridy = fila++;
        c.insets = new Insets(0, 0, 0, 0);
        f.add(btnIngresar, c);

        // Relleno para empujar todo hacia arriba
        c.gridy = fila++;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        f.add(new JLabel(), c);

        // Pie
        JLabel ayuda = new JLabel("¿Necesitas ayuda? Contacta al administrador.", SwingConstants.CENTER);
        ayuda.setForeground(GRIS);
        ayuda.setFont(ayuda.getFont().deriveFont(12f));
        c.gridy = fila++;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        f.add(ayuda, c);

        lado.add(f);

        // Limpia el estado de error al escribir
        DocumentListener limpiar = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { limpiarErrores(); }
            @Override public void removeUpdate(DocumentEvent e) { limpiarErrores(); }
            @Override public void changedUpdate(DocumentEvent e) { limpiarErrores(); }
        };
        txtUsuario.getDocument().addDocumentListener(limpiar);
        txtClave.getDocument().addDocumentListener(limpiar);

        return lado;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(TEXTO);
        return l;
    }

    private void estilizarCampo(JTextField campo, String placeholder, Icon icono) {
        campo.setMargin(new Insets(11, 10, 11, 10));
        campo.putClientProperty("JTextField.placeholderText", placeholder);
        campo.putClientProperty("JTextField.leadingIcon", icono);
        campo.putClientProperty(STYLE, "arc: 12; focusWidth: 2");
    }

    private JLabel crearEnlaceOlvido() {
        final String normal = "¿Olvidaste tu contraseña?";
        JLabel enlace = new JLabel(normal);
        enlace.setForeground(NARANJA);
        enlace.setFont(enlace.getFont().deriveFont(12f));
        enlace.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        enlace.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                enlace.setText("<html><u>" + normal + "</u></html>");
            }
            @Override public void mouseExited(MouseEvent e) {
                enlace.setText(normal);
            }
            @Override public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(FrmLogin.this,
                        "Contacta al administrador para restablecer tu contraseña.",
                        "Recuperar contraseña", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        return enlace;
    }

    // ================================================================= lógica de la vista

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        char[] clave = txtClave.getPassword();
        boolean ok = true;

        if (usuario.isEmpty()) {
            txtUsuario.putClientProperty("JComponent.outline", "error");
            ok = false;
        }
        if (clave.length == 0) {
            txtClave.putClientProperty("JComponent.outline", "error");
            ok = false;
        }
        if (!ok) {
            mostrarMensaje("Ingresa tu usuario y contraseña.", true);
            return;
        }

        if (autenticar(usuario, clave)) {
            java.util.Arrays.fill(clave, '\0');
            new PrincipalController(new FrmPrincipal(), usuario).iniciar();
            dispose();
            return;
        } else {
            txtUsuario.putClientProperty("JComponent.outline", "error");
            txtClave.putClientProperty("JComponent.outline", "error");
            mostrarMensaje("Usuario o contraseña incorrectos.", true);
        }
        java.util.Arrays.fill(clave, '\0');
    }

    /** DEMO: reemplazar por la validación real (DAO). Usuario: admin / Clave: 1234 */
    private boolean autenticar(String usuario, char[] clave) {
        return "admin".equals(usuario) && "1234".equals(new String(clave));
    }

    private void mostrarMensaje(String texto, boolean error) {
        lblMensaje.setForeground(error ? ROJO : VERDE);
        lblMensaje.setText(texto);
    }

    private void limpiarErrores() {
        txtUsuario.putClientProperty("JComponent.outline", null);
        txtClave.putClientProperty("JComponent.outline", null);
        lblMensaje.setText(" ");
    }

    // ================================================================= imágenes

    static BufferedImage cargarImagen(String nombre) {
        try (InputStream in = FrmLogin.class.getResourceAsStream(RUTA_IMG + nombre)) {
            return in == null ? null : ImageIO.read(in);
        } catch (IOException ex) {
            return null;
        }
    }

    static List<Image> cargarIconos() {
        List<Image> lista = new ArrayList<>();
        for (String n : new String[]{"app_icono_16.png", "app_icono_32.png",
            "app_icono_64.png", "app_icono_256.png"}) {
            BufferedImage img = cargarImagen(n);
            if (img != null) {
                lista.add(img);
            }
        }
        return lista;
    }

    /** Reduce con calidad (mitades sucesivas + bicúbico) para evitar bordes dentados. */
    static ImageIcon escalar(BufferedImage src, int ancho) {
        BufferedImage actual = src;
        while (actual.getWidth() / 2 >= ancho) {
            actual = redimensionar(actual, actual.getWidth() / 2);
        }
        return new ImageIcon(redimensionar(actual, ancho));
    }

    private static BufferedImage redimensionar(BufferedImage src, int ancho) {
        int alto = Math.round(src.getHeight() * (ancho / (float) src.getWidth()));
        BufferedImage dst = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, ancho, alto, null);
        g.dispose();
        return dst;
    }

    // ================================================================= clases auxiliares

    /** Fondo con degradado naranja y círculos decorativos translúcidos. */
    private static class PanelMarca extends JPanel {

        PanelMarca() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, NARANJA, w, h, NARANJA_OSCURO));
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, 24));
            g2.fillOval(-130, -130, 330, 330);
            g2.fillOval(w - 190, h - 210, 400, 400);
            g2.setColor(new Color(255, 255, 255, 14));
            g2.fillOval(w - 110, 50, 170, 170);
            g2.fillOval(-60, h - 150, 190, 190);
            g2.dispose();
        }
    }

    /** Iconos vectoriales simples para los campos (usuario y candado). */
    private static class IconoCampo implements Icon {

        static final int USUARIO = 0;
        static final int CANDADO = 1;
        private final int tipo;

        IconoCampo(int tipo) {
            this.tipo = tipo;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(GRIS_ICONO);
            g2.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            if (tipo == USUARIO) {
                g2.drawOval(5, 1, 6, 6);
                g2.draw(new Arc2D.Float(2, 10, 12, 10, 0, 180, Arc2D.OPEN));
            } else {
                g2.drawRoundRect(3, 7, 10, 8, 3, 3);
                g2.draw(new Arc2D.Float(5, 1, 6, 8, 0, 180, Arc2D.OPEN));
                g2.drawLine(5, 5, 5, 7);
                g2.drawLine(11, 5, 11, 7);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    // ================================================================= main de prueba

    public static void main(String[] args) {
        // Color de acento de FlatLaf = naranja del logo (foco, checkbox, selección)
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#E45E16"));

        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("CheckBox.arc", 5);
        UIManager.put("Component.focusWidth", 1);

        FlatLightLaf.setup();
        JFrame.setDefaultLookAndFeelDecorated(true);

        java.awt.EventQueue.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}