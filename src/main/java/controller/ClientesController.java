package controller;

import dao.ClienteDAO;
import dao.ErroresSQL;
import dao.VehiculoDAO;
import java.awt.Frame;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import model.Cliente;
import model.Vehiculo;
import views.componentes.Mensajes;
import views.componentes.PnlTabla;
import views.formularios.DlgCliente;
import views.formularios.DlgVehiculo;
import views.paneles.PnlClientes;

/**
 * Controlador de la pantalla "Clientes y vehículos" (sus dos pestañas):
 * llena las tablas, aplica los filtros y abre los formularios de Nuevo y Editar.
 * La vista no sabe nada de esto: solo muestra lo que este controlador le entrega.
 *
 * Las tablas no guardan el id; el controlador conserva la lista de objetos que
 * mostró (en el mismo orden) y la usa para saber qué registro está seleccionado.
 */
public class ClientesController {

    private static final String CAMBIO_AJENO =
            "Otro usuario modificó este registro mientras lo editabas. "
            + "Se recargó la lista para que veas los datos actuales y vuelvas a intentar.";

    private final PnlClientes vista;
    private final Frame ventana;
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();

    private List<Cliente> clientes = new ArrayList<>();
    private List<Vehiculo> vehiculos = new ArrayList<>();

    public ClientesController(PnlClientes vista, Frame ventana) {
        this.vista = vista;
        this.ventana = ventana;
    }

    public void iniciar() {
        conectarClientes();
        conectarVehiculos();
        recargarClientes();
        recargarVehiculos();
    }

    // ================================================================= clientes

    private void conectarClientes() {
        vista.getCmbEstadoClientes().addActionListener(e -> recargarClientes());
        vista.getBtnNuevoCliente().addActionListener(e -> nuevoCliente());
        vista.getBtnEditarCliente().addActionListener(e -> editarCliente());
        vista.getBtnEstadoCliente().addActionListener(e -> cambiarEstadoCliente());
        vista.getTblClientes().getTabla().getSelectionModel().addListSelectionListener(e -> {
            Cliente c = clienteSeleccionado();
            if (c != null) {
                vista.getBtnEstadoCliente().setText(c.isActivo() ? "Desactivar" : "Reactivar");
            }
        });
    }

    private void recargarClientes() {
        try {
            clientes = clienteDAO.listar(filtroEstado(vista.getCmbEstadoClientes().getSelectedIndex()));
        } catch (SQLException ex) {
            clientes = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los clientes", "dato"));
        }
        PnlTabla t = vista.getTblClientes();
        t.getModelo().setRowCount(0);
        for (Cliente c : clientes) {
            t.getModelo().addRow(new Object[]{
                c.getDocumento(), c.getNombres(), c.getApellidos(), c.getTelefono(),
                c.getCorreo(), c.getTotalVehiculos(), c.getEstadoTexto()});
        }
    }

