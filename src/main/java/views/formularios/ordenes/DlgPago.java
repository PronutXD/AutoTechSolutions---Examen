package views.formularios.ordenes;

import java.awt.Frame;
import java.math.BigDecimal;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import model.ordenes.Pago;
import views.componentes.Formato;
import views.formularios.DlgFormulario;

/** Formulario para registrar un pago de la orden. */
public class DlgPago extends DlgFormulario {

    private final BigDecimal saldo;
    private final JTextField txtMonto = campoTexto();
    private final JComboBox<String> cmbMetodo = new JComboBox<>(Pago.METODOS);
    private final JTextField txtReferencia = campoTexto();

    public DlgPago(Frame propietario, BigDecimal saldoPendiente) {
        super(propietario, "Registrar pago", "Saldo pendiente: " + Formato.dinero(saldoPendiente));
        this.saldo = saldoPendiente;
        txtMonto.setText(saldoPendiente.toPlainString());
        agregarCampo("Monto", txtMonto, true);
        agregarCampo("Método de pago", cmbMetodo, true);
        agregarCampo("Referencia", txtReferencia, false);
        armar();
    }

    public BigDecimal getMonto() {
        return new BigDecimal(txtMonto.getText().trim());
    }

    public String getMetodo() {
        return (String) cmbMetodo.getSelectedItem();
    }

    public String getReferencia() {
        return txtReferencia.getText().trim();
    }

    @Override
    protected String validar() {
        BigDecimal monto;
        try {
            monto = new BigDecimal(txtMonto.getText().trim());
        } catch (NumberFormatException e) {
            return "El monto debe ser un número (por ejemplo 500.00).";
        }
        if (monto.signum() <= 0) {
            return "El monto debe ser mayor que cero.";
        }
        if (monto.compareTo(saldo) > 0) {
            return "El monto no puede ser mayor al saldo pendiente (" + Formato.dinero(saldo) + ").";
        }
        if (txtReferencia.getText().trim().length() > 60) {
            return "La referencia no puede pasar de 60 caracteres.";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        // Nada que volcar: el controlador lee los getters.
    }
}
