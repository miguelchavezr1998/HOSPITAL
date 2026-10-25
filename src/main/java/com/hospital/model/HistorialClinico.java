package com.hospital.model;

import java.time.LocalDate;

public class HistorialClinico {

    private String id;
    private String pacienteId;
    private String doctorId;
    private String citaId;
    private String pacienteNombre;
    private String doctorNombre;
    private LocalDate fecha;
    private String diagnostico;
    private String diagnosticoCie10;
    private String tratamiento;
    private String notas;
    private String presionArterial;
    private String temperatura;
    private String pulso;
    private String peso;
    private String estatura;

    public HistorialClinico() {
        this.fecha = LocalDate.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getCitaId() { return citaId; }
    public void setCitaId(String citaId) { this.citaId = citaId; }

    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }

    public String getDoctorNombre() { return doctorNombre; }
    public void setDoctorNombre(String doctorNombre) { this.doctorNombre = doctorNombre; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getDiagnosticoCie10() { return diagnosticoCie10; }
    public void setDiagnosticoCie10(String diagnosticoCie10) { this.diagnosticoCie10 = diagnosticoCie10; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getPresionArterial() { return presionArterial; }
    public void setPresionArterial(String presionArterial) { this.presionArterial = presionArterial; }

    public String getTemperatura() { return temperatura; }
    public void setTemperatura(String temperatura) { this.temperatura = temperatura; }

    public String getPulso() { return pulso; }
    public void setPulso(String pulso) { this.pulso = pulso; }

    public String getPeso() { return peso; }
    public void setPeso(String peso) { this.peso = peso; }

    public String getEstatura() { return estatura; }
    public void setEstatura(String estatura) { this.estatura = estatura; }

    @Override
    public String toString() {
        return "Historial: " + pacienteNombre + " - " + diagnostico + " (" + fecha + ")";
    }
}