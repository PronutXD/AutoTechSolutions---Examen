package controller;

import dao.ErroresSQL;
import dao.UsuarioDAO;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * TEMPORAL: crea el usuario administrador, o le cambia la contraseña si ya existe.
 * Se ejecuta aparte (clic derecho > Run File) y se puede borrar cuando haya una
 * pantalla de Usuarios que cumpla esa función.
 *
 * Hace falta porque las contraseñas que había en la base estaban cifradas con
 * SHA-512 dentro de SQL Server y ahora el login espera hashes bcrypt.
 */
public class CrearAdmin {

    public static void main(String[] args) {
        JTextField txtUsuario = new JTextField("admin");
        JPasswordField txtClave = new JPasswordField();
        Object[] campos = {"Usuario:", txtUsuario, "Contraseña:", txtClave};

        if (JOptionPane.showConfirmDialog(null, campos, "Crear administrador",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        String usuario = txtUsuario.getText().trim();
        char[] clave = txtClave.getPassword();
        if (usuario.isEmpty() || clave.length < 4) {
            JOptionPane.showMessageDialog(null, "Escribe un usuario y una contraseña de al menos 4 caracteres.");
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        try {
            if (dao.cambiarClave(usuario, clave)) {
                JOptionPane.showMessageDialog(null, "Se cambió la contraseña de «" + usuario + "».");
            } else {
                dao.crear(usuario, clave, "Administrador", null);
                JOptionPane.showMessageDialog(null, "Administrador «" + usuario + "» creado.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ErroresSQL.mensaje(ex, "guardar el usuario", "usuario"),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(clave, '\0');
        }
    }
}
