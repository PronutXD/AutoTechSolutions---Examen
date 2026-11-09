package controller;

import dao.ErroresSQL;
import dao.MecanicoDAO;
import dao.ServicioDAO;
import dao.UsuarioDAO;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTable;
import model.Cuenta;
import model.Mecanico;
import model.Servicio;
import model.Sesion;
import views.componentes.Formato;
import views.componentes.Mensajes;
import views.componentes.PnlTabla;
import views.formularios.DlgClave;
import views.formularios.DlgMecanico;
import views.formularios.DlgServicio;
import views.formularios.DlgUsuario;
import views.paneles.PnlCatalogos;

/**
 * Controlador de la pantalla "Catálogos" (servicios, mecánicos y usuarios): llena las
 * tablas, aplica los filtros y abre los formularios. La vista no sabe nada de esto.
 *
 * Conserva la lista de objetos que mostró (en el mismo orden que las filas) para saber
 * qué registro está seleccionado. La pestaña Usuarios solo existe para el administrador.
 */
public class CatalogosController {

    private final PnlCatalogos vista;
    private final Frame ventana;
    private final ServicioDAO servicioDAO = new ServicioDAO();
    private final MecanicoDAO mecanicoDAO = new MecanicoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final boolean esAdministrador;

    private List<Servicio> servicios = new ArrayList<>();
    private List<Mecanico> mecanicos = new ArrayList<>();
    private List<Cuenta> cuentas = new ArrayList<>();

    public CatalogosController(PnlCatalogos vista, Frame ventana) {
        this.vista = vista;
        this.ventana = ventana;
        this.esAdministrador = Sesion.actual() != null && Cuenta.ADMINISTRADOR.equals(Sesion.actual().rol());
    }

