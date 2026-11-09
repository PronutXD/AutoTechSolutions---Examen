package views.formularios.clientes;

import java.awt.Component;
import java.awt.Frame;
import java.time.Year;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import model.Cliente;
import model.Vehiculo;
import views.formularios.DlgFormulario;

/**
 * Formulario de vehículo: sirve para crear (existente = null) y para editar.
 * El dueño se elige de una lista de clientes (el vehículo siempre pertenece a uno).
 */
public class DlgVehiculo extends DlgFormulario {

    private final Vehiculo vehiculo;

    private final JComboBox<Cliente> cmbPropietario;
    private final JTextField txtPlaca = campoTexto();
    private final JTextField txtVin = campoTexto();
    private final JTextField txtMarca = campoTexto();
    private final JTextField txtModelo = campoTexto();
    private final JTextField txtAnio = campoTexto();
    private final JTextField txtColor = campoTexto();
    private final JTextField txtKilometraje = campoTexto();
    private final JComboBox<String> cmbCombustible = new JComboBox<>(Vehiculo.COMBUSTIBLES);

    /** @param clientes clientes que se pueden elegir como propietario (no vacío) */
    public DlgVehiculo(Frame propietario, Vehiculo existente, List<Cliente> clientes) {
        super(propietario,
                existente == null ? "Nuevo vehículo" : "Editar vehículo",
                "Los campos con * son obligatorios");
        this.vehiculo = existente != null ? existente : new Vehiculo();

        cmbPropietario = new JComboBox<>(clientes.toArray(new Cliente[0]));
        cmbPropietario.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int i,
                    boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, i, seleccionado, foco);
                if (valor instanceof Cliente c) {
                    setText(c.getDocumento() + " · " + c.getNombreCompleto());
                }
                return this;
            }
        });

        agregarCampo("Propietario", cmbPropietario, true);
        agregarCampo("Placa", txtPlaca, true);
        agregarCampo("VIN / Chasis", txtVin, false);
        agregarCampo("Marca", txtMarca, true);
        agregarCampo("Modelo", txtModelo, true);
        agregarCampo("Año", txtAnio, true);
        agregarCampo("Color", txtColor, false);
        agregarCampo("Kilometraje", txtKilometraje, false);
        agregarCampo("Combustible", cmbCombustible, true);

        if (existente != null) {
            for (int i = 0; i < cmbPropietario.getItemCount(); i++) {
                if (cmbPropietario.getItemAt(i).getId() == existente.getIdCliente()) {
                    cmbPropietario.setSelectedIndex(i);
                    break;
                }
            }
            txtPlaca.setText(existente.getPlaca());
            txtVin.setText(existente.getVin());
            txtMarca.setText(existente.getMarca());
            txtModelo.setText(existente.getModelo());
            txtAnio.setText(String.valueOf(existente.getAnio()));
            txtColor.setText(existente.getColor());
            txtKilometraje.setText(String.valueOf(existente.getKilometraje()));
            cmbCombustible.setSelectedItem(existente.getCombustible());
        } else {
            txtKilometraje.setText("0");
        }
        armar();
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    @Override
    protected String validar() {
        if (cmbPropietario.getSelectedItem() == null) {
            return "Elige el propietario del vehículo.";
        }
        String placa = txtPlaca.getText().trim();
        if (placa.isEmpty()) {
            return "La placa es obligatoria.";
        }
        if (placa.length() > 15) {
            return "La placa no puede pasar de 15 caracteres.";
        }
        if (txtVin.getText().trim().length() > 25) {
            return "El VIN no puede pasar de 25 caracteres.";
        }
        if (txtMarca.getText().isBlank()) {
            return "La marca es obligatoria.";
        }
        if (txtModelo.getText().isBlank()) {
            return "El modelo es obligatorio.";
        }
        int anio;
        try {
            anio = Integer.parseInt(txtAnio.getText().trim());
        } catch (NumberFormatException e) {
            return "El año debe ser un número (por ejemplo 2015).";
        }
        if (anio < 1950 || anio > Year.now().getValue() + 1) {
            return "El año debe estar entre 1950 y " + (Year.now().getValue() + 1) + ".";
        }
        String km = txtKilometraje.getText().trim();
        if (!km.isEmpty()) {
            try {
                if (Integer.parseInt(km) < 0) {
                    return "El kilometraje no puede ser negativo.";
                }
            } catch (NumberFormatException e) {
                return "El kilometraje debe ser un número entero.";
            }
        }
        return null;
    }

    @Override
    protected void aplicar() {
        Cliente dueno = (Cliente) cmbPropietario.getSelectedItem();
        vehiculo.setIdCliente(dueno.getId());
        vehiculo.setPlaca(txtPlaca.getText().trim().toUpperCase());
        vehiculo.setVin(txtVin.getText().trim().toUpperCase());
        vehiculo.setMarca(txtMarca.getText().trim());
        vehiculo.setModelo(txtModelo.getText().trim());
        vehiculo.setAnio(Integer.parseInt(txtAnio.getText().trim()));
        vehiculo.setColor(txtColor.getText().trim());
        String km = txtKilometraje.getText().trim();
        vehiculo.setKilometraje(km.isEmpty() ? 0 : Integer.parseInt(km));
        vehiculo.setCombustible((String) cmbCombustible.getSelectedItem());
    }
}
