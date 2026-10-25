package com.hospital.model;

public class Doctor {

    private String id;
    private String cedula;
    private String nombre;
    private String apellido;
    private String especialidad;
    private String telefono;
    private String email;
    private String horario;

    public Doctor() {}

    public Doctor(String cedula, String nombre, String apellido, String especialidad) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.especialidad = especialidad;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getNombreCompleto() {
        return "Dr(a). " + nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + especialidad;
    }
}