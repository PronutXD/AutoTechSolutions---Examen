package views.formularios.ordenes;

import java.awt.Frame;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import model.Repuesto;
import views.formularios.DlgFormulario;

/**
 * Formulario para agregar un servicio o un repuesto a una orden.
 * El precio no se escribe: se toma del catálogo al guardar.
 */
public class DlgLineaOrden extends DlgFormulario {

    private final boolean repuesto;
    private final JComboBox<Object> cmbItem = new JComboBox<>();
    private final JTextField txtCantidad = campoTexto();

    /** @param opciones lista de Servicio o de Repuesto, según el valor de repuesto */
    public DlgLineaOrden(Frame propietario, boolean repuesto, List<?> opciones) {
        super(propietario, repuesto ? "Agregar repuesto" : "Agregar servicio",
                repuesto ? "Se descuenta del inventario al guardar" : "Se cobra la mano de obra del catálogo");
        this.repuesto = repuesto;
        for (Object o : opciones) {
            cmbItem.addItem(o);
        }
        txtCantidad.setText("1");
        agregarCampo(repuesto ? "Repuesto" : "Servicio", cmbItem, true);
        agregarCampo("Cantidad", txtCantidad, true);
        armar();
    }

    /** El Servicio o el Repuesto elegido. */
    public Object getSeleccion() {
        return cmbItem.getSelectedItem();
    }

    public int getCantidad() {
        return Integer.parseInt(txtCantidad.getText().trim());
    }

    @Override
    protected String validar() {
        if (cmbItem.getSelectedItem() == null) {
            return repuesto ? "No hay repuestos disponibles." : "No hay servicios disponibles.";
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException e) {
            return "La cantidad debe ser un número entero.";
        }
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor que cero.";
        }
        if (repuesto && cmbItem.getSelectedItem() instanceof Repuesto r && cantidad > r.stock()) {
            return "Stock insuficiente: solo hay " + r.stock() + " de «" + r.nombre() + "».";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        // Nada que volcar: el controlador lee getSeleccion() y getCantidad().
    }
}
