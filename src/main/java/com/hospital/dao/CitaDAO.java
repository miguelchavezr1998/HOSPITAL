package com.hospital.dao;

import com.hospital.database.PostgreSQLConnection;
import com.hospital.model.Cita;
import com.hospital.util.IdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO {

    private static final String TABLE = "citas";
    // Formato visual de hora usado por la interfaz (sin segundos)
    private static final DateTimeFormatter HORA_UI = DateTimeFormatter.ofPattern("HH:mm");

    public boolean insertar(Cita cita) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "INSERT INTO " + TABLE + " (id, paciente_id, doctor_id, paciente_nombre, doctor_nombre, fecha, hora, motivo, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String idGenerado = (cita.getId() == null || cita.getId().isEmpty())
                    ? IdGenerator.siguiente(conn, TABLE, "cita") : cita.getId();
            ps.setString(1, idGenerado);
            ps.setString(2, cita.getPacienteId());
            ps.setString(3, cita.getDoctorId());
            ps.setString(4, cita.getPacienteNombre());
            ps.setString(5, cita.getDoctorNombre());
            // fecha es DATE en PostgreSQL: se envia como LocalDate, no como String
            ps.setObject(6, cita.getFecha());
            // hora es TIME en PostgreSQL: se envia como LocalTime, no como String
            ps.setObject(7, toLocalTime(cita.getHora()));
            ps.setString(8, cita.getMotivo());
            ps.setString(9, cita.getEstado() != null ? cita.getEstado().getLabel() : "Programada");
            ps.setString(10, cita.getObservaciones());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // Violacion de UNIQUE (doctor_id, fecha, hora): doble agenda
                System.err.println("Doble agenda detectada: " + e.getMessage());
            } else {
                System.err.println("Error al insertar cita: " + e.getMessage());
            }
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Error al insertar cita: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Cita cita) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "UPDATE " + TABLE + " SET paciente_id=?, doctor_id=?, paciente_nombre=?, doctor_nombre=?, fecha=?, hora=?, motivo=?, estado=?, observaciones=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cita.getPacienteId());
            ps.setString(2, cita.getDoctorId());
            ps.setString(3, cita.getPacienteNombre());
            ps.setString(4, cita.getDoctorNombre());
            // fecha es DATE en PostgreSQL: se envia como LocalDate, no como String
            ps.setObject(5, cita.getFecha());
            // hora es TIME en PostgreSQL: se envia como LocalTime, no como String
            ps.setObject(6, toLocalTime(cita.getHora()));
            ps.setString(7, cita.getMotivo());
            ps.setString(8, cita.getEstado() != null ? cita.getEstado().getLabel() : "Programada");
            ps.setString(9, cita.getObservaciones());
            ps.setString(10, cita.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // Violacion de UNIQUE (doctor_id, fecha, hora): doble agenda
                System.err.println("Doble agenda detectada: " + e.getMessage());
            } else {
                System.err.println("Error al actualizar cita: " + e.getMessage());
            }
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Error al actualizar cita: " + e.getMessage());
            return false;
        }
    }

    public void eliminar(String id) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "DELETE FROM " + TABLE + " WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar cita: " + e.getMessage());
        }
    }

    public Cita buscarPorId(String id) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearCita(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error al buscar cita por ID: " + e.getMessage());
            return null;
        }
    }

    public List<Cita> listarTodas() {
        List<Cita> citas = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE;
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                citas.add(mapearCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar citas: " + e.getMessage());
        }
        return citas;
    }

    public List<Cita> buscarPorPaciente(String pacienteId) {
        List<Cita> citas = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE paciente_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, pacienteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                citas.add(mapearCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar citas por paciente: " + e.getMessage());
        }
        return citas;
    }

    public List<Cita> buscarPorDoctor(String doctorId) {
        List<Cita> citas = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE doctor_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                citas.add(mapearCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar citas por doctor: " + e.getMessage());
        }
        return citas;
    }

    public List<Cita> buscarPorEstado(String estado) {
        List<Cita> citas = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE estado=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, estado);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                citas.add(mapearCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar citas por estado: " + e.getMessage());
        }
        return citas;
    }

    public List<Cita> buscar(String texto) {
        List<Cita> citas = new ArrayList<>();
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + TABLE + " WHERE LOWER(paciente_nombre) LIKE LOWER(?) OR LOWER(doctor_nombre) LIKE LOWER(?) OR LOWER(motivo) LIKE LOWER(?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String likePattern = "%" + texto + "%";
            ps.setString(1, likePattern);
            ps.setString(2, likePattern);
            ps.setString(3, likePattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                citas.add(mapearCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar citas: " + e.getMessage());
        }
        return citas;
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
            System.err.println("Error al contar citas: " + e.getMessage());
        }
        return 0;
    }

    public long contarPorEstado(String estado) {
        try (Connection conn = PostgreSQLConnection.getInstance().getConnection()) {
            String sql = "SELECT COUNT(*) FROM " + TABLE + " WHERE estado=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, estado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar citas por estado: " + e.getMessage());
        }
        return 0;
    }

    private Cita mapearCita(ResultSet rs) throws SQLException {
        Cita c = new Cita();
        c.setId(rs.getString("id"));
        c.setPacienteId(rs.getString("paciente_id"));
        c.setDoctorId(rs.getString("doctor_id"));
        c.setPacienteNombre(rs.getString("paciente_nombre"));
        c.setDoctorNombre(rs.getString("doctor_nombre"));
        // fecha es DATE en PostgreSQL: se lee directamente como LocalDate
        try {
            c.setFecha(rs.getObject("fecha", java.time.LocalDate.class));
        } catch (SQLException ex) {
            System.err.println("Advertencia: fecha de cita con formato inesperado, se omite: " + ex.getMessage());
        }
        // hora es TIME en PostgreSQL: se lee como LocalTime y se normaliza a "HH:mm"
        c.setHora(leerHora(rs));
        c.setMotivo(rs.getString("motivo"));
        c.setEstado(Cita.Estado.fromString(rs.getString("estado")));
        c.setObservaciones(rs.getString("observaciones"));
        return c;
    }

    /** Convierte la hora de la interfaz ("HH:mm", "H:mm" o "HH:mm:ss") a LocalTime para la columna TIME. */
    private LocalTime toLocalTime(String hora) {
        if (hora == null || hora.trim().isEmpty()) {
            return null;
        }
        String h = hora.trim();
        try {
            return LocalTime.parse(h); // ISO: "HH:mm" u "HH:mm:ss"
        } catch (Exception e) {
            try {
                String[] partes = h.split(":");
                int hh = Integer.parseInt(partes[0].trim());
                int mm = partes.length > 1 ? Integer.parseInt(partes[1].trim()) : 0;
                return LocalTime.of(hh, mm); // tolera "9:30"
            } catch (Exception ex) {
                throw new IllegalArgumentException("Formato de hora invalido: '" + hora + "'. Use el formato HH:mm (ejemplo: 09:30)");
            }
        }
    }

    /** Lee la columna TIME y la devuelve como texto "HH:mm" para la interfaz, con respaldo defensivo. */
    private String leerHora(ResultSet rs) throws SQLException {
        try {
            LocalTime hora = rs.getObject("hora", LocalTime.class);
            return hora != null ? hora.format(HORA_UI) : "";
        } catch (SQLException ex) {
            // Respaldo: leer como texto y recortar los segundos si vienen
            try {
                String cruda = rs.getString("hora");
                if (cruda == null) return "";
                return cruda.length() >= 5 ? cruda.substring(0, 5) : cruda;
            } catch (SQLException ex2) {
                System.err.println("Advertencia: hora de cita ilegible, se omite: " + ex2.getMessage());
                return "";
            }
        }
    }
}