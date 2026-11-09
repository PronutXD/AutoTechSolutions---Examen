package controller;

import dao.UsuarioDAO;
import java.sql.SQLException;
import java.util.Optional;
import model.Sesion;
import model.Sesion.Usuario;

/** Lógica del inicio de sesión: la ventana solo pide los datos y muestra el resultado. */
public class LoginController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * @return el usuario si las credenciales son correctas y la cuenta está activa; vacío si no.
     * @throws SQLException si no hay conexión con la base de datos
     */
    public Optional<Usuario> autenticar(String usuario, char[] clave) throws SQLException {
        Optional<Usuario> u = usuarioDAO.validar(usuario, clave);
        if (u.isPresent()) {
            // Primero la sesión, para que el cambio de ultimoAcceso quede atribuido a este usuario.
            Sesion.iniciar(u.get());
            usuarioDAO.registrarAcceso(u.get().id());
        }
        return u;
    }
}
