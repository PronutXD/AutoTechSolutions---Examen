package model.ordenes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Orden de trabajo (tabla OrdenesTrabajo). Los campos cliente, vehiculo, responsable y total
 * son solo informativos: vienen de vw_ListadoOrdenes o de los JOIN al consultar.
 */
public class Orden {

    /** Mismos valores que el CHECK de la columna estado en SQL Server. */
    public static final String RECIBIDA = "Recibida";
    public static final String EN_DIAGNOSTICO = "En diagnóstico";
    public static final String PENDIENTE_APROBACION = "Pendiente de aprobación";
    public static final String EN_REPARACION = "En reparación";
    public static final String EN_CONTROL_CALIDAD = "En control de calidad";
    public static final String FINALIZADA = "Finalizada y entregada";
    public static final String RECHAZADA = "Rechazada";
    public static final String CANCELADA = "Cancelada";

    public static final String[] ESTADOS = {
        RECIBIDA, EN_DIAGNOSTICO, PENDIENTE_APROBACION, EN_REPARACION,
        EN_CONTROL_CALIDAD, FINALIZADA, RECHAZADA, CANCELADA};

    /**
     * Estados a los que se puede pasar a mano desde cada estado. "Finalizada y entregada"
     * no aparece: solo la alcanza sp_FinalizarOrden (exige calidad aprobada y saldo en cero).
     */
    private static final Map<String, List<String>> SIGUIENTES = Map.of(
            RECIBIDA, List.of(EN_DIAGNOSTICO, CANCELADA),
            EN_DIAGNOSTICO, List.of(PENDIENTE_APROBACION, CANCELADA),
            PENDIENTE_APROBACION, List.of(EN_REPARACION, RECHAZADA, CANCELADA),
            EN_REPARACION, List.of(EN_CONTROL_CALIDAD, CANCELADA),
            EN_CONTROL_CALIDAD, List.of(EN_REPARACION, CANCELADA));

    private int id;
    private String codigo;
    private int idVehiculo;
    private int idUsuario;
    private LocalDateTime fechaIngreso;
    private int kilometrajeIngreso;
    private String motivo;
    private String estado = RECIBIDA;
    private BigDecimal descuento = BigDecimal.ZERO;
    private BigDecimal porcentajeImpuesto = new BigDecimal("15");
    private LocalDateTime fechaEntrega;
    /** Valor ROWVERSION leído de la base: detecta ediciones simultáneas. */
    private byte[] version;

    private String cliente;
    private String vehiculo;
    private String responsable;
    private BigDecimal total;

    public static List<String> siguientesEstados(String estado) {
        return SIGUIENTES.getOrDefault(estado, List.of());
    }

    public boolean isCerrada() {
        return FINALIZADA.equals(estado) || RECHAZADA.equals(estado) || CANCELADA.equals(estado);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public int getKilometrajeIngreso() { return kilometrajeIngreso; }
    public void setKilometrajeIngreso(int kilometrajeIngreso) { this.kilometrajeIngreso = kilometrajeIngreso; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public BigDecimal getPorcentajeImpuesto() { return porcentajeImpuesto; }
    public void setPorcentajeImpuesto(BigDecimal porcentajeImpuesto) { this.porcentajeImpuesto = porcentajeImpuesto; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public byte[] getVersion() { return version; }
    public void setVersion(byte[] version) { this.version = version; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getVehiculo() { return vehiculo; }
    public void setVehiculo(String vehiculo) { this.vehiculo = vehiculo; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
