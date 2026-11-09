package views.formularios;

import java.awt.Frame;
import java.util.regex.Pattern;
import javax.swing.JTextField;
import model.Proveedor;

/** Formulario de proveedor: sirve para crear (existente = null) y para editar. */
public class DlgProveedor extends DlgFormulario {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\- ]{7,20}$");

    private final Proveedor proveedor;

    private final JTextField txtNombre = campoTexto();
    private final JTextField txtContacto = campoTexto();
    private final JTextField txtTelefono = campoTexto();
    private final JTextField txtCorreo = campoTexto();

    public DlgProveedor(Frame propietario, Proveedor existente) {
        super(propietario,
                existente == null ? "Nuevo proveedor" : "Editar proveedor",
                "Los campos con * son obligatorios");
        this.proveedor = existente != null ? existente : new Proveedor();

        agregarCampo("Nombre", txtNombre, true);
        agregarCampo("Contacto", txtContacto, false);
        agregarCampo("Teléfono", txtTelefono, false);
        agregarCampo("Correo", txtCorreo, false);

        if (existente != null) {
            txtNombre.setText(existente.getNombre());
            txtContacto.setText(existente.getContacto());
            txtTelefono.setText(existente.getTelefono());
            txtCorreo.setText(existente.getCorreo());
        }
        armar();
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    @Override
    protected String validar() {
        if (txtNombre.getText().isBlank()) {
            return "El nombre es obligatorio.";
        }
        String tel = txtTelefono.getText().trim();
        if (!tel.isEmpty() && !TELEFONO.matcher(tel).matches()) {
            return "El teléfono solo puede tener números, espacios, + - ( ) (7 a 20 caracteres).";
        }
        String correo = txtCorreo.getText().trim();
        if (!correo.isEmpty() && !CORREO.matcher(correo).matches()) {
            return "El correo no tiene un formato válido.";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        proveedor.setNombre(txtNombre.getText().trim());
        proveedor.setContacto(txtContacto.getText().trim());
        proveedor.setTelefono(txtTelefono.getText().trim());
        proveedor.setCorreo(txtCorreo.getText().trim());
    }
}
