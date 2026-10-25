package com.hospital.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class IdGenerator {

    private IdGenerator() {
    }

    /**
     * Devuelve el siguiente id correlativo para la tabla y prefijo indicados.
     *
     * @param conn   conexion activa (no se cierra aqui)
     * @param tabla  nombre de la tabla (ej: "pacientes")
     * @param prefijo prefijo del id (ej: "pac", "doc", "cita", "hist")
     * @return siguiente id, por ejemplo "pac-006"
     */
    public static String siguiente(Connection conn, String tabla, String prefijo) throws SQLException {
        int inicio = prefijo.length() + 2;
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(id FROM " + inicio + ") AS INTEGER)), 0) + 1"
                + " FROM " + tabla
                + " WHERE id ~ '^" + prefijo + "-[0-9]+$'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int numero = rs.next() ? rs.getInt(1) : 1;
            if (numero < 1) {
                numero = 1;
            }
            return String.format("%s-%03d", prefijo, numero);
        }
    }
}
