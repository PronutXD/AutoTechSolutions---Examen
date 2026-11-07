package views;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * Vista de la ventana principal: sidebar a la izquierda y un contenedor central
 * (CardLayout) donde se muestra el panel de la sección elegida.
 *
 * Esta clase SOLO dibuja y expone métodos para manipular la interfaz. No decide
 * qué secciones existen, qué panel va en cada una, ni qué pasa al hacer clic:
 * eso lo define controller.PrincipalController.
 */
public class FrmPrincipal extends JFrame {

    /** Iconos disponibles para los botones del sidebar. */
    public enum Icono {
        CLIENTES, ORDENES, INVENTARIO, CATALOGOS, CONSULTAS
    }

    // ----------------------------------------------------------- paleta
    private static final Color NARANJA = new Color(0xE45E16);
    private static final Color NARANJA_OSCURO = new Color(0xB8480E);
    private static final Color SIDEBAR = new Color(0x1E1E1E);
    private static final Color SIDEBAR_HOVER = new Color(255, 255, 255, 22);
    private static final Color SIDEBAR_TEXTO = new Color(0xC9C9C9);
    private static final Color SIDEBAR_ICONO = new Color(0x9A9A9A);
    private static final Color SIDEBAR_LINEA = new Color(255, 255, 255, 28);
    private static final Color TEXTO = new Color(0x1E1E1E);
    private static final Color GRIS = new Color(0x6B6B6B);
    private static final Color LINEA = new Color(0xE8E7E2);
    private static final Color FONDO = new Color(0xFFFEF8);

    private static final String STYLE = "FlatLaf.style";
    private static final String RUTA_SVG = "assets/img/";
    private static final int ANCHO_SIDEBAR = 250;
    private static final int LOGO_ANCHO = 210;
    private static final int LOGO_ALTO = 45; // proporción del SVG horizontal (553 x 119)

    // ----------------------------------------------------------- componentes
    private final CardLayout cards = new CardLayout();
    private final JPanel contenedor = new JPanel(cards);
    private final JPanel nav = new JPanel();
    private final ButtonGroup grupoNav = new ButtonGroup();
    private final Map<String, Seccion> secciones = new LinkedHashMap<>();
    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblSubtitulo = new JLabel();
    private final JLabel lblUsuario = new JLabel();

    // ----------------------------------------------------------- eventos hacia el controlador
    private Consumer<String> alNavegar = clave -> { };
    private Runnable alCerrarSesion = () -> { };

    public FrmPrincipal() {
        initUI();
    }

    // ================================================================= API para el controlador

    /** Muestra el nombre del usuario en la parte inferior del sidebar. */
    public void setUsuario(String usuario) {
        String u = (usuario == null || usuario.isBlank()) ? "usuario" : usuario;
        lblUsuario.setText(u);
        lblUsuario.setIcon(new IconoAvatar(u));
    }

    /** Agrega un botón al sidebar y registra su panel en el contenedor central. */
    public void agregarSeccion(String clave, String titulo, String subtitulo, Icono icono, JPanel panel) {
        BotonNav boton = new BotonNav(titulo, icono);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.addActionListener(e -> alNavegar.accept(clave));
        grupoNav.add(boton);

        nav.add(boton);
        nav.add(Box.createVerticalStrut(6));
        nav.revalidate();

        contenedor.add(panel, clave);
        secciones.put(clave, new Seccion(titulo, subtitulo, boton));
    }

    /** Cambia el panel visible, el encabezado y el botón seleccionado. */
    public void mostrarSeccion(String clave) {
        Seccion s = secciones.get(clave);
        if (s == null) {
            return;
        }
        s.boton.setSelected(true);
        lblTitulo.setText(s.titulo);
        lblSubtitulo.setText(s.subtitulo);
        cards.show(contenedor, clave);
    }

    /** Se llama con la clave de la sección cuando el usuario pulsa un botón del sidebar. */
    public void setAlNavegar(Consumer<String> accion) {
        this.alNavegar = accion;
    }

    /** Se llama cuando el usuario pulsa "Cerrar sesión". */
    public void setAlCerrarSesion(Runnable accion) {
        this.alCerrarSesion = accion;
    }

