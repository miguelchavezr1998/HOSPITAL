package com.hospital.validation;

import org.apache.commons.lang3.StringUtils;

public class PacienteValidator {

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
