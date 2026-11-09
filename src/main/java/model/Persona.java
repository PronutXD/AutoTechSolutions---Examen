package model;

/** Datos comunes de las personas del sistema (clientes y, después, mecánicos). */
public abstract class Persona {

    private int id;
    private String codigo;
    private boolean activo = true;
    private String documento;
    private String nombres;
    private String apellidos;
    private String telefono;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** Texto para mostrar en las tablas. */
    public String getEstadoTexto() { return activo ? "Activo" : "Inactivo"; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNombreCompleto() { return nombres + " " + apellidos; }
}
