package views.formularios;

import java.awt.Frame;
import java.util.Arrays;
import javax.swing.JPasswordField;

/** Pide la contraseña nueva de una cuenta (la restablece el administrador). */
public class DlgClave extends DlgFormulario {

    private final JPasswordField txtClave = new JPasswordField(24);
    private final JPasswordField txtConfirmar = new JPasswordField(24);
    private char[] clave;

    public DlgClave(Frame propietario, String usuario) {
        super(propietario, "Restablecer contraseña", "Cuenta: " + usuario);
        agregarCampo("Contraseña nueva", txtClave, true);
        agregarCampo("Confirmar contraseña", txtConfirmar, true);
        armar();
    }

    /** El controlador debe borrarla con Arrays.fill al terminar. */
    public char[] getClave() {
        return clave;
    }

    @Override
    protected String validar() {
        char[] c1 = txtClave.getPassword();
        char[] c2 = txtConfirmar.getPassword();
        boolean igual = Arrays.equals(c1, c2);
        int largo = c1.length;
        Arrays.fill(c1, '\0');
        Arrays.fill(c2, '\0');
        if (largo < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        return igual ? null : "Las contraseñas no coinciden.";
    }

    @Override
    protected void aplicar() {
        clave = txtClave.getPassword();
        txtClave.setText("");
        txtConfirmar.setText("");
    }
}
