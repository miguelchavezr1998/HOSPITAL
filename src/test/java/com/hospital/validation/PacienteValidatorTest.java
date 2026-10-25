package com.hospital.validation;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacienteValidatorTest {

    private final PacienteValidator validator = new PacienteValidator();

    @Test
    void pacienteValido() {
        assertNull(validator.validar(
                "12345678", "Carlos", "Perez",
                "carlos@gmail.com", "987654321",
                LocalDate.of(2000, 5, 10)));
    }

    @Test
    void cedulaIncorrecta() {
        assertNotNull(validator.validar(
                "123", "Carlos", "Perez",
                "carlos@gmail.com", "987654321",
                LocalDate.of(2000, 5, 10)));
    }

    @Test
    void correoIncorrecto() {
        assertNotNull(validator.validar(
                "12345678", "Carlos", "Perez",
                "correo-invalido", "987654321",
                LocalDate.of(2000, 5, 10)));
    }

    @Test
    void fechaFutura() {
        assertNotNull(validator.validar(
                "12345678", "Carlos", "Perez",
                "carlos@gmail.com", "987654321",
                LocalDate.now().plusDays(1)));
    }

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

    @Test
    void validarDatosRequeridosConCamposOpcionalesVacios() {
        assertNull(validator.validar(
                "12345678", "Ana", "Lopez", "", "", null));
    }

    @Test
    void rechazarCorreoInvalido() {
        assertEquals(
                "El correo electronico no es valido.",
                validator.validar(
                        "12345678", "Ana", "Lopez", "correo-invalido", "", null));
    }

    @Test
    void rechazarTelefonoConCantidadIncorrectaDeDigitos() {
        assertEquals(
                "El telefono debe contener 9 digitos.",
                validator.validar(
                        "12345678", "Ana", "Lopez", "", "12345", null));
    }

    @Test
    void rechazarFechaDeNacimientoFutura() {
        assertEquals(
                "La fecha de nacimiento no puede ser futura.",
                validator.validar(
                        "12345678", "Ana", "Lopez", "", "",
                        LocalDate.now().plusDays(1)));
    }
}
