package views.formularios;

import java.awt.Frame;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import model.Cuenta;
import model.Mecanico;

/**
 * Formulario de cuenta de acceso. Al crear (existente = null) pide la contraseña dos veces;
 * al editar solo cambia usuario, rol y mecánico (la contraseña se restablece con DlgClave).
 */
public class DlgUsuario extends DlgFormulario {

    private static final String SIN_MECANICO = "Sin mecánico asociado";

    private final boolean nueva;
    private final List<Mecanico> mecanicos;

    private final JTextField txtUsuario = campoTexto();
    private final JPasswordField txtClave = new JPasswordField(24);
    private final JPasswordField txtConfirmar = new JPasswordField(24);
    private final JComboBox<String> cmbRol = new JComboBox<>(Cuenta.ROLES);
    private final JComboBox<Object> cmbMecanico = new JComboBox<>();

    private String usuario;
    private String rol;
    private Integer idMecanico;
    private char[] clave;

    /** @param mecanicos activos sin cuenta (más el de la cuenta que se edita) */
    public DlgUsuario(Frame propietario, Cuenta existente, List<Mecanico> mecanicos) {
        super(propietario,
                existente == null ? "Nuevo usuario" : "Editar usuario",
                existente == null ? "La contraseña se guarda cifrada" : "La contraseña no se cambia aquí");
        this.nueva = existente == null;
        this.mecanicos = mecanicos;

        cmbMecanico.addItem(SIN_MECANICO);
        for (Mecanico m : mecanicos) {
            cmbMecanico.addItem(m);
        }

        agregarCampo("Usuario", txtUsuario, true);
        if (nueva) {
            agregarCampo("Contraseña", txtClave, true);
            agregarCampo("Confirmar contraseña", txtConfirmar, true);
        }
        agregarCampo("Rol", cmbRol, true);
        agregarCampo("Mecánico asociado", cmbMecanico, false);

        if (existente != null) {
            txtUsuario.setText(existente.usuario());
            cmbRol.setSelectedItem(existente.rol());
            for (int i = 0; i < mecanicos.size(); i++) {
                if (existente.idMecanico() != null && mecanicos.get(i).getId() == existente.idMecanico()) {
                    cmbMecanico.setSelectedIndex(i + 1);
                }
            }
        }
        armar();
    }

    public String getUsuario() { return usuario; }
    public String getRol() { return rol; }
    public Integer getIdMecanico() { return idMecanico; }
    /** Solo al crear; el controlador debe borrarla con Arrays.fill al terminar. */
    public char[] getClave() { return clave; }

    @Override
    protected String validar() {
        String u = txtUsuario.getText().trim();
        if (u.length() < 3 || u.length() > 50) {
            return "El usuario debe tener entre 3 y 50 caracteres.";
        }
        if (u.matches(".*\\s.*")) {
            return "El usuario no puede tener espacios.";
        }
        if (nueva) {
            char[] c1 = txtClave.getPassword();
            char[] c2 = txtConfirmar.getPassword();
            boolean igual = Arrays.equals(c1, c2);
            int largo = c1.length;
            Arrays.fill(c1, '\0');
            Arrays.fill(c2, '\0');
            if (largo < 8) {
                return "La contraseña debe tener al menos 8 caracteres.";
            }
            if (!igual) {
                return "Las contraseñas no coinciden.";
            }
        }
        return null;
    }

    @Override
    protected void aplicar() {
        usuario = txtUsuario.getText().trim();
        rol = (String) cmbRol.getSelectedItem();
        int i = cmbMecanico.getSelectedIndex();
        idMecanico = i <= 0 ? null : mecanicos.get(i - 1).getId();
        if (nueva) {
            clave = txtClave.getPassword();
        }
        txtClave.setText("");
        txtConfirmar.setText("");
    }
}
