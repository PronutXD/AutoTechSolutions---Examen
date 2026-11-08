package views.componentes;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Componente reutilizable: una fila con buscador y botones, otra con los filtros
 * (si hay), y debajo una tabla con su contador de registros.
 *
 * Es solo diseño: no conoce la base de datos. El controlador llena el modelo
 * (getModelo()) y conecta los botones y filtros que esta clase le entrega.
 * El buscador sí filtra por texto las filas que ya están cargadas en la tabla.
 */
public class PnlTabla extends JPanel {

    private static final Color GRIS = new Color(0x6B6B6B);
    private static final Color GRIS_ICONO = new Color(0x8C8C8C);
    private static final Color LINEA = new Color(0xE8E7E2);
    private static final String STYLE = "FlatLaf.style";

    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField txtBuscar = new JTextField(18);
    private final JPanel zonaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final JPanel zonaAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    private final JLabel lblConteo = new JLabel();
    private JPopupMenu menuContextual;

    public PnlTabla(String... columnas) {
        super(new BorderLayout(0, 12));
        setOpaque(false);

        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = crearTabla();
        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        add(crearBarra(), BorderLayout.NORTH);
        add(crearScroll(), BorderLayout.CENTER);

        lblConteo.setForeground(GRIS);
        lblConteo.setFont(lblConteo.getFont().deriveFont(12f));
        add(lblConteo, BorderLayout.SOUTH);

        // El contador se actualiza al filtrar y también cuando el controlador llena o cambia el modelo.
        sorter.addRowSorterListener(e -> actualizarConteo());
        modelo.addTableModelListener(e -> SwingUtilities.invokeLater(this::actualizarConteo));
        actualizarConteo();
    }

    // ================================================================= API para el controlador

    public JTable getTabla() {
        return tabla;
    }

    public DefaultTableModel getModelo() {
        return modelo;
    }

    public JTextField getBuscador() {
        return txtBuscar;
    }

    /** Cambia el texto de ayuda del buscador. */
    public void setPlaceholder(String texto) {
        txtBuscar.putClientProperty("JTextField.placeholderText", texto);
    }

