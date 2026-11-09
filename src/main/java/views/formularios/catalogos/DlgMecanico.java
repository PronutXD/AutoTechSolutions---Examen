package views.formularios.catalogos;

import java.awt.Frame;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import model.Mecanico;
import views.formularios.DlgFormulario;

/** Formulario de mecánico: sirve para crear (existente = null) y para editar. */
public class DlgMecanico extends DlgFormulario {

    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\- ]{7,20}$");

    private final Mecanico mecanico;

    private final JTextField txtDocumento = campoTexto();
    private final JTextField txtNombres = campoTexto();
    private final JTextField txtApellidos = campoTexto();
    private final JTextField txtTelefono = campoTexto();
    private final JTextField txtEspecialidad = campoTexto();
    private final JCheckBox chkDisponible = new JCheckBox("Disponible para recibir órdenes");

    public DlgMecanico(Frame propietario, Mecanico existente) {
        super(propietario,
                existente == null ? "Nuevo mecánico" : "Editar mecánico",
                "Los campos con * son obligatorios");
        this.mecanico = existente != null ? existente : new Mecanico();

        agregarCampo("Documento", txtDocumento, true);
        agregarCampo("Nombres", txtNombres, true);
        agregarCampo("Apellidos", txtApellidos, true);
        agregarCampo("Teléfono", txtTelefono, false);
        agregarCampo("Especialidad", txtEspecialidad, false);
        agregarCampo("Disponibilidad", chkDisponible, false);

        chkDisponible.setOpaque(false);
        chkDisponible.setSelected(true);
        if (existente != null) {
            txtDocumento.setText(existente.getDocumento());
            txtNombres.setText(existente.getNombres());
            txtApellidos.setText(existente.getApellidos());
            txtTelefono.setText(existente.getTelefono());
            txtEspecialidad.setText(existente.getEspecialidad());
            chkDisponible.setSelected(existente.isDisponible());
        }
        armar();
    }

    public Mecanico getMecanico() {
        return mecanico;
    }

    @Override
    protected String validar() {
        if (txtDocumento.getText().isBlank()) {
            return "El documento es obligatorio.";
        }
        if (txtNombres.getText().isBlank() || txtApellidos.getText().isBlank()) {
            return "Los nombres y los apellidos son obligatorios.";
        }
        String tel = txtTelefono.getText().trim();
        if (!tel.isEmpty() && !TELEFONO.matcher(tel).matches()) {
            return "El teléfono solo puede tener números, espacios, + - ( ) (7 a 20 caracteres).";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        mecanico.setDocumento(txtDocumento.getText().trim());
        mecanico.setNombres(txtNombres.getText().trim());
        mecanico.setApellidos(txtApellidos.getText().trim());
        mecanico.setTelefono(txtTelefono.getText().trim());
        mecanico.setEspecialidad(txtEspecialidad.getText().trim());
        mecanico.setDisponible(chkDisponible.isSelected());
    }
}
