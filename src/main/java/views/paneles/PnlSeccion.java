package views.paneles;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;

/**
 * Base común de todas las pantallas de sección.
 *
 * Cada pantalla pasa su título y subtítulo al constructor y coloca su propio
 * contenido (formularios, tablas, pestañas) dentro de getContenido(). Así el
 * título pertenece al panel y no lo decide la ventana principal ni el controlador.
 */
public abstract class PnlSeccion extends JPanel {

    private static final Color TEXTO = new Color(0x1E1E1E);
    private static final Color GRIS = new Color(0x6B6B6B);
    private static final Color LINEA = new Color(0xE8E7E2);
    private static final Color FONDO = new Color(0xFFFEF8);

    private final JPanel contenido = new JPanel(new BorderLayout());

    protected PnlSeccion(String titulo, String subtitulo) {
        setLayout(new BorderLayout());
        setBackground(FONDO);

        add(crearEncabezado(titulo, subtitulo), BorderLayout.NORTH);

        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        add(contenido, BorderLayout.CENTER);
    }

    /** Zona donde cada pantalla coloca su formulario, sus tablas y sus pestañas. */
    protected JPanel getContenido() {
        return contenido;
    }

    private JPanel crearEncabezado(String titulo, String subtitulo) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FONDO);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINEA),
                BorderFactory.createEmptyBorder(22, 32, 18, 32)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 24f));
        lblTitulo.setForeground(TEXTO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.setFont(lblSubtitulo.getFont().deriveFont(13f));
        lblSubtitulo.setForeground(GRIS);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(lblSubtitulo);
        header.add(textos, BorderLayout.WEST);

        JLabel lblFecha = new JLabel(fechaHoy());
        lblFecha.setFont(lblFecha.getFont().deriveFont(13f));
        lblFecha.setForeground(GRIS);
        header.add(lblFecha, BorderLayout.EAST);
        return header;
    }

    private static String fechaHoy() {
        String t = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.of("es", "MX")));
        return t.substring(0, 1).toUpperCase() + t.substring(1);
    }
}