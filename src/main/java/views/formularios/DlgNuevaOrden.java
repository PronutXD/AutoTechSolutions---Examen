package views.formularios;

import java.awt.Dimension;
import java.awt.Frame;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import model.Cliente;
import model.Mecanico;
import model.Orden;
import model.Vehiculo;

/**
 * Formulario para abrir una orden. Los campos son los de la tabla OrdenesTrabajo
 * (vehículo, kilometraje, motivo, impuesto) más los mecánicos que se asignan.
 * El cliente solo sirve para filtrar la lista de vehículos.
 */
public class DlgNuevaOrden extends DlgFormulario {

    private final Orden orden = new Orden();
    private final List<Vehiculo> vehiculos;

    private final JComboBox<Cliente> cmbCliente;
    private final JComboBox<Vehiculo> cmbVehiculo = new JComboBox<>();
    private final JTextField txtKilometraje = campoTexto();
    private final JTextField txtMotivo = campoTexto();
    private final JTextField txtImpuesto = campoTexto();
    private final JComboBox<Mecanico> cmbResponsable;
    private final JList<Mecanico> lstAdicionales;

    /**
     * @param clientes   clientes activos
     * @param vehiculos  todos los vehículos activos (el formulario muestra los del cliente elegido)
     * @param mecanicos  mecánicos activos
     */
    public DlgNuevaOrden(Frame propietario, List<Cliente> clientes, List<Vehiculo> vehiculos,
            List<Mecanico> mecanicos) {
        super(propietario, "Nueva orden de trabajo", "Los campos con * son obligatorios");
        this.vehiculos = vehiculos;

        cmbCliente = new JComboBox<>(clientes.toArray(new Cliente[0]));
        cmbCliente.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> lista, Object valor, int i,
                    boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, i, seleccionado, foco);
                if (valor instanceof Cliente c) {
                    setText(c.getDocumento() + " · " + c.getNombreCompleto());
                }
                return this;
            }
        });

        cmbResponsable = new JComboBox<>(mecanicos.toArray(new Mecanico[0]));
        lstAdicionales = new JList<>(mecanicos.toArray(new Mecanico[0]));
        lstAdicionales.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstAdicionales.setVisibleRowCount(3);
        JScrollPane scrollAdicionales = new JScrollPane(lstAdicionales);
        scrollAdicionales.setPreferredSize(new Dimension(300, 70));

        agregarCampo("Cliente", cmbCliente, true);
        agregarCampo("Vehículo", cmbVehiculo, true);
        agregarCampo("Kilometraje de ingreso", txtKilometraje, true);
        agregarCampo("Motivo", txtMotivo, false);
        agregarCampo("Impuesto (%)", txtImpuesto, true);
        agregarCampo("Mecánico responsable", cmbResponsable, true);
        agregarCampo("Otros mecánicos", scrollAdicionales, false);

        txtImpuesto.setText(orden.getPorcentajeImpuesto().toPlainString());
        cmbCliente.addActionListener(e -> cargarVehiculos());
        cmbVehiculo.addActionListener(e -> sugerirKilometraje());
        cargarVehiculos();
        armar();
    }

    /** La orden ya llena (vehículo, kilometraje, motivo, impuesto). El usuario lo pone el controlador. */
    public Orden getOrden() {
        return orden;
    }

    public int getIdResponsable() {
        return ((Mecanico) cmbResponsable.getSelectedItem()).getId();
    }

    public List<Integer> getIdsAdicionales() {
        List<Integer> ids = new ArrayList<>();
        for (Mecanico m : lstAdicionales.getSelectedValuesList()) {
            ids.add(m.getId());
        }
        return ids;
    }

    private void cargarVehiculos() {
        Cliente c = (Cliente) cmbCliente.getSelectedItem();
        DefaultComboBoxModel<Vehiculo> modelo = new DefaultComboBoxModel<>();
        if (c != null) {
            for (Vehiculo v : vehiculos) {
                if (v.getIdCliente() == c.getId()) {
                    modelo.addElement(v);
                }
            }
        }
        cmbVehiculo.setModel(modelo);
        sugerirKilometraje();
    }

    /** Propone el último kilometraje conocido del vehículo; el usuario lo puede cambiar. */
    private void sugerirKilometraje() {
        Vehiculo v = (Vehiculo) cmbVehiculo.getSelectedItem();
        txtKilometraje.setText(v == null ? "" : String.valueOf(v.getKilometraje()));
    }

    @Override
    protected String validar() {
        if (cmbCliente.getSelectedItem() == null) {
            return "Elige un cliente.";
        }
        if (cmbVehiculo.getSelectedItem() == null) {
            return "Ese cliente no tiene vehículos activos. Registra uno en Clientes y vehículos.";
        }
        try {
            if (Integer.parseInt(txtKilometraje.getText().trim()) < 0) {
                return "El kilometraje no puede ser negativo.";
            }
        } catch (NumberFormatException e) {
            return "El kilometraje debe ser un número entero.";
        }
        if (txtMotivo.getText().trim().length() > 300) {
            return "El motivo no puede pasar de 300 caracteres.";
        }
        try {
            BigDecimal impuesto = new BigDecimal(txtImpuesto.getText().trim());
            if (impuesto.signum() < 0 || impuesto.compareTo(new BigDecimal("100")) > 0) {
                return "El impuesto debe estar entre 0 y 100.";
            }
        } catch (NumberFormatException e) {
            return "El impuesto debe ser un número (por ejemplo 15).";
        }
        if (cmbResponsable.getSelectedItem() == null) {
            return "Elige el mecánico responsable.";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        orden.setIdVehiculo(((Vehiculo) cmbVehiculo.getSelectedItem()).getId());
        orden.setKilometrajeIngreso(Integer.parseInt(txtKilometraje.getText().trim()));
        orden.setMotivo(txtMotivo.getText().trim());
        orden.setPorcentajeImpuesto(new BigDecimal(txtImpuesto.getText().trim()));
    }
}
