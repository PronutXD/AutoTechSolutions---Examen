package views.formularios.inventario;

import java.awt.Frame;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import model.Repuesto;
import model.inventario.Proveedor;
import views.componentes.Numeros;
import views.formularios.DlgFormulario;

/** Formulario para registrar una entrada de almacén (compra de un repuesto a un proveedor). */
public class DlgEntrada extends DlgFormulario {

    private static final BigDecimal MAXIMO = new BigDecimal("9999999999.99");

    private final JTextField txtCantidad = campoTexto();
    private final JTextField txtCosto = campoTexto();
    private final JComboBox<Proveedor> cmbProveedor;

    /** @param proveedores proveedores que se pueden elegir (no vacío) */
    public DlgEntrada(Frame propietario, Repuesto repuesto, List<Proveedor> proveedores) {
        super(propietario, "Registrar entrada",
                repuesto.nombre() + " · stock actual " + repuesto.stock());
        cmbProveedor = new JComboBox<>(proveedores.toArray(new Proveedor[0]));
        for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
            if (cmbProveedor.getItemAt(i).getId() == repuesto.idProveedor()) {
                cmbProveedor.setSelectedIndex(i);
                break;
            }
        }
        txtCosto.setText(repuesto.costo().toPlainString());

        agregarCampo("Cantidad", txtCantidad, true);
        agregarCampo("Costo unitario", txtCosto, true);
        agregarCampo("Proveedor", cmbProveedor, true);
        armar();
    }

    public int getCantidad() {
        return Numeros.entero(txtCantidad.getText());
    }

    public BigDecimal getCostoUnitario() {
        return Numeros.decimal(txtCosto.getText(), 2);
    }

    public int getIdProveedor() {
        return ((Proveedor) cmbProveedor.getSelectedItem()).getId();
    }

    @Override
    protected String validar() {
        Integer cantidad = Numeros.entero(txtCantidad.getText());
        if (cantidad == null) {
            return "La cantidad debe ser un número entero.";
        }
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor que cero.";
        }
        BigDecimal costo = Numeros.decimal(txtCosto.getText(), 2);
        if (costo == null) {
            return "El costo unitario debe ser un número con hasta 2 decimales (por ejemplo 150.00).";
        }
        if (costo.signum() < 0 || costo.compareTo(MAXIMO) > 0) {
            return "El costo unitario no es válido.";
        }
        if (cmbProveedor.getSelectedItem() == null) {
            return "Elige el proveedor.";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        // Nada que volcar: el controlador lee los getters.
    }
}
