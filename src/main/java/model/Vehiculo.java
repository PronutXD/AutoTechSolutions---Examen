package model;

public class Vehiculo {

    /** Mismos valores que el CHECK de la columna combustible en SQL Server. */
    public static final String[] COMBUSTIBLES = {"Gasolina", "Diésel", "Híbrido", "Eléctrico", "Gas"};

    private int id;
    private String codigo;
    private boolean activo = true;
    private int idCliente;
    /** Solo informativo (viene del JOIN al listar): nombre del dueño. */
    private String propietario;
    private String placa;
    private String vin;
    private String marca;
    private String modelo;
    private int anio;
    private String color;
    private int kilometraje;
    private String combustible = COMBUSTIBLES[0];
    /** Valor ROWVERSION leído de la base: detecta ediciones simultáneas. */
    private byte[] version;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getKilometraje() { return kilometraje; }
    public void setKilometraje(int kilometraje) { this.kilometraje = kilometraje; }

    public String getCombustible() { return combustible; }
    public void setCombustible(String combustible) { this.combustible = combustible; }

    public byte[] getVersion() { return version; }
    public void setVersion(byte[] version) { this.version = version; }

    /** Texto para mostrar en las tablas. */
    public String getEstadoTexto() { return activo ? "Activo" : "Inactivo"; }
}
