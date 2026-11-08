package views.paneles;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import views.componentes.PnlTabla;

/**
 * Pantalla de Inventario: control de productos y proveedores.
 */
public class PnlInventario extends PnlSeccion {

    private final PnlTabla tblProductos = new PnlTabla(
            "Código", "Descripción", "Categoría", "Stock", "Precio Compra", "Precio Venta", "Ubicación");

    private final PnlTabla tblProveedores = new PnlTabla(
            "ID", "Nombre / Empresa", "Contacto", "Teléfono", "Correo", "Dirección");

    public PnlInventario() {
        super("Inventario y Proveedores",
                "Gestión de stock de repuestos, productos y proveedores");

        configurarProductos();
        configurarProveedores();

        JTabbedPane tabs = crearPestanas();
        agregarPestana(tabs, "Productos", tblProductos);
        agregarPestana(tabs, "Proveedores", tblProveedores);
    }

    private void configurarProductos() {
        tblProductos.setPlaceholder("Buscar por código, descripción o categoría...");
        tblProductos.addFiltro("Categoría", "Todos", "Repuestos", "Lubricantes", "Accesorios", "Herramientas");
        
        var btnNuevoRegistro = tblProductos.addAccion("Nuevo registro", true, false);
        tblProductos.addAccion("Editar", false, true);
        tblProductos.addAccion("Ajustar Stock", false, true);
        
        tblProductos.setAnchos(90, 200, 120, 70, 100, 100, 110);
        tblProductos.alinearDerecha(3, 4, 5);

        // Acción para el botón "Nuevo registro" (Producto)
        btnNuevoRegistro.addActionListener(e -> {
            try {
                JTextField txtCodigo = new JTextField(15);
                JTextField txtDescripcion = new JTextField(15);
                JTextField txtCategoria = new JTextField(15);
                JTextField txtStock = new JTextField(15);
                JTextField txtPCompra = new JTextField(15);
                JTextField txtPVenta = new JTextField(15);
                JTextField txtUbicacion = new JTextField(15);

                JPanel panelFormulario = new JPanel();
                panelFormulario.setLayout(new java.awt.GridLayout(0, 1, 3, 3));
                panelFormulario.add(new JLabel("Código:"));
                panelFormulario.add(txtCodigo);
                panelFormulario.add(new JLabel("Descripción:"));
                panelFormulario.add(txtDescripcion);
                panelFormulario.add(new JLabel("Categoría:"));
                panelFormulario.add(txtCategoria);
                panelFormulario.add(new JLabel("Stock Inicial:"));
                panelFormulario.add(txtStock);
                panelFormulario.add(new JLabel("Precio Compra:"));
                panelFormulario.add(txtPCompra);
                panelFormulario.add(new JLabel("Precio Venta:"));
                panelFormulario.add(txtPVenta);
                panelFormulario.add(new JLabel("Ubicación:"));
                panelFormulario.add(txtUbicacion);

                JScrollPane scrollPane = new JScrollPane(panelFormulario);
                scrollPane.setPreferredSize(new java.awt.Dimension(300, 300));

                int resultado = JOptionPane.showConfirmDialog(
                        this, scrollPane, "Nuevo Producto en Inventario", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (resultado == JOptionPane.OK_OPTION) {
                    String codigo = txtCodigo.getText().trim();
                    String descripcion = txtDescripcion.getText().trim();

                    if (codigo.isEmpty() || descripcion.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "El código y la descripción son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    tblProductos.getModelo().addRow(new Object[]{
                        codigo, descripcion, txtCategoria.getText().trim(),
                        txtStock.getText().trim().isEmpty() ? "0" : txtStock.getText().trim(),
                        txtPCompra.getText().trim().isEmpty() ? "0.00" : txtPCompra.getText().trim(),
                        txtPVenta.getText().trim().isEmpty() ? "0.00" : txtPVenta.getText().trim(),
                        txtUbicacion.getText().trim()
                    });

                    JOptionPane.showMessageDialog(this, "¡Producto registrado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar el producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void configurarProveedores() {
        tblProveedores.setPlaceholder("Buscar proveedor por nombre o teléfono...");
        
        var btnNuevoProveedor = tblProveedores.addAccion("Nuevo proveedor", true, false);
        tblProveedores.addAccion("Editar", false, true);
        
        tblProveedores.setAnchos(60, 180, 140, 100, 180, 200);

        // Acción para el botón "Nuevo proveedor"
        btnNuevoProveedor.addActionListener(e -> {
            try {
                JTextField txtId = new JTextField(15);
                JTextField txtNombre = new JTextField(15);
                JTextField txtContacto = new JTextField(15);
                JTextField txtTelefono = new JTextField(15);
                JTextField txtCorreo = new JTextField(15);
                JTextField txtDireccion = new JTextField(15);

                JPanel panelFormulario = new JPanel();
                panelFormulario.setLayout(new java.awt.GridLayout(0, 1, 3, 3));
                panelFormulario.add(new JLabel("ID / RTN:"));
                panelFormulario.add(txtId);
                panelFormulario.add(new JLabel("Nombre / Empresa:"));
                panelFormulario.add(txtNombre);
                panelFormulario.add(new JLabel("Persona de Contacto:"));
                panelFormulario.add(txtContacto);
                panelFormulario.add(new JLabel("Teléfono:"));
                panelFormulario.add(txtTelefono);
                panelFormulario.add(new JLabel("Correo:"));
                panelFormulario.add(txtCorreo);
                panelFormulario.add(new JLabel("Dirección:"));
                panelFormulario.add(txtDireccion);

                JScrollPane scrollPane = new JScrollPane(panelFormulario);
                scrollPane.setPreferredSize(new java.awt.Dimension(300, 280));

                int resultado = JOptionPane.showConfirmDialog(
                        this, scrollPane, "Registrar Nuevo Proveedor", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (resultado == JOptionPane.OK_OPTION) {
                    String nombre = txtNombre.getText().trim();

                    if (nombre.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "El nombre del proveedor es obligatorio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    tblProveedores.getModelo().addRow(new Object[]{
                        txtId.getText().trim(), nombre, txtContacto.getText().trim(),
                        txtTelefono.getText().trim(), txtCorreo.getText().trim(), txtDireccion.getText().trim()
                    });

                    JOptionPane.showMessageDialog(this, "¡Proveedor registrado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar el proveedor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}