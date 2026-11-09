package model;

import java.math.BigDecimal;

/**
 * Ficha técnica de un motor (tabla Motores). Todo motor es además un repuesto de
 * categoría Motor: idRepuesto lo enlaza con esa fila de Repuestos (nombre y precio).
 */
public class Motor {

    /** Mismos valores que usa la pantalla de Inventario en el filtro de estado. */
    public static final String DISPONIBLE = "Disponible";
    public static final String[] ESTADOS = {"Disponible", "Reservado", "Instalado", "Retirado", "Descartado"};

    private int id;
    private String codigo;
    private int idRepuesto;
    private String numeroSerie;
    private String marca;
    private String modeloCompatible;
    private int anioDesde;
    private int anioHasta;
    private BigDecimal cilindraje;
    private String combustible = Vehiculo.COMBUSTIBLES[0];
    private String estado = DISPONIBLE;

    /** Solo informativos (vienen del JOIN con Repuestos). */
    private String repuesto;
    private BigDecimal precioVenta;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public int getIdRepuesto() { return idRepuesto; }
    public void setIdRepuesto(int idRepuesto) { this.idRepuesto = idRepuesto; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModeloCompatible() { return modeloCompatible; }
    public void setModeloCompatible(String modeloCompatible) { this.modeloCompatible = modeloCompatible; }

    public int getAnioDesde() { return anioDesde; }
    public void setAnioDesde(int anioDesde) { this.anioDesde = anioDesde; }

    public int getAnioHasta() { return anioHasta; }
    public void setAnioHasta(int anioHasta) { this.anioHasta = anioHasta; }

    public BigDecimal getCilindraje() { return cilindraje; }
    public void setCilindraje(BigDecimal cilindraje) { this.cilindraje = cilindraje; }

    public String getCombustible() { return combustible; }
    public void setCombustible(String combustible) { this.combustible = combustible; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRepuesto() { return repuesto; }
    public void setRepuesto(String repuesto) { this.repuesto = repuesto; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    /** Nombre del repuesto y número de serie, como se muestra en la tabla de motores. */
    public String getNombreSerie() {
        return numeroSerie == null || numeroSerie.isBlank() ? repuesto : repuesto + " · " + numeroSerie;
    }

    /** Rango de años compatible, por ejemplo 2009-2013. */
    public String getAnios() {
        return anioDesde == anioHasta ? String.valueOf(anioDesde) : anioDesde + "-" + anioHasta;
    }
}
