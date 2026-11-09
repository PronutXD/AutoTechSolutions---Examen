package views.formularios;

import java.awt.Frame;
import java.math.BigDecimal;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import model.Servicio;
import views.componentes.Numeros;

/** Formulario de servicio: sirve para crear (existente = null) y para editar. */
public class DlgServicio extends DlgFormulario {

    private final Servicio existente;
    private Servicio resultado;

    private final JTextField txtNombre = campoTexto();
    private final JComboBox<String> cmbNivel = new JComboBox<>(Servicio.NIVELES);
    private final JTextField txtManoObra = campoTexto();
    private final JTextField txtHoras = campoTexto();
    private final JTextField txtGarantia = campoTexto();

    public DlgServicio(Frame propietario, Servicio existente) {
        super(propietario,
                existente == null ? "Nuevo servicio" : "Editar servicio",
                "Los cambios de precio no afectan órdenes ya registradas");
        this.existente = existente;

        agregarCampo("Nombre", txtNombre, true);
        agregarCampo("Nivel", cmbNivel, true);
        agregarCampo("Mano de obra ($)", txtManoObra, true);
        agregarCampo("Horas estimadas", txtHoras, true);
        agregarCampo("Garantía (días)", txtGarantia, true);

        if (existente != null) {
            txtNombre.setText(existente.nombre());
            cmbNivel.setSelectedIndex(existente.nivel() - 1);
            txtManoObra.setText(existente.manoObra().toPlainString());
            txtHoras.setText(existente.horasEstimadas().toPlainString());
            txtGarantia.setText(String.valueOf(existente.garantiaDias()));
        } else {
            txtGarantia.setText("0");
        }
        armar();
    }

    public Servicio getServicio() {
        return resultado;
    }

    @Override
    protected String validar() {
        if (txtNombre.getText().isBlank()) {
            return "El nombre es obligatorio.";
        }
        BigDecimal mano = Numeros.decimal(txtManoObra.getText(), 2);
        if (mano == null || mano.signum() < 0) {
            return "La mano de obra debe ser un monto de 0 o más.";
        }
        BigDecimal horas = Numeros.decimal(txtHoras.getText(), 1);
        if (horas == null || horas.signum() <= 0) {
            return "Las horas estimadas deben ser mayores que 0.";
        }
        Integer garantia = Numeros.entero(txtGarantia.getText());
        if (garantia == null || garantia < 0) {
            return "La garantía debe ser un número de días de 0 o más.";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        resultado = new Servicio(existente == null ? 0 : existente.id(),
                existente == null ? null : existente.codigo(),
                txtNombre.getText().trim(),
                cmbNivel.getSelectedIndex() + 1,
                Numeros.decimal(txtManoObra.getText(), 2),
                Numeros.decimal(txtHoras.getText(), 1),
                Numeros.entero(txtGarantia.getText()),
                existente == null || existente.activo());
    }
}