    /** Agrega un filtro desplegable con su etiqueta a la izquierda. */
    public JComboBox<String> addFiltro(String etiqueta, String... opciones) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setForeground(GRIS);
        JComboBox<String> combo = new JComboBox<>(opciones);
        zonaFiltros.add(lbl);
        zonaFiltros.add(Box.createHorizontalStrut(6));
        zonaFiltros.add(combo);
        zonaFiltros.add(Box.createHorizontalStrut(20));
        zonaFiltros.setVisible(true);
        return combo;
    }

    /** Agrega un filtro de casilla (por ejemplo "Solo stock bajo"). */
    public JCheckBox addFiltroCheck(String texto) {
        JCheckBox check = new JCheckBox(texto);
        check.setOpaque(false);
        zonaFiltros.add(check);
        zonaFiltros.add(Box.createHorizontalStrut(20));
        zonaFiltros.setVisible(true);
        return check;
    }

    /**
     * Agrega un botón de acción a la derecha.
     *
     * @param principal si es true, se pinta naranja (acción principal)
     * @param requiereSeleccion si es true, el botón solo se habilita con una fila elegida
     */
    public JButton addAccion(String texto, boolean principal, boolean requiereSeleccion) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setMargin(new Insets(7, 16, 7, 16));
        if (principal) {
            boton.setFont(boton.getFont().deriveFont(Font.BOLD));
            boton.putClientProperty(STYLE,
                    "arc: 12; background: #E45E16; foreground: #FFFFFF; "
                    + "hoverBackground: #C84F0F; pressedBackground: #A84008; "
                    + "borderWidth: 0; focusWidth: 0");
        }
        if (requiereSeleccion) {
            boton.setEnabled(false);
            tabla.getSelectionModel().addListSelectionListener(
                    e -> boton.setEnabled(tabla.getSelectedRow() >= 0));
        }
        if (zonaAcciones.getComponentCount() > 0) {
            zonaAcciones.add(Box.createHorizontalStrut(8));
        }
        zonaAcciones.add(boton);
        return boton;
    }

    /**
     * Agrega una opción al menú de clic derecho de la tabla. El clic derecho
     * selecciona primero la fila sobre la que se hizo. El controlador (o el panel)
     * le agrega el ActionListener al item devuelto.
     */
    public JMenuItem addMenuContextual(String texto) {
        if (menuContextual == null) {
            menuContextual = new JPopupMenu();
            tabla.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { mostrarMenu(e); }
                @Override public void mouseReleased(MouseEvent e) { mostrarMenu(e); }
            });
        }
        JMenuItem item = new JMenuItem(texto);
        menuContextual.add(item);
        return item;
    }

    /** Valor de una columna en la fila seleccionada (null si no hay selección). */
    public Object getValorSeleccionado(int columna) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return modelo.getValueAt(tabla.convertRowIndexToModel(fila), columna);
    }

    /** Anchos preferidos de las columnas, en píxeles y en orden. */
    public void setAnchos(int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    /** Alinea a la derecha las columnas indicadas (números, montos). */
    public void alinearDerecha(int... columnas) {
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : columnas) {
            tabla.getColumnModel().getColumn(c).setCellRenderer(derecha);
        }
    }

    // ================================================================= construcción

    private JTable crearTabla() {
        JTable t = new JTable(modelo) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (getRowCount() == 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(GRIS);
                    g2.setFont(getFont().deriveFont(13f));
                    String msg = "No hay registros para mostrar";
                    FontMetrics fm = g2.getFontMetrics();
                    Rectangle r = getVisibleRect();
                    g2.drawString(msg, r.x + (r.width - fm.stringWidth(msg)) / 2, r.y + r.height / 2);
                    g2.dispose();
                }
            }
        };
        t.setRowHeight(36);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(LINEA);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(Font.BOLD));
        return t;
    }

    private JScrollPane crearScroll() {
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createLineBorder(LINEA));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JPanel crearBarra() {
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar...");
        txtBuscar.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        txtBuscar.putClientProperty("JTextField.leadingIcon", new IconoLupa());
        txtBuscar.setMargin(new Insets(6, 8, 6, 8));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrarPorTexto(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrarPorTexto(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrarPorTexto(); }
        });

        zonaFiltros.setOpaque(false);
        zonaFiltros.setVisible(false); // se muestra al agregar el primer filtro
        zonaAcciones.setOpaque(false);

        // Fila 1: buscador a la izquierda y botones a la derecha.
        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buscador.setOpaque(false);
        buscador.add(txtBuscar);

        JPanel fila1 = new JPanel(new BorderLayout(16, 0));
        fila1.setOpaque(false);
        fila1.add(buscador, BorderLayout.WEST);
        fila1.add(zonaAcciones, BorderLayout.EAST);

        // Fila 2: filtros (solo aparece si la tabla tiene alguno).
        JPanel barra = new JPanel(new BorderLayout(0, 10));
        barra.setOpaque(false);
        barra.add(fila1, BorderLayout.NORTH);
        barra.add(zonaFiltros, BorderLayout.CENTER);
        return barra;
    }

    // ================================================================= comportamiento visual

    private void mostrarMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        int fila = tabla.rowAtPoint(e.getPoint());
        if (fila >= 0) {
            tabla.setRowSelectionInterval(fila, fila);
            menuContextual.show(tabla, e.getX(), e.getY());
        }
    }

    private void filtrarPorTexto() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
        }
    }

    private void actualizarConteo() {
        int n = tabla.getRowCount();
        lblConteo.setText(n == 1 ? "1 registro" : n + " registros");
    }

    public Object getAccion(String nuevo_cliente) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /** Lupa vectorial para el buscador. */
    private static class IconoLupa implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(GRIS_ICONO);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(x + 1, y + 1, 9, 9);
            g2.drawLine(x + 10, y + 10, x + 14, y + 14);
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
}