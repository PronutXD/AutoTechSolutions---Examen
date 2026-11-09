package model;

/** Proveedor de repuestos (tabla Proveedores). */
public class Proveedor {

    private int id;
    private String codigo;
    private boolean activo = true;
    private String nombre;
    private String contacto;
    private String telefono;
    private String correo;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** Texto para mostrar en las tablas. */
    public String getEstadoTexto() { return activo ? "Activo" : "Inactivo"; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    /** Texto con el que aparece en las listas desplegables. */
    @Override
    public String toString() { return nombre; }
}
