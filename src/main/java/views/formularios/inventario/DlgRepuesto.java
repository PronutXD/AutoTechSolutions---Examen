package views.formularios.inventario;

import java.awt.Frame;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import model.Repuesto;
import model.inventario.Proveedor;
import views.componentes.Numeros;
import views.formularios.DlgFormulario;

/**
 * Formulario de repuesto: sirve para crear (existente = null) y para editar.
 * El stock no se escribe aquí: el repuesto nuevo empieza en 0 y el stock sube con "Registrar entrada".
 */
public class DlgRepuesto extends DlgFormulario {

    private static final BigDecimal MAXIMO = new BigDecimal("9999999999.99");

    private final Repuesto existente;
    private Repuesto repuesto;

    private final JTextField txtNombre = campoTexto();
    private final JComboBox<String> cmbCategoria = new JComboBox<>(Repuesto.CATEGORIAS);
    private final JComboBox<Proveedor> cmbProveedor;
    private final JTextField txtStockMinimo = campoTexto();
    private final JTextField txtCosto = campoTexto();
    private final JTextField txtPrecio = campoTexto();

    /** @param proveedores proveedores que se pueden elegir (no vacío) */
    public DlgRepuesto(Frame propietario, Repuesto existente, List<Proveedor> proveedores) {
        super(propietario,
                existente == null ? "Nuevo repuesto" : "Editar repuesto",
                "Los campos con * son obligatorios");
        this.existente = existente;

        cmbProveedor = new JComboBox<>(proveedores.toArray(new Proveedor[0]));

        agregarCampo("Nombre", txtNombre, true);
        agregarCampo("Categoría", cmbCategoria, true);
        agregarCampo("Proveedor", cmbProveedor, true);
        agregarCampo("Stock mínimo", txtStockMinimo, true);
        agregarCampo("Costo", txtCosto, true);
        agregarCampo("Precio de venta", txtPrecio, true);

        if (existente != null) {
            txtNombre.setText(existente.nombre());
            cmbCategoria.setSelectedItem(existente.categoria());
            for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
                if (cmbProveedor.getItemAt(i).getId() == existente.idProveedor()) {
                    cmbProveedor.setSelectedIndex(i);
                    break;
                }
            }
            txtStockMinimo.setText(String.valueOf(existente.stockMinimo()));
            txtCosto.setText(existente.costo().toPlainString());
            txtPrecio.setText(existente.precioVenta().toPlainString());
            agregarCampo("Stock actual", new JLabel(existente.stock() + "  (cambia con \"Registrar entrada\")"), false);
        } else {
            txtStockMinimo.setText("0");
        }
        armar();
    }

    /** El repuesto ya lleno (conserva id, estado y version del original al editar). */
    public Repuesto getRepuesto() {
        return repuesto;
    }

    @Override
    protected String validar() {
        if (txtNombre.getText().isBlank()) {
            return "El nombre es obligatorio.";
        }
        if (cmbProveedor.getSelectedItem() == null) {
            return "Elige el proveedor del repuesto.";
        }
        Integer minimo = Numeros.entero(txtStockMinimo.getText());
        if (minimo == null) {
            return "El stock mínimo debe ser un número entero.";
        }
        if (minimo < 0) {
            return "El stock mínimo no puede ser negativo.";
        }
        BigDecimal costo = Numeros.decimal(txtCosto.getText(), 2);
        if (costo == null) {
            return "El costo debe ser un número con hasta 2 decimales (por ejemplo 150.00).";
        }
        if (costo.signum() < 0 || costo.compareTo(MAXIMO) > 0) {
            return "El costo no es válido.";
        }
        BigDecimal precio = Numeros.decimal(txtPrecio.getText(), 2);
        if (precio == null) {
            return "El precio de venta debe ser un número con hasta 2 decimales (por ejemplo 210.00).";
        }
        if (precio.signum() < 0 || precio.compareTo(MAXIMO) > 0) {
            return "El precio de venta no es válido.";
        }
        return null;
    }

    @Override
    protected void aplicar() {
        Proveedor proveedor = (Proveedor) cmbProveedor.getSelectedItem();
        repuesto = new Repuesto(
                existente == null ? 0 : existente.id(),
                existente == null ? null : existente.codigo(),
                txtNombre.getText().trim(),
                (String) cmbCategoria.getSelectedItem(),
                existente == null ? 0 : existente.stock(),
                Numeros.entero(txtStockMinimo.getText()),
                Numeros.decimal(txtCosto.getText(), 2),
                Numeros.decimal(txtPrecio.getText(), 2),
                existente == null || existente.activo(),
                proveedor.getId(),
                proveedor.getNombre(),
                existente == null ? null : existente.version());
    }
}
