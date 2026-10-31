package com.hospital.validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class PacienteValidator {

    public String validarCamposObligatorios(String sexo, String telefono,
            String direccion) {
        List<String> camposFaltantes = new ArrayList<>();
        if (StringUtils.isBlank(sexo)) {
            camposFaltantes.add("Sexo");
        }
        if (StringUtils.isBlank(telefono)) {
            camposFaltantes.add("Telefono");
        }
        if (StringUtils.isBlank(direccion)) {
            camposFaltantes.add("Direccion");
        }

        return camposFaltantes.isEmpty()
                ? null
                : "Completa los siguientes campos obligatorios: "
                        + String.join(", ", camposFaltantes) + ".";
    }

    public String validar(String cedula, String nombre, String apellido,
            String correo, String telefono, LocalDate fechaNacimiento) {
        if (StringUtils.isBlank(cedula)
                || !cedula.matches("\\d{8}")) {
            return "La cedula debe contener 8 digitos.";
        }

        if (StringUtils.isBlank(nombre)
                || nombre.trim().length() < 2) {
            return "El nombre debe tener al menos 2 caracteres.";
        }

        if (StringUtils.isBlank(apellido)
                || apellido.trim().length() < 2) {
            return "El apellido debe tener al menos 2 caracteres.";
        }

        if (StringUtils.isNotBlank(correo)
                && !correo.matches(
                    "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "El correo electronico no es valido.";
        }

        if (StringUtils.isNotBlank(telefono)
                && !telefono.matches("\\d{9}")) {
            return "El telefono debe contener 9 digitos.";
        }

        if (fechaNacimiento != null
                && fechaNacimiento.isAfter(LocalDate.now())) {
            return "La fecha de nacimiento no puede ser futura.";
        }

        return null;
    }

    public boolean validarNombre(String nombre) {
        return StringUtils.isNotBlank(nombre)
                && nombre.trim().length() >= 2;
    }

    public boolean validarDni(String dni) {
        return StringUtils.isNotBlank(dni)
                && dni.matches("\\d{8}");
    }

    public boolean validarCorreo(String correo) {
        return StringUtils.isNotBlank(correo)
                && correo.matches(
                    "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"
                );
    }
}
