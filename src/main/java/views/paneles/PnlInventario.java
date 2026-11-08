package views.paneles;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import views.componentes.PnlTabla;

/**
 * Pantalla "Inventario": repuestos, proveedores, movimientos de almacén y motores.
 *
 * Los motores también son repuestos: aparecen en la lista de Repuestos con su
 * nombre y número de serie, y desde ahí (clic derecho) se salta a la pestaña
 * Motores, que muestra la ficha técnica y permite buscar motores compatibles.
 */
public class PnlInventario extends PnlSeccion {

    private static final int COL_REPUESTO = 1;
    private static final int COL_CATEGORIA = 2;

    private final PnlTabla tblRepuestos = new PnlTabla(
            "Código", "Repuesto", "Categoría", "Proveedor", "Stock", "Mínimo", "Costo", "Precio venta");

    private final PnlTabla tblProveedores = new PnlTabla(
            "Nombre", "Contacto", "Teléfono", "Correo", "Estado");

    private final PnlTabla tblMovimientos = new PnlTabla(
            "Código", "Fecha", "Repuesto", "Tipo", "Cantidad", "Costo unitario", "Proveedor / Orden", "Usuario");

    private final PnlTabla tblMotores = new PnlTabla(
            "Motor (nombre / serie)", "Marca", "Modelo compatible", "Años", "Cilindraje", "Combustible", "Estado");

    private final JTabbedPane tabs;
    private int indiceMotores;

    public PnlInventario() {
        super("Inventario",
                "Repuestos, proveedores, movimientos de almacén y motores");

        configurarRepuestos();
        configurarProveedores();
        configurarMovimientos();
        configurarMotores();

        tabs = crearPestanas();
        agregarPestana(tabs, "Repuestos", tblRepuestos);
        agregarPestana(tabs, "Proveedores", tblProveedores);
        agregarPestana(tabs, "Movimientos", tblMovimientos);
        agregarPestana(tabs, "Motores", tblMotores);
        indiceMotores = 3;
    }

    private void configurarRepuestos() {
        tblRepuestos.setPlaceholder("Buscar por código, nombre o proveedor...");
        tblRepuestos.addFiltro("Categoría", "Todas", "Aceite", "Filtro", "Bujía", "Batería", "Freno",
                "Radiador", "Transmisión", "Motor", "Otro");
        tblRepuestos.addFiltroCheck("Solo stock bajo");
        tblRepuestos.addAccion("Nuevo repuesto", true, false);
        tblRepuestos.addAccion("Registrar entrada", false, true);
        tblRepuestos.addAccion("Editar", false, true);
        tblRepuestos.setAnchos(110, 230, 110, 150, 60, 70, 80, 100);
        tblRepuestos.alinearDerecha(4, 5, 6, 7);

        tblRepuestos.addMenuContextual("Ver detalles del motor")
                .addActionListener(e -> verDetalleMotor());
    }

    private void configurarProveedores() {
        tblProveedores.setPlaceholder("Buscar por nombre, contacto o correo...");
        tblProveedores.addFiltro("Estado", "Todos", "Activos", "Inactivos");
        tblProveedores.addAccion("Nuevo proveedor", true, false);
        tblProveedores.addAccion("Editar", false, true);
        tblProveedores.addAccion("Desactivar", false, true);
        tblProveedores.setAnchos(200, 160, 120, 220, 90);
    }

    /** Solo lectura: los movimientos los generan las entradas y los consumos de las órdenes. */
    private void configurarMovimientos() {
        tblMovimientos.setPlaceholder("Buscar por código, repuesto, proveedor u orden...");
        tblMovimientos.addFiltro("Tipo", "Todos", "Entrada", "Salida");
        tblMovimientos.setAnchos(110, 130, 220, 80, 80, 110, 180, 110);
        tblMovimientos.alinearDerecha(4, 5);
    }

    private void configurarMotores() {
        tblMotores.setPlaceholder("Buscar por serie, marca, modelo o cilindraje...");
        tblMotores.addFiltro("Combustible", "Todos", "Gasolina", "Diésel", "Híbrido", "Eléctrico", "Gas");
        tblMotores.addFiltro("Estado", "Todos", "Disponible", "Reservado", "Instalado", "Retirado", "Descartado");
        tblMotores.addAccion("Nuevo motor", true, false);
        tblMotores.addAccion("Buscar compatibles", false, true);
        tblMotores.addAccion("Editar", false, true);
        tblMotores.setAnchos(220, 110, 160, 110, 90, 110, 100);
    }

    /** Clic derecho en Repuestos: salta a Motores y deja filtrado ese motor. */
    private void verDetalleMotor() {
        Object categoria = tblRepuestos.getValorSeleccionado(COL_CATEGORIA);
        if (!"Motor".equals(categoria)) {
            JOptionPane.showMessageDialog(this, "Esta opción solo aplica a repuestos de categoría Motor.",
                    "Ver detalles del motor", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object nombre = tblRepuestos.getValorSeleccionado(COL_REPUESTO);
        tblMotores.getBuscador().setText(nombre == null ? "" : nombre.toString());
        tabs.setSelectedIndex(indiceMotores);
    }
}