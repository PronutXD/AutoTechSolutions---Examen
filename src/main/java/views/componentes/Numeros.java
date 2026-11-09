package views.componentes;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Lectura de números escritos en los formularios (null si el texto no es válido). */
public final class Numeros {

    private Numeros() { }

    /** Entero sin signo decimal, o null. */
    public static Integer entero(String texto) {
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Número con como máximo "decimales" cifras decimales, o null si no es un número o tiene más. */
    public static BigDecimal decimal(String texto, int decimales) {
        try {
            return new BigDecimal(texto.trim()).setScale(decimales, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException | ArithmeticException e) {
            return null;
        }
    }
}
