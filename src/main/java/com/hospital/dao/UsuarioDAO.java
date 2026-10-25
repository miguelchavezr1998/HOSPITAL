package com.hospital.dao;

import com.hospital.database.PostgreSQLConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public boolean autenticar(String usuario, String contrasena) throws SQLException {
        String id;
        String hashGuardado;

        try (Connection conn = PostgreSQLConnection.getInstance().getConnection();
             PreparedStatement consulta = conn.prepareStatement(
                     "SELECT id, password_hash FROM public.usuarios WHERE usuario = ? AND activo = TRUE")) {
            consulta.setString(1, usuario);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    return false;
                }
                id = resultado.getString("id");
                hashGuardado = resultado.getString("password_hash");
            }

            if (hashGuardado == null) {
                return false;
            }

            if (esHashBCrypt(hashGuardado)) {
                if (!BCrypt.checkpw(contrasena, hashGuardado)) {
                    return false;
                }
                actualizarUltimoLogin(conn, id);
                return true;
            }

            if (!MessageDigest.isEqual(
                    hashGuardado.getBytes(StandardCharsets.UTF_8),
                    contrasena.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            String nuevoHash = BCrypt.hashpw(contrasena, BCrypt.gensalt(12));
            try (PreparedStatement actualizacion = conn.prepareStatement(
                    "UPDATE public.usuarios SET password_hash = ?, ultimo_login = CURRENT_TIMESTAMP "
                            + "WHERE id = ? AND password_hash = ? AND activo = TRUE")) {
                actualizacion.setString(1, nuevoHash);
                actualizacion.setString(2, id);
                actualizacion.setString(3, hashGuardado);
                return actualizacion.executeUpdate() == 1;
            }
        }
    }

    private boolean esHashBCrypt(String valor) {
        return valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$");
    }

    private void actualizarUltimoLogin(Connection conn, String id) throws SQLException {
        try (PreparedStatement actualizacion = conn.prepareStatement(
                "UPDATE public.usuarios SET ultimo_login = CURRENT_TIMESTAMP WHERE id = ?")) {
            actualizacion.setString(1, id);
            actualizacion.executeUpdate();
        }
    }
}