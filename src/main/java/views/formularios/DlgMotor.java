package views.formularios;

import java.awt.Component;
import java.awt.Frame;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import model.Motor;
import model.Repuesto;
import model.Vehiculo;
import views.componentes.Numeros;

/**
 * Formulario de la ficha técnica de un motor: sirve para crear (existente = null) y para editar.
 * Todo motor es un repuesto de categoría Motor, así que al crear se elige cuál de esos
 * repuestos (que aún no tienen ficha) es el que se describe. Al editar ese enlace no cambia.
 */
public class DlgMotor extends DlgFormulario {

    private final Motor motor;

    private final JComboBox<Repuesto> cmbRepuesto;
    private final JTextField txtSerie = campoTexto();
    private final JTextField txtMarca = campoTexto();
    private final JTextField txtModelo = campoTexto();
    private final JTextField txtAnioDesde = campoTexto();
    private final JTextField txtAnioHasta = campoTexto();
    private final JTextField txtCilindraje = campoTexto();
    private final JComboBox<String> cmbCombustible = new JComboBox<>(Vehiculo.COMBUSTIBLES);

    /** @param repuestos repuestos de categoría Motor que se pueden describir (no vacío; al editar, solo el suyo) */
    public DlgMotor(Frame propietario, Motor existente, List<Repuesto> repuestos) {
        super(propietario,
                existente == null ? "Nuevo motor" : "Editar motor",
                "Los campos con * son obligatorios");
        this.motor = existente != null ? existente : new Motor();

        cmbRepuesto = new JComboBox<>(repuestos.toArray(new Repuesto[0]));
        cmbRepuesto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int i,
                    boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, i, seleccionado, foco);
                if (valor instanceof Repuesto r) {
                    setText(r.nombre());
                }
                return this;
            }
        });

        agregarCampo("Repuesto (motor)", cmbRepuesto, true);
        agregarCampo("Número de serie", txtSerie, true);
        agregarCampo("Marca", txtMarca, true);
        agregarCampo("Modelo compatible", txtModelo, true);
        agregarCampo("Año desde", txtAnioDesde, true);
        agregarCampo("Año hasta", txtAnioHasta, true);
        agregarCampo("Cilindraje (L)", txtCilindraje, true);
        agregarCampo("Combustible", cmbCombustible, true);

        if (existente != null) {
            for (int i = 0; i < cmbRepuesto.getItemCount(); i++) {
                if (cmbRepuesto.getItemAt(i).id() == existente.getIdRepuesto()) {
                    cmbRepuesto.setSelectedIndex(i);
                    break;
                }
            }
            cmbRepuesto.setEnabled(false);
            txtSerie.setText(existente.getNumeroSerie());
            txtMarca.setText(existente.getMarca());
            txtModelo.setText(existente.getModeloCompatible());
            txtAnioDesde.setText(String.valueOf(existente.getAnioDesde()));
            txtAnioHasta.setText(String.valueOf(existente.getAnioHasta()));
            txtCilindraje.setText(existente.getCilindraje().toPlainString());
            cmbCombustible.setSelectedItem(existente.getCombustible());
            agregarCampo("Estado", new JLabel(existente.getEstado()), false);
        }
        armar();
    }

    public Motor getMotor() {
        return motor;
    }

    @Override
    protected String validar() {
        if (cmbRepuesto.getSelectedItem() == null) {
            return "Elige el repuesto al que pertenece este motor.";
        }
        if (txtSerie.getText().isBlank()) {
            return "El número de serie es obligatorio.";
        }
        if (txtMarca.getText().isBlank()) {
            return "La marca es obligatoria.";
        }
        if (txtModelo.getText().isBlank()) {
            return "El modelo compatible es obligatorio.";
        }
        int maximo = Year.now().getValue() + 1;
        Integer desde = Numeros.entero(txtAnioDesde.getText());
        Integer hasta = Numeros.entero(txtAnioHasta.getText());
        if (desde == null || hasta == null) {
            return "Los años deben ser números (por ejemplo 2009).";
        }
        if (desde < 1950 || hasta < 1950 || desde > maximo || hasta > maximo) {
            return "Los años deben estar entre 1950 y " + maximo + ".";
        }
        if (desde > hasta) {
            return "El año inicial no puede ser mayor que el año final.";
        }
        BigDecimal cilindraje = Numeros.decimal(txtCilindraje.getText(), 1);
        if (cilindraje == null) {
            return "El cilindraje debe ser un número con un decimal (por ejemplo 1.8).";
        }
        if (cilindraje.signum() <= 0 || cilindraje.compareTo(new BigDecimal("99.9")) > 0) {
            return "El cilindraje debe estar entre 0.1 y 99.9 litros.";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        motor.setIdRepuesto(((Repuesto) cmbRepuesto.getSelectedItem()).id());
        motor.setNumeroSerie(txtSerie.getText().trim());
        motor.setMarca(txtMarca.getText().trim());
        motor.setModeloCompatible(txtModelo.getText().trim());
        motor.setAnioDesde(Numeros.entero(txtAnioDesde.getText()));
        motor.setAnioHasta(Numeros.entero(txtAnioHasta.getText()));
        motor.setCilindraje(Numeros.decimal(txtCilindraje.getText(), 1));
        motor.setCombustible((String) cmbCombustible.getSelectedItem());
    }
}
