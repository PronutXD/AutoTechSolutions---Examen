package model;

/** Usuario que inició sesión en la aplicación. */
public final class Sesion {

    public record Usuario(int id, String usuario, String rol, Integer idMecanico, boolean debeCambiarClave) { }

    private static Usuario actual;

    private Sesion() { }

    public static void iniciar(Usuario u) { actual = u; }
    public static void cerrar() { actual = null; }
    public static Usuario actual() { return actual; }
}