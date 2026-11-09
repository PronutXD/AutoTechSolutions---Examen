package views.componentes;

/** Acomoda un JSON de una línea (como el que guarda Auditoria) en varias líneas para leerlo. */
public final class Json {

    private Json() { }

    /** Un campo por línea, con sangría. Respeta las comas y llaves que van dentro de textos entre comillas. */
    public static String ordenar(String json) {
        if (json == null || json.isBlank()) {
            return "(sin valor)";
        }
        StringBuilder sb = new StringBuilder();
        boolean enTexto = false;
        boolean escapado = false;
        int nivel = 0;
        for (char c : json.toCharArray()) {
            if (escapado) {
                sb.append(c);
                escapado = false;
            } else if (enTexto) {
                sb.append(c);
                if (c == (char) 92) {          // barra invertida
                    escapado = true;
                } else if (c == (char) 34) {   // comilla de cierre
                    enTexto = false;
                }
            } else if (c == (char) 34) {
                enTexto = true;
                sb.append(c);
            } else if (c == (char) 123) {      // {
                nivel++;
                sb.append(c).append("\n").append("  ".repeat(nivel));
            } else if (c == (char) 125) {      // }
                nivel = Math.max(0, nivel - 1);
                sb.append("\n").append("  ".repeat(nivel)).append(c);
            } else if (c == (char) 44) {       // ,
                sb.append(c).append("\n").append("  ".repeat(nivel));
            } else if (c == (char) 58) {       // :
                sb.append(": ");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
