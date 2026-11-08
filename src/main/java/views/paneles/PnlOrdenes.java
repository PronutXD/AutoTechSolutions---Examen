package views.paneles;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import views.componentes.PnlTabla;

/**
 * Pantalla "Órdenes de trabajo": creación y seguimiento de órdenes de servicio.
 */
public class PnlOrdenes extends PnlSeccion {

    private final PnlTabla tblOrdenes = new PnlTabla(
            "Nº Orden", "Fecha", "Cliente", "Vehículo", "Mecánico", "Estado", "Total");

    public PnlOrdenes() {
        super("Órdenes de Trabajo",
                "Gestión, emisión y seguimiento de las órdenes de servicio del taller");

        configurarOrdenes();

        JTabbedPane tabs = crearPestanas();
        agregarPestana(tabs, "Órdenes Activas y Histórico", tblOrdenes);
    }

    private void configurarOrdenes() {
        tblOrdenes.setPlaceholder("Buscar por cliente, placa o número de orden...");
        tblOrdenes.addFiltro("Estado", "Todos", "Pendiente", "En Proceso", "Completada", "Cancelada");
        
        var btnNuevaOrden = tblOrdenes.addAccion("Nueva orden", true, false);
        tblOrdenes.addAccion("Ver / Editar", false, true);
        tblOrdenes.addAccion("Imprimir", false, true);
        
        tblOrdenes.setAnchos(90, 100, 160, 110, 150, 110, 90);
        tblOrdenes.alinearDerecha(6);

        // Acción para el botón "Nueva orden"
        btnNuevaOrden.addActionListener(e -> {
            try {
                JTextField txtCliente = new JTextField(15);
                JTextField txtVehiculo = new JTextField(15);
                JTextField txtMecanico = new JTextField(15);
                JTextField txtEstado = new JTextField("Pendiente", 15);
                JTextField txtTotal = new JTextField("0.00", 15);

                JPanel panelFormulario = new JPanel();
                panelFormulario.setLayout(new java.awt.GridLayout(0, 1, 3, 3));
                panelFormulario.add(new JLabel("Cliente:"));
                panelFormulario.add(txtCliente);
                panelFormulario.add(new JLabel("Vehículo (Placa):"));
                panelFormulario.add(txtVehiculo);
                panelFormulario.add(new JLabel("Mecánico Asignado:"));
                panelFormulario.add(txtMecanico);
                panelFormulario.add(new JLabel("Estado Inicial:"));
                panelFormulario.add(txtEstado);
                panelFormulario.add(new JLabel("Total estimado:"));
                panelFormulario.add(txtTotal);

                JScrollPane scrollPane = new JScrollPane(panelFormulario);
                scrollPane.setPreferredSize(new java.awt.Dimension(300, 260));

                int resultado = JOptionPane.showConfirmDialog(
                        this, scrollPane, "Crear Nueva Orden de Trabajo", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (resultado == JOptionPane.OK_OPTION) {
                    String cliente = txtCliente.getText().trim();
                    String vehiculo = txtVehiculo.getText().trim();

                    if (cliente.isEmpty() || vehiculo.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "El cliente y el vehículo son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Generamos un número correlativo simple para la prueba
                    String numOrden = "ORD-" + (tblOrdenes.getModelo().getRowCount() + 101);
                    String fecha = java.time.LocalDate.now().toString();

                    tblOrdenes.getModelo().addRow(new Object[]{
                        numOrden, fecha, cliente, vehiculo,
                        txtMecanico.getText().trim(),
                        txtEstado.getText().trim(),
                        txtTotal.getText().trim()
                    });

                    JOptionPane.showMessageDialog(this, "¡Orden creada con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al crear la orden: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}