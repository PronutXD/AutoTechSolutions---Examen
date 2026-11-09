package views.formularios;

import java.awt.Frame;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import model.ControlCalidad;
import model.Mecanico;

/** Formulario para registrar una revisión de calidad de la orden. */
public class DlgControlCalidad extends DlgFormulario {

    private final JComboBox<Mecanico> cmbInspector;
    private final JComboBox<String> cmbResultado =
            new JComboBox<>(new String[]{ControlCalidad.APROBADO, ControlCalidad.RECHAZADO});
    private final JCheckBox chkPruebaRuta = new JCheckBox("Se hizo prueba de ruta");
    private final JTextField txtObservaciones = campoTexto();

    public DlgControlCalidad(Frame propietario, List<Mecanico> mecanicos) {
        super(propietario, "Control de calidad", "Revisión final antes de entregar el vehículo");
        cmbInspector = new JComboBox<>(mecanicos.toArray(new Mecanico[0]));
        chkPruebaRuta.setOpaque(false);
        agregarCampo("Inspector", cmbInspector, true);
        agregarCampo("Resultado", cmbResultado, true);
        agregarCampo("Prueba de ruta", chkPruebaRuta, false);
        agregarCampo("Observaciones", txtObservaciones, false);
        armar();
    }

    public int getIdInspector() {
        return ((Mecanico) cmbInspector.getSelectedItem()).getId();
    }

    public String getResultado() {
        return (String) cmbResultado.getSelectedItem();
    }

    public boolean isPruebaRuta() {
        return chkPruebaRuta.isSelected();
    }

    public String getObservaciones() {
        return txtObservaciones.getText().trim();
    }

    @Override
    protected String validar() {
        if (cmbInspector.getSelectedItem() == null) {
            return "Elige quién hizo la revisión.";
        }
        if (txtObservaciones.getText().trim().length() > 300) {
            return "Las observaciones no pueden pasar de 300 caracteres.";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        // Nada que volcar: el controlador lee los getters.
    }
}