    /** Muestra un diálogo Sí/No y devuelve true si el usuario eligió Sí. */
    public boolean confirmar(String titulo, String mensaje) {
        return JOptionPane.showConfirmDialog(this, mensaje, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ================================================================= UI

    private void initUI() {
        setTitle("AutoTech Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImages(FrmLogin.cargarIconos());
        setLayout(new BorderLayout());

        add(crearSidebar(), BorderLayout.WEST);
        add(crearAreaCentral(), BorderLayout.CENTER);

        setUsuario(null);
        setMinimumSize(new Dimension(1000, 620));
        setSize(1280, 780);
        setLocationRelativeTo(null);
    }

    // ----------------------------------------------------------- sidebar

    private JPanel crearSidebar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setBackground(SIDEBAR);
        sb.setPreferredSize(new Dimension(ANCHO_SIDEBAR, 0));

        // --- superior: logo + línea
        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        JPanel logoBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoBox.setOpaque(false);
        logoBox.setBorder(BorderFactory.createEmptyBorder(26, 20, 22, 20));
        logoBox.add(crearLogo());
        superior.add(logoBox, BorderLayout.CENTER);
        superior.add(new Linea(SIDEBAR_LINEA), BorderLayout.SOUTH);
        sb.add(superior, BorderLayout.NORTH);

        // --- centro: navegación (los botones los agrega agregarSeccion)
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(22, 16, 0, 16));

        JLabel menu = new JLabel("MENÚ");
        menu.setForeground(new Color(0x8A8A8A));
        menu.setFont(menu.getFont().deriveFont(Font.BOLD, 11f));
        menu.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 0));
        menu.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(menu);
        sb.add(nav, BorderLayout.CENTER);

