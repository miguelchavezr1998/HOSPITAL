package com.hospital.model;

import java.time.LocalDate;

public class Cita {

    public enum Estado {
        PROGRAMADA("Programada"),
        COMPLETADA("Completada"),
        CANCELADA("Cancelada");

        private final String label;
        Estado(String label) { this.label = label; }
        public String getLabel() { return label; }

        public static Estado fromString(String text) {
            for (Estado e : values()) {
                if (e.label.equalsIgnoreCase(text)) return e;
            }
            return PROGRAMADA;
        }
    }

    private String id;
    private String pacienteId;
    private String doctorId;
    private String pacienteNombre;
    private String doctorNombre;
    private LocalDate fecha;
    private String hora;
    private String motivo;
    private Estado estado;
    private String observaciones;

    public Cita() {
        this.estado = Estado.PROGRAMADA;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }

    public String getDoctorNombre() { return doctorNombre; }
    public void setDoctorNombre(String doctorNombre) { this.doctorNombre = doctorNombre; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return "Cita: " + pacienteNombre + " con " + doctorNombre + " - " + fecha + " " + hora;
    }
}