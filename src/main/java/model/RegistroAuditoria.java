package model;

import java.time.LocalDateTime;

/** Una fila de la tabla Auditoria. Los valores son JSON con el registro antes y después del cambio. */
public record RegistroAuditoria(LocalDateTime fecha, String usuario, String tabla, int idRegistro, String accion,
        String valorAnterior, String valorNuevo) { }