        // --- inferior: usuario + cerrar sesión
        sb.add(crearPanelUsuario(), BorderLayout.SOUTH);
        return sb;
    }

    private JComponent crearLogo() {
        JLabel logo = new JLabel();
        FlatSVGIcon svg = new FlatSVGIcon(RUTA_SVG + "logo_horizontal_oscuro.svg").derive(LOGO_ANCHO, LOGO_ALTO);
        BufferedImage png = FrmLogin.cargarImagen("logo_horizontal_oscuro.png");
        if (svg.hasFound()) {
            logo.setIcon(svg);
        } else if (png != null) {
            logo.setIcon(FrmLogin.escalar(png, LOGO_ANCHO));
        } else {
            logo.setText("AUTOTECHSOLUTIONS");
            logo.setForeground(NARANJA);
            logo.setFont(logo.getFont().deriveFont(Font.BOLD, 17f));
        }
        return logo;
    }

    private JPanel crearPanelUsuario() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(new Linea(SIDEBAR_LINEA), BorderLayout.NORTH);

        JPanel interior = new JPanel(new BorderLayout(0, 14));
        interior.setOpaque(false);
        interior.setBorder(BorderFactory.createEmptyBorder(16, 16, 20, 16));

        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setFont(lblUsuario.getFont().deriveFont(Font.BOLD, 13f));
        lblUsuario.setIconTextGap(12);
        interior.add(lblUsuario, BorderLayout.NORTH);

        JButton salir = new JButton("Cerrar sesión");
        salir.setFocusPainted(false);
        salir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        salir.setMargin(new Insets(9, 12, 9, 12));
        salir.putClientProperty(STYLE,
                "arc: 10; background: #2A2A2A; foreground: #E8E8E8; "
                + "hoverBackground: #3A3A3A; pressedBackground: #444444; "
                + "borderWidth: 0; focusWidth: 0");
        salir.addActionListener(e -> alCerrarSesion.run());
        interior.add(salir, BorderLayout.SOUTH);

        p.add(interior, BorderLayout.CENTER);
        return p;
    }

    // ----------------------------------------------------------- área central

    private JPanel crearAreaCentral() {
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(FONDO);

        // Encabezado: título de la sección + fecha
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FONDO);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINEA),
                BorderFactory.createEmptyBorder(22, 32, 18, 32)));

        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 24f));
        lblTitulo.setForeground(TEXTO);
        lblSubtitulo.setFont(lblSubtitulo.getFont().deriveFont(13f));
        lblSubtitulo.setForeground(GRIS);

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulos.add(lblTitulo);
        titulos.add(Box.createVerticalStrut(4));
        titulos.add(lblSubtitulo);
        header.add(titulos, BorderLayout.WEST);

        JLabel fecha = new JLabel(fechaHoy());
        fecha.setForeground(GRIS);
        fecha.setFont(fecha.getFont().deriveFont(13f));
        header.add(fecha, BorderLayout.EAST);

        centro.add(header, BorderLayout.NORTH);

        // Contenedor de paneles (los agrega agregarSeccion)
        contenedor.setOpaque(false);
        contenedor.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        centro.add(contenedor, BorderLayout.CENTER);
        return centro;
    }

    private static String fechaHoy() {
        String t = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.of("es", "MX")));
        return t.substring(0, 1).toUpperCase() + t.substring(1);
    }

    // ================================================================= clases auxiliares

    /** Datos de una sección ya registrada en el sidebar. */
    private static class Seccion {

        final String titulo;
        final String subtitulo;
        final BotonNav boton;

        Seccion(String titulo, String subtitulo, BotonNav boton) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.boton = boton;
        }
    }

    /** Línea horizontal de 1 px. */
    private static class Linea extends JComponent {

        private final Color color;

        Linea(Color color) {
            this.color = color;
            setPreferredSize(new Dimension(1, 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), 1);
        }
    }

    /** Botón del sidebar dibujado a mano: icono + texto, con estados hover y seleccionado. */
    private static class BotonNav extends JToggleButton {

        private final IconoNav icono;
        private boolean hover;

        BotonNav(String texto, Icono tipoIcono) {
            super(texto);
            this.icono = new IconoNav(tipoIcono);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(getFont().deriveFont(Font.BOLD, 13.5f));
            setPreferredSize(new Dimension(200, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Object hints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
            if (hints instanceof Map<?, ?> m) {
                g2.addRenderingHints(m);
            }

            int w = getWidth();
            int h = getHeight();
            boolean sel = isSelected();

            if (sel) {
                g2.setColor(NARANJA);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            } else if (hover) {
                g2.setColor(SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            }

            icono.color = sel || hover ? Color.WHITE : SIDEBAR_ICONO;
            icono.paintIcon(this, g2, 14, (h - icono.getIconHeight()) / 2);

            g2.setFont(getFont());
            g2.setColor(sel || hover ? Color.WHITE : SIDEBAR_TEXTO);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 46, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Iconos vectoriales del menú. */
    private static class IconoNav implements Icon {

        private final Icono tipo;
        Color color = SIDEBAR_ICONO;

        IconoNav(Icono tipo) {
            this.tipo = tipo;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (tipo) {
                case CLIENTES -> {
                    g2.drawOval(7, 2, 6, 6);
                    g2.draw(new Arc2D.Float(3, 11, 14, 11, 0, 180, Arc2D.OPEN));
                }
                case ORDENES -> {
                    g2.drawRoundRect(4, 3, 12, 15, 3, 3);
                    g2.drawRoundRect(7, 1, 6, 4, 2, 2);
                    g2.drawLine(7, 10, 13, 10);
                    g2.drawLine(7, 13, 13, 13);
                }
                case INVENTARIO -> {
                    g2.drawRoundRect(2, 3, 16, 4, 1, 1);
                    g2.drawRoundRect(4, 7, 12, 10, 1, 1);
                    g2.drawLine(8, 11, 12, 11);
                }
                case CATALOGOS -> {
                    for (int i = 0; i < 3; i++) {
                        float yy = 4.5f + i * 5.5f;
                        g2.fill(new Ellipse2D.Float(2.5f, yy - 1.3f, 2.6f, 2.6f));
                        g2.draw(new Line2D.Float(7.5f, yy, 17.5f, yy));
                    }
                }
                case CONSULTAS -> {
                    g2.drawLine(2, 17, 18, 17);
                    g2.fillRoundRect(4, 10, 3, 7, 1, 1);
                    g2.fillRoundRect(9, 4, 3, 13, 1, 1);
                    g2.fillRoundRect(14, 7, 3, 10, 1, 1);
                }
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }
    }

    /** Círculo naranja con la inicial del usuario. */
    private static class IconoAvatar implements Icon {

        private final String inicial;

        IconoAvatar(String usuario) {
            this.inicial = usuario.substring(0, 1).toUpperCase();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(x, y, NARANJA, x + 38, y + 38, NARANJA_OSCURO));
            g2.fill(new Ellipse2D.Float(x, y, 38, 38));
            g2.setColor(Color.WHITE);
            g2.setFont(c.getFont().deriveFont(Font.BOLD, 16f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(inicial, x + (38 - fm.stringWidth(inicial)) / 2,
                    y + (38 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 38;
        }

        @Override
        public int getIconHeight() {
            return 38;
        }
    }
}