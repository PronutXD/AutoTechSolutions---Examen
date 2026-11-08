package views.paneles;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import views.componentes.PnlTabla;

/**
 * Pantalla "Clientes y vehículos": dos pestañas con su tabla, buscador y filtros.
 */
public class PnlClientes extends PnlSeccion {

    private final PnlTabla tblClientes = new PnlTabla(
            "Documento", "Nombres", "Apellidos", "Teléfono", "Correo", "Vehículos", "Estado");

    private final PnlTabla tblVehiculos = new PnlTabla(
            "Placa", "VIN / Chasis", "Marca", "Modelo", "Año", "Color", "Kilometraje",
            "Combustible", "Propietario");

    public PnlClientes() {
        super("Clientes y vehículos",
                "Registro de clientes y de los vehículos de cada uno");

        configurarClientes();
        configurarVehiculos();

        JTabbedPane tabs = crearPestanas();
        agregarPestana(tabs, "Clientes", tblClientes);
        agregarPestana(tabs, "Vehículos", tblVehiculos);
    }

    private void configurarClientes() {
        tblClientes.setPlaceholder("Buscar por nombre, documento o teléfono...");
        tblClientes.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        
        var btnNuevoCliente = tblClientes.addAccion("Nuevo cliente", true, false);
        tblClientes.addAccion("Editar", false, true);
        tblClientes.addAccion("Desactivar", false, true);
        
        tblClientes.setAnchos(110, 160, 160, 110, 210, 80, 90);
        tblClientes.alinearDerecha(5);

        btnNuevoCliente.addActionListener(e -> {
            try {
                JTextField txtDocumento = new JTextField(15);
                JTextField txtNombres = new JTextField(15);
                JTextField txtApellidos = new JTextField(15);
                JTextField txtTelefono = new JTextField(15);
                JTextField txtCorreo = new JTextField(15);

                JPanel panelFormulario = new JPanel();
                panelFormulario.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
                panelFormulario.add(new JLabel("Documento:"));
                panelFormulario.add(txtDocumento);
                panelFormulario.add(new JLabel("Nombres:"));
                panelFormulario.add(txtNombres);
                panelFormulario.add(new JLabel("Apellidos:"));
                panelFormulario.add(txtApellidos);
                panelFormulario.add(new JLabel("Teléfono:"));
                panelFormulario.add(txtTelefono);
                panelFormulario.add(new JLabel("Correo:"));
                panelFormulario.add(txtCorreo);

                int resultado = JOptionPane.showConfirmDialog(
                        this, panelFormulario, "Registrar Nuevo Cliente", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (resultado == JOptionPane.OK_OPTION) {
                    String documento = txtDocumento.getText().trim();
                    String nombres = txtNombres.getText().trim();
                    String apellidos = txtApellidos.getText().trim();
                    String telefono = txtTelefono.getText().trim();
                    String correo = txtCorreo.getText().trim();

                    if (documento.isEmpty() || nombres.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "El documento y los nombres son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // 1. Creamos el objeto Modelo
                    modelo.Cliente nuevoCliente = new modelo.Cliente(documento, nombres, apellidos, telefono, correo, "Activo");

                    // 2. Instanciamos el DAO y guardamos en la base de datos
                    Dao.ClienteDao dao = new Dao.ClienteDao();
                    boolean registrado = dao.insertar(nuevoCliente);

                    if (registrado) {
                        // 3. Si se guardó con éxito en la BD, lo reflejamos en la tabla visual
                        tblClientes.getModelo().addRow(new Object[]{
                            documento, nombres, apellidos, telefono, correo, "0", "Activo"
                        });
                        JOptionPane.showMessageDialog(this, "¡Cliente registrado en la base de datos con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo guardar en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void configurarVehiculos() {
        tblVehiculos.setPlaceholder("Buscar por placa, VIN, marca o propietario...");
        tblVehiculos.addFiltro("Combustible", "Todos", "Gasolina", "Diésel", "Híbrido", "Eléctrico", "Gas");
        
        var btnNuevoVehiculo = tblVehiculos.addAccion("Nuevo vehículo", true, false);
        tblVehiculos.addAccion("Editar", false, true);
        
        tblVehiculos.setAnchos(90, 170, 100, 110, 60, 90, 100, 100, 170);
        tblVehiculos.alinearDerecha(4, 6);

        // Acción para el botón "Nuevo vehículo"
        btnNuevoVehiculo.addActionListener(e -> {
            try {
                JTextField txtPlaca = new JTextField(15);
                JTextField txtVin = new JTextField(15);
                JTextField txtMarca = new JTextField(15);
                JTextField txtModelo = new JTextField(15);
                JTextField txtAnio = new JTextField(15);
                JTextField txtColor = new JTextField(15);
                JTextField txtKm = new JTextField(15);
                JTextField txtCombustible = new JTextField(15);
                JTextField txtPropietario = new JTextField(15);

                JPanel panelFormulario = new JPanel();
                panelFormulario.setLayout(new java.awt.GridLayout(0, 1, 3, 3));
                panelFormulario.add(new JLabel("Placa:"));
                panelFormulario.add(txtPlaca);
                panelFormulario.add(new JLabel("VIN / Chasis:"));
                panelFormulario.add(txtVin);
                panelFormulario.add(new JLabel("Marca:"));
                panelFormulario.add(txtMarca);
                panelFormulario.add(new JLabel("Modelo:"));
                panelFormulario.add(txtModelo);
                panelFormulario.add(new JLabel("Año:"));
                panelFormulario.add(txtAnio);
                panelFormulario.add(new JLabel("Color:"));
                panelFormulario.add(txtColor);
                panelFormulario.add(new JLabel("Kilometraje:"));
                panelFormulario.add(txtKm);
                panelFormulario.add(new JLabel("Combustible:"));
                panelFormulario.add(txtCombustible);
                panelFormulario.add(new JLabel("Propietario:"));
                panelFormulario.add(txtPropietario);

                JScrollPane scrollPane = new JScrollPane(panelFormulario);
                scrollPane.setPreferredSize(new java.awt.Dimension(320, 350));

                int resultado = JOptionPane.showConfirmDialog(
                        this, scrollPane, "Registrar Nuevo Vehículo", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (resultado == JOptionPane.OK_OPTION) {
                    String placa = txtPlaca.getText().trim();
                    String marca = txtMarca.getText().trim();

                    if (placa.isEmpty() || marca.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "La placa y la marca son obligatorias.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    tblVehiculos.getModelo().addRow(new Object[]{
                        placa, txtVin.getText().trim(), marca, txtModelo.getText().trim(),
                        txtAnio.getText().trim(), txtColor.getText().trim(), txtKm.getText().trim(),
                        txtCombustible.getText().trim(), txtPropietario.getText().trim()
                    });

                    JOptionPane.showMessageDialog(this, "¡Vehículo registrado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar el vehículo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}