    public void iniciar() {
        conectarServicios();
        conectarMecanicos();
        if (esAdministrador) {
            conectarUsuarios();
        } else {
            vista.quitarPestanaUsuarios();
        }
        recargarTodo();

        vista.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                recargarTodo();
            }
        });
        vista.getTabs().addChangeListener(e -> recargarPestanaActual());
    }

    private void recargarTodo() {
        recargarServicios();
        recargarMecanicos();
        if (esAdministrador) {
            recargarUsuarios();
        }
    }

    private void recargarPestanaActual() {
        switch (vista.getTabs().getSelectedIndex()) {
            case 0 -> recargarServicios();
            case 1 -> recargarMecanicos();
            case 2 -> recargarUsuarios();
            default -> { }
        }
    }

    // ================================================================= servicios

    private void conectarServicios() {
        vista.getCmbNivel().addActionListener(e -> recargarServicios());
        vista.getCmbEstadoServicios().addActionListener(e -> recargarServicios());
        vista.getBtnNuevoServicio().addActionListener(e -> nuevoServicio());
        vista.getBtnEditarServicio().addActionListener(e -> editarServicio());
        vista.getBtnEstadoServicio().addActionListener(e -> cambiarEstadoServicio());
        vista.getTblServicios().getTabla().getSelectionModel().addListSelectionListener(e -> {
            Servicio s = servicioSeleccionado();
            if (s != null) {
                vista.getBtnEstadoServicio().setText(s.activo() ? "Desactivar" : "Reactivar");
            }
        });
    }

    private void recargarServicios() {
        int nivel = vista.getCmbNivel().getSelectedIndex(); // 0 = todos, 1..5 = nivel
        try {
            servicios = servicioDAO.listar(filtroEstado(vista.getCmbEstadoServicios().getSelectedIndex()),
                    nivel <= 0 ? null : nivel);
        } catch (SQLException ex) {
            servicios = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los servicios", "dato"));
        }
        PnlTabla t = vista.getTblServicios();
        t.getModelo().setRowCount(0);
        for (Servicio s : servicios) {
            t.getModelo().addRow(new Object[]{
                s.codigo(), s.nombre(), s.nivelTexto(), Formato.dinero(s.manoObra()),
                s.horasEstimadas().toPlainString(), s.garantiaDias(), s.estadoTexto()});
        }
    }

    private void nuevoServicio() {
        DlgServicio dlg = new DlgServicio(ventana, null);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            servicioDAO.insertar(dlg.getServicio());
            Mensajes.info(ventana, "Servicio registrado.");
            recargarServicios();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "guardar el servicio", "nombre"));
        }
    }

    private void editarServicio() {
        Servicio s = servicioSeleccionado();
        if (s == null) {
            return;
        }
        DlgServicio dlg = new DlgServicio(ventana, s);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            servicioDAO.actualizar(dlg.getServicio());
            Mensajes.info(ventana, "Servicio actualizado. Las órdenes anteriores conservan su precio.");
            recargarServicios();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "actualizar el servicio", "nombre"));
        }
    }

    private void cambiarEstadoServicio() {
        Servicio s = servicioSeleccionado();
        if (s == null) {
            return;
        }
        boolean activar = !s.activo();
        if (!activar && !Mensajes.confirmar(ventana, "Desactivar servicio",
                "¿Desactivar \"" + s.nombre() + "\"? Ya no se podrá agregar a órdenes nuevas.")) {
            return;
        }
        try {
            servicioDAO.cambiarEstado(s.id(), activar);
            recargarServicios();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cambiar el estado del servicio", "dato"));
        }
    }

    private Servicio servicioSeleccionado() {
        return seleccionado(vista.getTblServicios(), servicios);
    }

    // ================================================================= mecánicos

    private void conectarMecanicos() {
        vista.getCmbEstadoMecanicos().addActionListener(e -> recargarMecanicos());
        vista.getCmbDisponibilidad().addActionListener(e -> recargarMecanicos());
        vista.getBtnNuevoMecanico().addActionListener(e -> nuevoMecanico());
        vista.getBtnEditarMecanico().addActionListener(e -> editarMecanico());
        vista.getBtnEstadoMecanico().addActionListener(e -> cambiarEstadoMecanico());
        vista.getTblMecanicos().getTabla().getSelectionModel().addListSelectionListener(e -> {
            Mecanico m = mecanicoSeleccionado();
            if (m != null) {
                vista.getBtnEstadoMecanico().setText(m.isActivo() ? "Desactivar" : "Reactivar");
            }
        });
    }

    private void recargarMecanicos() {
        int disp = vista.getCmbDisponibilidad().getSelectedIndex(); // 0 todos, 1 disponibles, 2 no
        try {
            mecanicos = mecanicoDAO.listar(filtroEstado(vista.getCmbEstadoMecanicos().getSelectedIndex()),
                    filtroEstado(disp));
        } catch (SQLException ex) {
            mecanicos = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los mecánicos", "dato"));
        }
        PnlTabla t = vista.getTblMecanicos();
        t.getModelo().setRowCount(0);
        for (Mecanico m : mecanicos) {
            t.getModelo().addRow(new Object[]{
                m.getDocumento(), m.getNombreCompleto(), m.getTelefono(), m.getEspecialidad(),
                m.getEstadoTexto(), m.isDisponible() ? "Disponible" : "No disponible"});
        }
    }

    private void nuevoMecanico() {
        DlgMecanico dlg = new DlgMecanico(ventana, null);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            mecanicoDAO.insertar(dlg.getMecanico());
            Mensajes.info(ventana, "Mecánico registrado.");
            recargarMecanicos();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "guardar el mecánico", "documento"));
        }
    }

    private void editarMecanico() {
        Mecanico seleccionado = mecanicoSeleccionado();
        if (seleccionado == null) {
            return;
        }
        // Se edita una copia: si el usuario cancela o falla el guardado, la lista no cambia.
        DlgMecanico dlg = new DlgMecanico(ventana, copiar(seleccionado));
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        try {
            mecanicoDAO.actualizar(dlg.getMecanico());
            Mensajes.info(ventana, "Mecánico actualizado.");
            recargarMecanicos();
            if (esAdministrador) {
                recargarUsuarios(); // el nombre del mecánico aparece en la tabla de usuarios
            }
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "actualizar el mecánico", "documento"));
        }
    }

    private void cambiarEstadoMecanico() {
        Mecanico m = mecanicoSeleccionado();
        if (m == null) {
            return;
        }
        boolean activar = !m.isActivo();
        if (!activar && !Mensajes.confirmar(ventana, "Desactivar mecánico",
                "¿Desactivar a " + m.getNombreCompleto() + "? Si tiene una cuenta de acceso, también se desactivará.")) {
            return;
        }
        if (!activar && Sesion.actual() != null && Sesion.actual().idMecanico() != null
                && Sesion.actual().idMecanico() == m.getId()) {
            Mensajes.advertencia(ventana, "No puedes desactivar al mecánico de tu propia sesión.");
            return;
        }
        try {
            int cuentasDesactivadas = mecanicoDAO.cambiarEstado(m.getId(), activar);
            if (cuentasDesactivadas > 0) {
                Mensajes.info(ventana, "Mecánico desactivado. Su cuenta de acceso también se desactivó.");
            }
            recargarMecanicos();
            if (esAdministrador) {
                recargarUsuarios();
            }
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cambiar el estado del mecánico", "dato"));
        }
    }

    private Mecanico mecanicoSeleccionado() {
        return seleccionado(vista.getTblMecanicos(), mecanicos);
    }

    // ================================================================= usuarios

    private void conectarUsuarios() {
        vista.getCmbRol().addActionListener(e -> recargarUsuarios());
        vista.getCmbEstadoUsuarios().addActionListener(e -> recargarUsuarios());
        vista.getBtnNuevoUsuario().addActionListener(e -> nuevoUsuario());
        vista.getBtnEditarUsuario().addActionListener(e -> editarUsuario());
        vista.getBtnClave().addActionListener(e -> restablecerClave());
        vista.getBtnEstadoUsuario().addActionListener(e -> cambiarEstadoUsuario());
        vista.getTblUsuarios().getTabla().getSelectionModel().addListSelectionListener(e -> {
            Cuenta c = cuentaSeleccionada();
            if (c != null) {
                vista.getBtnEstadoUsuario().setText(c.activo() ? "Desactivar" : "Reactivar");
            }
        });
    }

    private void recargarUsuarios() {
        int rol = vista.getCmbRol().getSelectedIndex(); // 0 = todos
        try {
            cuentas = usuarioDAO.listar(filtroEstado(vista.getCmbEstadoUsuarios().getSelectedIndex()),
                    rol <= 0 ? null : Cuenta.ROLES[rol - 1]);
        } catch (SQLException ex) {
            cuentas = new ArrayList<>();
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los usuarios", "dato"));
        }
        PnlTabla t = vista.getTblUsuarios();
        t.getModelo().setRowCount(0);
        for (Cuenta c : cuentas) {
            t.getModelo().addRow(new Object[]{
                c.codigo(), c.usuario(), c.mecanico() == null ? "—" : c.mecanico(), c.rol(),
                c.estadoTexto(), c.ultimoAcceso() == null ? "Nunca" : Formato.fechaHora(c.ultimoAcceso())});
        }
    }

    private void nuevoUsuario() {
        List<Mecanico> libres = mecanicosSinCuenta(null);
        if (libres == null) {
            return;
        }
        DlgUsuario dlg = new DlgUsuario(ventana, null, libres);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        char[] clave = dlg.getClave();
        try {
            usuarioDAO.crear(dlg.getUsuario(), clave, dlg.getRol(), dlg.getIdMecanico());
            Mensajes.info(ventana, "Usuario creado.");
            recargarUsuarios();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "crear el usuario", "usuario o mecánico"));
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    private void editarUsuario() {
        Cuenta c = cuentaSeleccionada();
        if (c == null) {
            return;
        }
        List<Mecanico> libres = mecanicosSinCuenta(c.idMecanico());
        if (libres == null) {
            return;
        }
        DlgUsuario dlg = new DlgUsuario(ventana, c, libres);
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        if (c.activo() && Cuenta.ADMINISTRADOR.equals(c.rol()) && !Cuenta.ADMINISTRADOR.equals(dlg.getRol())
                && otrosAdministradoresActivos(c.id()) == 0) {
            Mensajes.advertencia(ventana, "Debe quedar al menos un administrador activo.");
            return;
        }
        if (esMiCuenta(c) && !Cuenta.ADMINISTRADOR.equals(dlg.getRol())) {
            Mensajes.advertencia(ventana, "No puedes quitarte a ti mismo el rol de Administrador.");
            return;
        }
        try {
            usuarioDAO.actualizar(c.id(), dlg.getUsuario(), dlg.getRol(), dlg.getIdMecanico());
            Mensajes.info(ventana, "Usuario actualizado."
                    + (esMiCuenta(c) ? " Los cambios de tu propia cuenta se aplican al volver a iniciar sesión." : ""));
            recargarUsuarios();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "actualizar el usuario", "usuario o mecánico"));
        }
    }

    private void restablecerClave() {
        Cuenta c = cuentaSeleccionada();
        if (c == null) {
            return;
        }
        DlgClave dlg = new DlgClave(ventana, c.usuario());
        dlg.setVisible(true);
        if (!dlg.isAceptado()) {
            return;
        }
        char[] clave = dlg.getClave();
        try {
            usuarioDAO.restablecerClave(c.id(), clave);
            Mensajes.info(ventana, "Contraseña restablecida. " + c.usuario() + " deberá cambiarla al entrar.");
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "restablecer la contraseña", "dato"));
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    private void cambiarEstadoUsuario() {
        Cuenta c = cuentaSeleccionada();
        if (c == null) {
            return;
        }
        boolean activar = !c.activo();
        if (!activar) {
            if (esMiCuenta(c)) {
                Mensajes.advertencia(ventana, "No puedes desactivar tu propia cuenta.");
                return;
            }
            if (Cuenta.ADMINISTRADOR.equals(c.rol()) && otrosAdministradoresActivos(c.id()) == 0) {
                Mensajes.advertencia(ventana, "Debe quedar al menos un administrador activo.");
                return;
            }
            if (!Mensajes.confirmar(ventana, "Desactivar usuario",
                    "¿Desactivar la cuenta \"" + c.usuario() + "\"? Ya no podrá iniciar sesión.")) {
                return;
            }
        }
        try {
            usuarioDAO.cambiarEstado(c.id(), activar);
            recargarUsuarios();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cambiar el estado del usuario", "dato"));
        }
    }

    private Cuenta cuentaSeleccionada() {
        return seleccionado(vista.getTblUsuarios(), cuentas);
    }

    private boolean esMiCuenta(Cuenta c) {
        return Sesion.actual() != null && Sesion.actual().id() == c.id();
    }

    /** Administradores activos distintos de la cuenta indicada (se consulta la base, no la lista filtrada). */
    private int otrosAdministradoresActivos(int idExcluido) {
        try {
            return (int) usuarioDAO.listar(true, Cuenta.ADMINISTRADOR).stream()
                    .filter(a -> a.id() != idExcluido).count();
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "verificar los administradores", "dato"));
            return 0;
        }
    }

    /** Mecánicos activos sin cuenta (más el de la cuenta editada). Null si falla la consulta. */
    private List<Mecanico> mecanicosSinCuenta(Integer incluirId) {
        try {
            return mecanicoDAO.listarSinCuenta(incluirId);
        } catch (SQLException ex) {
            Mensajes.error(ventana, ErroresSQL.mensaje(ex, "cargar los mecánicos", "dato"));
            return null;
        }
    }

    // ================================================================= comunes

    /** 0 = Todos, 1 = el primer valor (Activos / Disponibles), 2 = el segundo. */
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

    private static Mecanico copiar(Mecanico o) {
        Mecanico m = new Mecanico();
        m.setId(o.getId());
        m.setCodigo(o.getCodigo());
        m.setActivo(o.isActivo());
        m.setDocumento(o.getDocumento());
        m.setNombres(o.getNombres());
        m.setApellidos(o.getApellidos());
        m.setTelefono(o.getTelefono());
        m.setEspecialidad(o.getEspecialidad());
        m.setDisponible(o.isDisponible());
        return m;
    }
}
