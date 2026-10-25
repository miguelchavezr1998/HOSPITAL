package com.hospital.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hospital.database.PostgreSQLConnection;
import com.hospital.model.HistorialClinico;
import com.hospital.util.IdGenerator;

public class HistorialClinicoDAO {

    private static final String TABLE = "historial_clinico";

    public void insertar(HistorialClinico historial) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            // CORREGIDO: antes habia 14 columnas y solo 13 valores (?, ?, ...). Falta el id como PK.
            String sql = "INSERT INTO " + TABLE + " (id, paciente_id, doctor_id, cita_id, paciente_nombre, doctor_nombre, fecha, diagnostico, diagnostico_cie10, tratamiento, notas, presion_arterial, temperatura, pulso, peso, estatura) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String idGenerado = (historial.getId() == null || historial.getId().isEmpty())
                    ? IdGenerator.siguiente(conn, TABLE, "hist") : historial.getId();
            ps.setString(1, idGenerado);
            ps.setString(2, historial.getPacienteId());
            ps.setString(3, historial.getDoctorId());
            ps.setString(4, historial.getCitaId());
            ps.setString(5, historial.getPacienteNombre());
            ps.setString(6, historial.getDoctorNombre());
            // fecha es DATE en PostgreSQL: se envia como LocalDate, no como String
            ps.setObject(7, historial.getFecha());
            ps.setString(8, historial.getDiagnostico());
            ps.setString(9, historial.getDiagnosticoCie10());
            ps.setString(10, historial.getTratamiento());
            ps.setString(11, historial.getNotas());
            ps.setString(12, historial.getPresionArterial());
            ps.setString(13, historial.getTemperatura());
            ps.setString(14, historial.getPulso());
            ps.setString(15, historial.getPeso());
            ps.setString(16, historial.getEstatura());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al insertar historial clinico: " + e.getMessage());
        }
    }

    public void actualizar(HistorialClinico historial) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "UPDATE " + TABLE + " SET paciente_id=?, doctor_id=?, cita_id=?, paciente_nombre=?, doctor_nombre=?, fecha=?, diagnostico=?, diagnostico_cie10=?, tratamiento=?, notas=?, presion_arterial=?, temperatura=?, pulso=?, peso=?, estatura=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, historial.getPacienteId());
            ps.setString(2, historial.getDoctorId());
            ps.setString(3, historial.getCitaId());
            ps.setString(4, historial.getPacienteNombre());
            ps.setString(5, historial.getDoctorNombre());
            // fecha es DATE en PostgreSQL: se envia como LocalDate, no como String
            ps.setObject(6, historial.getFecha());
            ps.setString(7, historial.getDiagnostico());
            ps.setString(8, historial.getDiagnosticoCie10());
            ps.setString(9, historial.getTratamiento());
            ps.setString(10, historial.getNotas());
            ps.setString(11, historial.getPresionArterial());
            ps.setString(12, historial.getTemperatura());
            ps.setString(13, historial.getPulso());
            ps.setString(14, historial.getPeso());
            ps.setString(15, historial.getEstatura());
            ps.setString(16, historial.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar historial clinico: " + e.getMessage());
        }
    }

    public void eliminar(String id) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "DELETE FROM " + TABLE + " WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar historial clinico: " + e.getMessage());
        }
    }

    public HistorialClinico buscarPorId(String id) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearHistorialClinico(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar historial clinico por ID: " + e.getMessage());
            return null;
        }
    }

    public List<HistorialClinico> listarTodos() {
        List<HistorialClinico> historiales = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                historiales.add(mapearHistorialClinico(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar historiales clinicos: " + e.getMessage());
        }
        return historiales;
    }

    public List<HistorialClinico> buscarPorPaciente(String pacienteId) {
        List<HistorialClinico> historiales = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE paciente_id=? ORDER BY fecha DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pacienteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                historiales.add(mapearHistorialClinico(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar historiales por paciente: " + e.getMessage());
        }
        return historiales;
    }

    public List<HistorialClinico> buscarPorDoctor(String doctorId) {
        List<HistorialClinico> historiales = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE doctor_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                historiales.add(mapearHistorialClinico(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar historiales por doctor: " + e.getMessage());
        }
        return historiales;
    }

    public List<HistorialClinico> buscar(String texto) {
        List<HistorialClinico> historiales = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE LOWER(paciente_nombre) LIKE LOWER(?) OR LOWER(doctor_nombre) LIKE LOWER(?) OR LOWER(diagnostico) LIKE LOWER(?) OR LOWER(tratamiento) LIKE LOWER(?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String likePattern = "%" + texto + "%";
            ps.setString(1, likePattern);
            ps.setString(2, likePattern);
            ps.setString(3, likePattern);
            ps.setString(4, likePattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                historiales.add(mapearHistorialClinico(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar historiales clinicos: " + e.getMessage());
        }
        return historiales;
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
            System.err.println("Error al contar historiales clinicos: " + e.getMessage());
        }
        return 0;
    }

    private HistorialClinico mapearHistorialClinico(ResultSet rs) throws SQLException {
        HistorialClinico h = new HistorialClinico();
        h.setId(rs.getString("id"));
        h.setPacienteId(rs.getString("paciente_id"));
        h.setDoctorId(rs.getString("doctor_id"));
        h.setCitaId(rs.getString("cita_id"));
        h.setPacienteNombre(rs.getString("paciente_nombre"));
        h.setDoctorNombre(rs.getString("doctor_nombre"));
        // fecha es DATE en PostgreSQL: se lee directamente como LocalDate
        try {
            h.setFecha(rs.getObject("fecha", java.time.LocalDate.class));
        } catch (SQLException ex) {
            System.err.println("Advertencia: fecha de historial con formato inesperado, se omite: " + ex.getMessage());
        }
        h.setDiagnostico(rs.getString("diagnostico"));
        h.setDiagnosticoCie10(rs.getString("diagnostico_cie10"));
        h.setTratamiento(rs.getString("tratamiento"));
        h.setNotas(rs.getString("notas"));
        h.setPresionArterial(rs.getString("presion_arterial"));
        h.setTemperatura(rs.getString("temperatura"));
        h.setPulso(rs.getString("pulso"));
        h.setPeso(rs.getString("peso"));
        h.setEstatura(rs.getString("estatura"));
        return h;
    }
}