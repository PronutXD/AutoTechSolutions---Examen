package views.paneles;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import views.componentes.PnlTabla;

/**
 * Pantalla de Catálogos: servicios, mecánicos y usuarios del sistema.
 */
public class PnlCatalogos extends PnlSeccion {

    private final PnlTabla tblServicios = new PnlTabla(
            "Código", "Nombre del Servicio", "Categoría", "Precio Base", "Tiempo Estimado");

    private final PnlTabla tblMecanicos = new PnlTabla(
            "ID", "Nombres y Apellidos", "Especialidad", "Teléfono", "Estado");

    private final PnlTabla tblUsuarios = new PnlTabla(
            "Usuario", "Nombre Completo", "Rol", "Correo", "Estado");

    public PnlCatalogos() {
        super("Catálogos y Configuración",
                "Gestión de servicios mecánicos, personal técnico y usuarios del sistema");

        configurarServicios();
        configurarMecanicos();
        configurarUsuarios();

        JTabbedPane tabs = crearPestanas();
        agregarPestana(tabs, "Servicios", tblServicios);
        agregarPestana(tabs, "Mecánicos", tblMecanicos);
        agregarPestana(tabs, "Usuarios", tblUsuarios);
    }

    private void configurarServicios() {
        tblServicios.setPlaceholder("Buscar servicio por nombre o categoría...");
        
        var btnNuevoServicio = tblServicios.addAccion("Nuevo servicio mecánico", true, false);
        tblServicios.addAccion("Editar", false, true);
        
        tblServicios.setAnchos(90, 220, 140, 100, 120);
        tblServicios.alinearDerecha(3);

        btnNuevoServicio.addActionListener(e -> {
            try {
                JTextField txtCodigo = new JTextField(15);
                JTextField txtNombre = new JTextField(15);
                JTextField txtCategoria = new JTextField(15);
                JTextField txtPrecio = new JTextField("0.00", 15);
                JTextField txtTiempo = new JTextField(15);

                JPanel panelFormulario = new JPanel();
                panelFormulario.setLayout(new java.awt.GridLayout(0, 1, 3, 3));
                panelFormulario.add(new JLabel("Código:"));
                panelFormulario.add(txtCodigo);
                panelFormulario.add(new JLabel("Nombre del Servicio:"));
                panelFormulario.add(txtNombre);
                panelFormulario.add(new JLabel("Categoría:"));
                panelFormulario.add(txtCategoria);
                panelFormulario.add(new JLabel("Precio Base:"));
                panelFormulario.add(txtPrecio);
                panelFormulario.add(new JLabel("Tiempo Estimado:"));
                panelFormulario.add(txtTiempo);

                int resultado = JOptionPane.showConfirmDialog(
                        this, panelFormulario, "Registrar Nuevo Servicio Mecánico", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (resultado == JOptionPane.OK_OPTION) {
                    String nombre = txtNombre.getText().trim();
                    if (nombre.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "El nombre del servicio es obligatorio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    tblServicios.getModelo().addRow(new Object[]{
                        txtCodigo.getText().trim(), nombre, txtCategoria.getText().trim(),
                        txtPrecio.getText().trim(), txtTiempo.getText().trim()
                    });
                    JOptionPane.showMessageDialog(this, "¡Servicio registrado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar el servicio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void configurarMecanicos() {
        tblMecanicos.setPlaceholder("Buscar mecánico por nombre o especialidad...");
        
        var btnNuevoMecanico = tblMecanicos.addAccion("Nuevo mecánico", true, false);
        tblMecanicos.addAccion("Editar", false, true);
        
        tblMecanicos.setAnchos(70, 200, 150, 110, 100);

        btnNuevoMecanico.addActionListener(e -> {
            try {
                JTextField txtId = new JTextField(15);
                JTextField txtNombre = new JTextField(15);
                JTextField txtEspecialidad = new JTextField(15);
                JTextField txtTelefono = new JTextField(15);
                JTextField txtEstado = new JTextField("Activo", 15);

                JPanel panelFormulario = new JPanel();
                panelFormulario.setLayout(new java.awt.GridLayout(0, 1, 3, 3));
                panelFormulario.add(new JLabel("ID / Documento:"));
                panelFormulario.add(txtId);
                panelFormulario.add(new JLabel("Nombres y Apellidos:"));
                panelFormulario.add(txtNombre);
                panelFormulario.add(new JLabel("Especialidad:"));
                panelFormulario.add(txtEspecialidad);
                panelFormulario.add(new JLabel("Teléfono:"));
                panelFormulario.add(txtTelefono);
                panelFormulario.add(new JLabel("Estado:"));
                panelFormulario.add(txtEstado);

                int resultado = JOptionPane.showConfirmDialog(
                        this, panelFormulario, "Registrar Nuevo Mecánico", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (resultado == JOptionPane.OK_OPTION) {
                    String nombre = txtNombre.getText().trim();
                    if (nombre.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "El nombre del mecánico es obligatorio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    tblMecanicos.getModelo().addRow(new Object[]{
                        txtId.getText().trim(), nombre, txtEspecialidad.getText().trim(),
                        txtTelefono.getText().trim(), txtEstado.getText().trim()
                    });
                    JOptionPane.showMessageDialog(this, "¡Mecánico registrado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar el mecánico: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void configurarUsuarios() {
        tblUsuarios.setPlaceholder("Buscar usuario por nombre o rol...");
        
        var btnNuevoUsuario = tblUsuarios.addAccion("Nuevo usuario", true, false);
        tblUsuarios.addAccion("Editar", false, true);
        
        tblUsuarios.setAnchos(110, 180, 110, 180, 100);

        btnNuevoUsuario.addActionListener(e -> {
            try {
                JTextField txtUser = new JTextField(15);
                JTextField txtNombre = new JTextField(15);
                JTextField txtRol = new JTextField(15);
                JTextField txtCorreo = new JTextField(15);
                JTextField txtEstado = new JTextField("Activo", 15);

                JPanel panelFormulario = new JPanel();
                panelFormulario.setLayout(new java.awt.GridLayout(0, 1, 3, 3));
                panelFormulario.add(new JLabel("Nombre de Usuario:"));
                panelFormulario.add(txtUser);
                panelFormulario.add(new JLabel("Nombre Completo:"));
                panelFormulario.add(txtNombre);
                panelFormulario.add(new JLabel("Rol (Ej. Administrador, Recepción):"));
                panelFormulario.add(txtRol);
                panelFormulario.add(new JLabel("Correo Electrónico:"));
                panelFormulario.add(txtCorreo);
                panelFormulario.add(new JLabel("Estado:"));
                panelFormulario.add(txtEstado);

                int resultado = JOptionPane.showConfirmDialog(
                        this, panelFormulario, "Registrar Nuevo Usuario", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (resultado == JOptionPane.OK_OPTION) {
                    String user = txtUser.getText().trim();
                    if (user.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "El nombre de usuario es obligatorio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    tblUsuarios.getModelo().addRow(new Object[]{
                        user, txtNombre.getText().trim(), txtRol.getText().trim(),
                        txtCorreo.getText().trim(), txtEstado.getText().trim()
                    });
                    JOptionPane.showMessageDialog(this, "¡Usuario registrado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar el usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}