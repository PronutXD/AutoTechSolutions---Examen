package views.componentes;

import java.awt.Component;
import javax.swing.JOptionPane;

/** Cuadros de mensaje sencillos, para no repetir JOptionPane en cada controlador. */
public final class Mensajes {

    private Mensajes() {
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "AutoTech", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void advertencia(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String titulo, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
