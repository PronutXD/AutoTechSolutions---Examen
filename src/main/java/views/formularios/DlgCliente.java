package views.formularios;

import java.awt.Frame;
import java.util.regex.Pattern;
import javax.swing.JTextField;
import model.Cliente;

/** Formulario de cliente: sirve para crear (existente = null) y para editar. */
public class DlgCliente extends DlgFormulario {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\- ]{7,20}$");

    private final Cliente cliente;

    private final JTextField txtDocumento = campoTexto();
    private final JTextField txtNombres = campoTexto();
    private final JTextField txtApellidos = campoTexto();
    private final JTextField txtTelefono = campoTexto();
    private final JTextField txtCorreo = campoTexto();
    private final JTextField txtDireccion = campoTexto();

    public DlgCliente(Frame propietario, Cliente existente) {
        super(propietario,
                existente == null ? "Nuevo cliente" : "Editar cliente",
                "Los campos con * son obligatorios");
        this.cliente = existente != null ? existente : new Cliente();

        agregarCampo("Documento", txtDocumento, true);
        agregarCampo("Nombres", txtNombres, true);
        agregarCampo("Apellidos", txtApellidos, true);
        agregarCampo("Teléfono", txtTelefono, false);
        agregarCampo("Correo", txtCorreo, false);
        agregarCampo("Dirección", txtDireccion, false);

        if (existente != null) {
            txtDocumento.setText(existente.getDocumento());
            txtNombres.setText(existente.getNombres());
            txtApellidos.setText(existente.getApellidos());
            txtTelefono.setText(existente.getTelefono());
            txtCorreo.setText(existente.getCorreo());
            txtDireccion.setText(existente.getDireccion());
        }
        armar();
    }

    public Cliente getCliente() {
        return cliente;
    }

    @Override
    protected String validar() {
        if (txtDocumento.getText().isBlank()) {
            return "El documento es obligatorio.";
        }
        if (txtDocumento.getText().trim().length() > 20) {
            return "El documento no puede pasar de 20 caracteres.";
        }
        if (txtNombres.getText().isBlank()) {
            return "Los nombres son obligatorios.";
        }
        if (txtApellidos.getText().isBlank()) {
            return "Los apellidos son obligatorios.";
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
        cliente.setDocumento(txtDocumento.getText().trim());
        cliente.setNombres(txtNombres.getText().trim());
        cliente.setApellidos(txtApellidos.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());
        cliente.setCorreo(txtCorreo.getText().trim());
        cliente.setDireccion(txtDireccion.getText().trim());
    }
}
