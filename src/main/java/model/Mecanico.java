package model;

/** Mecánico del taller (tabla Mecanicos). */
public class Mecanico extends Persona {

    private String especialidad;
    private boolean disponible = true;

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    /** Texto con el que aparece en las listas desplegables. */
    @Override
    public String toString() {
        return getNombreCompleto() + (especialidad == null ? "" : " · " + especialidad);
    }
}
