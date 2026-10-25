package com.hospital.dao;

import com.hospital.database.PostgreSQLConnection;
import com.hospital.model.Doctor;
import com.hospital.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoctorDAO.class);
    private static final String TABLE = "doctores";

    public void insertar(Doctor doctor) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "INSERT INTO " + TABLE + " (id, cedula, nombre, apellido, especialidad, telefono, email, horario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String idGenerado = (doctor.getId() == null || doctor.getId().isEmpty())
                    ? IdGenerator.siguiente(conn, TABLE, "doc") : doctor.getId();
            ps.setString(1, idGenerado);
            ps.setString(2, doctor.getCedula());
            ps.setString(3, doctor.getNombre());
            ps.setString(4, doctor.getApellido());
            ps.setString(5, doctor.getEspecialidad());
            ps.setString(6, doctor.getTelefono());
            ps.setString(7, doctor.getEmail());
            ps.setString(8, doctor.getHorario());
            ps.executeUpdate();
            LOGGER.info("Doctor registrado correctamente");
        } catch (SQLException e) {
            LOGGER.error("Error al registrar doctor", e);
        }
    }

    public void actualizar(Doctor doctor) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "UPDATE " + TABLE + " SET cedula=?, nombre=?, apellido=?, especialidad=?, telefono=?, email=?, horario=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, doctor.getCedula());
            ps.setString(2, doctor.getNombre());
            ps.setString(3, doctor.getApellido());
            ps.setString(4, doctor.getEspecialidad());
            ps.setString(5, doctor.getTelefono());
            ps.setString(6, doctor.getEmail());
            ps.setString(7, doctor.getHorario());
            ps.setString(8, doctor.getId());
            ps.executeUpdate();
            LOGGER.info("Registro de doctor actualizado correctamente");
        } catch (SQLException e) {
            LOGGER.error("Error al actualizar registro de doctor", e);
        }
    }

    public void eliminar(String id) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "DELETE FROM " + TABLE + " WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ps.executeUpdate();
            LOGGER.info("Registro de doctor eliminado correctamente");
        } catch (SQLException e) {
            LOGGER.error("Error al eliminar registro de doctor", e);
        }
    }

    public Doctor buscarPorId(String id) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearDoctor(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar doctor por ID: " + e.getMessage());
            return null;
        }
    }

    public Doctor buscarPorCedula(String cedula) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE cedula=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearDoctor(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar doctor por cedula: " + e.getMessage());
            return null;
        }
    }

    public List<Doctor> listarTodos() {
        List<Doctor> doctores = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                doctores.add(mapearDoctor(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar doctores: " + e.getMessage());
        }
        return doctores;
    }

    public List<Doctor> buscar(String texto) {
        List<Doctor> doctores = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE LOWER(nombre) LIKE LOWER(?) OR LOWER(apellido) LIKE LOWER(?) OR LOWER(especialidad) LIKE LOWER(?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String likePattern = "%" + texto + "%";
            ps.setString(1, likePattern);
            ps.setString(2, likePattern);
            ps.setString(3, likePattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                doctores.add(mapearDoctor(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar doctores: " + e.getMessage());
        }
        return doctores;
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
            System.err.println("Error al contar doctores: " + e.getMessage());
        }
        return 0;
    }

    private Doctor mapearDoctor(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getString("id"));
        d.setCedula(rs.getString("cedula"));
        d.setNombre(rs.getString("nombre"));
        d.setApellido(rs.getString("apellido"));
        d.setEspecialidad(rs.getString("especialidad"));
        d.setTelefono(rs.getString("telefono"));
        d.setEmail(rs.getString("email"));
        d.setHorario(rs.getString("horario"));
        return d;
    }
}