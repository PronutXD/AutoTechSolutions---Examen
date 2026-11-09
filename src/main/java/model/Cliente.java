package model;

public class Cliente extends Persona {

    private String correo;
    private String direccion;
    /** Valor ROWVERSION leído de la base: sirve para detectar ediciones simultáneas. */
    private byte[] version;
    /** Solo informativo (se calcula al listar): vehículos activos del cliente. */
    private int totalVehiculos;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public byte[] getVersion() {
        return version;
    }

    public void setVersion(byte[] version) {
        this.version = version;
    }

    public int getTotalVehiculos() {
        return totalVehiculos;
    }

    public void setTotalVehiculos(int totalVehiculos) {
        this.totalVehiculos = totalVehiculos;
    }
}
