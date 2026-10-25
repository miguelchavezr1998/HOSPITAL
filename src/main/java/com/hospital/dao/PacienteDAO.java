package com.hospital.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.hospital.database.PostgreSQLConnection;
import com.hospital.model.Paciente;
import com.hospital.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacienteDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacienteDAO.class);
    private static final String TABLE = "pacientes";

    public void insertar(Paciente paciente) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "INSERT INTO " + TABLE + " (id, cedula, nombre, apellido, fecha_nacimiento, sexo, telefono, email, direccion, tipo_sangre, alergias) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String idGenerado = (paciente.getId() == null || paciente.getId().isEmpty())
                    ? IdGenerator.siguiente(conn, TABLE, "pac") : paciente.getId();
            ps.setString(1, idGenerado);
            ps.setString(2, paciente.getCedula());
            ps.setString(3, paciente.getNombre());
            ps.setString(4, paciente.getApellido());
            // fecha_nacimiento es DATE en PostgreSQL: se envia como LocalDate, no como String
            ps.setObject(5, paciente.getFechaNacimiento());
            ps.setString(6, paciente.getSexo());
            ps.setString(7, paciente.getTelefono());
            ps.setString(8, paciente.getEmail());
            ps.setString(9, paciente.getDireccion());
            ps.setString(10, paciente.getTipoSangre());
            ps.setString(11, paciente.getAlergias() != null ? String.join(",", paciente.getAlergias()) : null);
            // fecha_registro no se inserta: la columna es TIMESTAMP DEFAULT CURRENT_TIMESTAMP y lo llena PostgreSQL
            ps.executeUpdate();
            LOGGER.info("Paciente registrado correctamente");
        } catch (SQLException e) {
            LOGGER.error("Error al registrar paciente", e);
        }
    }

    public void actualizar(Paciente paciente) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "UPDATE " + TABLE + " SET cedula=?, nombre=?, apellido=?, fecha_nacimiento=?, sexo=?, telefono=?, email=?, direccion=?, tipo_sangre=?, alergias=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, paciente.getCedula());
            ps.setString(2, paciente.getNombre());
            ps.setString(3, paciente.getApellido());
            // fecha_nacimiento es DATE en PostgreSQL: se envia como LocalDate, no como String
            ps.setObject(4, paciente.getFechaNacimiento());
            ps.setString(5, paciente.getSexo());
            ps.setString(6, paciente.getTelefono());
            ps.setString(7, paciente.getEmail());
            ps.setString(8, paciente.getDireccion());
            ps.setString(9, paciente.getTipoSangre());
            ps.setString(10, paciente.getAlergias() != null ? String.join(",", paciente.getAlergias()) : null);
            // fecha_registro no se actualiza: es la fecha de creacion del registro
            ps.setString(11, paciente.getId());
            ps.executeUpdate();
            LOGGER.info("Registro de paciente actualizado correctamente");
        } catch (SQLException e) {
            LOGGER.error("Error al actualizar registro de paciente", e);
        }
    }

    public void eliminar(String id) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "DELETE FROM " + TABLE + " WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ps.executeUpdate();
            LOGGER.info("Registro de paciente eliminado correctamente");
        } catch (SQLException e) {
            LOGGER.error("Error al eliminar registro de paciente", e);
        }
    }

    public Paciente buscarPorId(String id) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearPaciente(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar paciente por ID: " + e.getMessage());
            return null;
        }
    }

    public Paciente buscarPorCedula(String cedula) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE cedula=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearPaciente(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar paciente por cedula: " + e.getMessage());
            return null;
        }
    }

    public List<Paciente> listarTodos() {
        List<Paciente> pacientes = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    public List<Paciente> buscar(String texto) {
        List<Paciente> pacientes = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE LOWER(nombre) LIKE LOWER(?) OR LOWER(apellido) LIKE LOWER(?) OR LOWER(cedula) LIKE LOWER(?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String likePattern = "%" + texto + "%";
            ps.setString(1, likePattern);
            ps.setString(2, likePattern);
            ps.setString(3, likePattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    public long contar() {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT COUNT(*) FROM " + TABLE;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar pacientes: " + e.getMessage());
        }
        return 0;
    }

    private Paciente mapearPaciente(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId(rs.getString("id"));
        p.setCedula(rs.getString("cedula"));
        p.setNombre(rs.getString("nombre"));
        p.setApellido(rs.getString("apellido"));
        try {
            // fecha_nacimiento es DATE: el driver la entrega directamente como LocalDate
            p.setFechaNacimiento(rs.getObject("fecha_nacimiento", LocalDate.class));
        } catch (SQLException ex) {
            System.err.println("Advertencia: fecha_nacimiento con formato inesperado, se omite: " + ex.getMessage());
        }
        p.setSexo(rs.getString("sexo"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        p.setDireccion(rs.getString("direccion"));
        p.setTipoSangre(rs.getString("tipo_sangre"));
        String alergiasStr = rs.getString("alergias");
        if (alergiasStr != null && !alergiasStr.isEmpty()) {
            String[] partes = alergiasStr.split(",");
            p.setAlergias(java.util.Arrays.asList(partes));
        }
        try {
            // fecha_registro es TIMESTAMP: se lee como Timestamp y se convierte a LocalDate
            Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
            if (fechaRegistro != null) {
                p.setFechaRegistro(fechaRegistro.toLocalDateTime().toLocalDate());
            }
        } catch (SQLException ex) {
            System.err.println("Advertencia: fecha_registro con formato inesperado, se omite: " + ex.getMessage());
        }
        return p;
    }
}