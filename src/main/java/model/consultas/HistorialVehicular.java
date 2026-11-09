package model.consultas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Una fila de vw_HistorialVehiculo: una orden de un vehículo con sus servicios, mecánicos y repuestos. */
public record HistorialVehicular(LocalDateTime fecha, String orden, String vehiculo, String servicios,
        String mecanicos, String repuestos, BigDecimal costo) { }