    private void nuevoCliente() {
        DlgCliente dlg = new DlgCliente(ventana, null);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            clienteDAO.insertar(dlg.getCliente());
            Mensajes.info(ventana, "Cliente registrado.");
            recargarClientes();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "guardar el cliente", "documento"));
        }
    }

    private void editarCliente() {
        Cliente seleccionado = clienteSeleccionado();
        if (seleccionado == null) {
            return;
        }
        // Se edita una copia: si el usuario cancela o falla el guardado, la lista no cambia.
        DlgCliente dlg = new DlgCliente(ventana, copiar(seleccionado));
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            if (clienteDAO.actualizar(dlg.getCliente())) {
                Mensajes.info(ventana, "Cliente actualizado.");
            } else {
                Mensajes.advertencia(ventana, CAMBIO_AJENO);
            }
            recargarClientes();
            recargarVehiculos(); // el nombre del propietario aparece en la tabla de vehículos
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "actualizar el cliente", "documento"));
        }
    }

    private void cambiarEstadoCliente() {
        Cliente c = clienteSeleccionado();
        if (c == null) {
            return;
        }
        boolean activar = !c.isActivo();
        if (!activar) {
            String pregunta = c.getTotalVehiculos() > 0
                    ? c.getNombreCompleto() + " tiene " + c.getTotalVehiculos()
                            + " vehículo(s) activo(s). ¿Desactivarlo de todos modos?"
                    : "¿Desactivar a " + c.getNombreCompleto() + "?";
            if (!Mensajes.confirmar(ventana, "Desactivar cliente", pregunta)) {
                return;
            }
        }
        try {
            clienteDAO.cambiarEstado(c.getId(), activar);
            recargarClientes();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cambiar el estado del cliente", "dato"));
        }
    }

    private Cliente clienteSeleccionado() {
        return seleccionado(vista.getTblClientes(), clientes);
    }

    // ================================================================= vehículos

    private void conectarVehiculos() {
        vista.getCmbCombustible().addActionListener(e -> recargarVehiculos());
        vista.getCmbEstadoVehiculos().addActionListener(e -> recargarVehiculos());
        vista.getBtnNuevoVehiculo().addActionListener(e -> nuevoVehiculo());
        vista.getBtnEditarVehiculo().addActionListener(e -> editarVehiculo());
        vista.getBtnEstadoVehiculo().addActionListener(e -> cambiarEstadoVehiculo());
        vista.getTblVehiculos().getTabla().getSelectionModel().addListSelectionListener(e -> {
            Vehiculo v = vehiculoSeleccionado();
            if (v != null) {
                vista.getBtnEstadoVehiculo().setText(v.isActivo() ? "Desactivar" : "Reactivar");
            }
        });
    }

    private void recargarVehiculos() {
        int combo = vista.getCmbCombustible().getSelectedIndex();
        String combustible = combo <= 0 ? null : Vehiculo.COMBUSTIBLES[combo - 1];
        try {
            vehiculos = vehiculoDAO.listar(filtroEstado(vista.getCmbEstadoVehiculos().getSelectedIndex()), combustible);
        } catch (SQLException ex) {
            vehiculos = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los vehículos", "dato"));
        }
        PnlTabla t = vista.getTblVehiculos();
        t.getModelo().setRowCount(0);
        for (Vehiculo v : vehiculos) {
            t.getModelo().addRow(new Object[]{
                v.getPlaca(), v.getVin(), v.getMarca(), v.getModelo(), v.getAnio(), v.getColor(),
                v.getKilometraje(), v.getCombustible(), v.getPropietario(), v.getEstadoTexto()});
        }
    }

    private void nuevoVehiculo() {
        List<Cliente> disponibles = clientesParaElegir(null);
        if (disponibles == null) {
            return;
        }
        DlgVehiculo dlg = new DlgVehiculo(ventana, null, disponibles);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            vehiculoDAO.insertar(dlg.getVehiculo());
            Mensajes.info(ventana, "Vehículo registrado.");
            recargarVehiculos();
            recargarClientes(); // cambia el contador de vehículos de su dueño
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "guardar el vehículo", "placa o VIN"));
        }
    }

    private void editarVehiculo() {
        Vehiculo seleccionado = vehiculoSeleccionado();
        if (seleccionado == null) {
            return;
        }
        List<Cliente> disponibles = clientesParaElegir(seleccionado.getIdCliente());
        if (disponibles == null) {
            return;
        }
        DlgVehiculo dlg = new DlgVehiculo(ventana, copiar(seleccionado), disponibles);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            if (vehiculoDAO.actualizar(dlg.getVehiculo())) {
                Mensajes.info(ventana, "Vehículo actualizado.");
            } else {
                Mensajes.advertencia(ventana, CAMBIO_AJENO);
            }
            recargarVehiculos();
            recargarClientes();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "actualizar el vehículo", "placa o VIN"));
        }
    }

    private void cambiarEstadoVehiculo() {
        Vehiculo v = vehiculoSeleccionado();
        if (v == null) {
            return;
        }
        boolean activar = !v.isActivo();
        if (!activar && !Mensajes.confirmar(ventana, "Desactivar vehículo",
                "¿Desactivar el vehículo " + v.getPlaca() + "?")) {
            return;
        }
        try {
            vehiculoDAO.cambiarEstado(v.getId(), activar);
            recargarVehiculos();
            recargarClientes();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cambiar el estado del vehículo", "dato"));
        }
    }

    private Vehiculo vehiculoSeleccionado() {
        return seleccionado(vista.getTblVehiculos(), vehiculos);
    }

    /**
     * Clientes activos para el combo de propietario (más el dueño actual si se está
     * editando y ese cliente está inactivo). Devuelve null si no hay ninguno.
     */
    private List<Cliente> clientesParaElegir(Integer idDuenoActual) {
        try {
            List<Cliente> lista = new ArrayList<>(clienteDAO.listar(true));
            if (idDuenoActual != null && lista.stream().noneMatch(c -> c.getId() == idDuenoActual)) {
                clienteDAO.buscarPorId(idDuenoActual).ifPresent(lista::add);
            }
            if (lista.isEmpty()) {
                Mensajes.advertencia(ventana, "Primero registra un cliente: todo vehículo necesita un propietario.");
                return null;
            }
            return lista;
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los clientes", "dato"));
            return null;
        }
    }

    // ================================================================= comunes

    /** 0 = Todos, 1 = Activos, 2 = Inactivos. */
    private static Boolean filtroEstado(int indice) {
        return indice == 1 ? Boolean.TRUE : indice == 2 ? Boolean.FALSE : null;
    }

    private static <T> T seleccionado(PnlTabla tabla, List<T> datos) {
        JTable t = tabla.getTabla();
        int fila = t.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        int modelo = t.convertRowIndexToModel(fila);
        return modelo < datos.size() ? datos.get(modelo) : null;
    }

    private static Cliente copiar(Cliente o) {
        Cliente c = new Cliente();
        c.setId(o.getId());
        c.setCodigo(o.getCodigo());
        c.setActivo(o.isActivo());
        c.setDocumento(o.getDocumento());
        c.setNombres(o.getNombres());
        c.setApellidos(o.getApellidos());
        c.setTelefono(o.getTelefono());
        c.setCorreo(o.getCorreo());
        c.setDireccion(o.getDireccion());
        c.setVersion(o.getVersion());
        return c;
    }

    private static Vehiculo copiar(Vehiculo o) {
        Vehiculo v = new Vehiculo();
        v.setId(o.getId());
        v.setCodigo(o.getCodigo());
        v.setActivo(o.isActivo());
        v.setIdCliente(o.getIdCliente());
        v.setPlaca(o.getPlaca());
        v.setVin(o.getVin());
        v.setMarca(o.getMarca());
        v.setModelo(o.getModelo());
        v.setAnio(o.getAnio());
        v.setColor(o.getColor());
        v.setKilometraje(o.getKilometraje());
        v.setCombustible(o.getCombustible());
        v.setVersion(o.getVersion());
        return v;
    }
}
