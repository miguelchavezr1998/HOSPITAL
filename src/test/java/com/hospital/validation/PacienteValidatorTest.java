package com.hospital.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacienteValidatorTest {

    private final PacienteValidator validator = new PacienteValidator();

    @Test
    void validarDniCorrecto() {
        assertTrue(validator.validarDni("12345678"));
    }

    @Test
    void rechazarDniIncorrecto() {
        assertFalse(validator.validarDni("123"));
    }

    @Test
    void rechazarNombreVacio() {
        assertFalse(validator.validarNombre(""));
    }

    @Test
    void validarCorreoCorrecto() {
        assertTrue(validator.validarCorreo("paciente@gmail.com"));
    }
}
