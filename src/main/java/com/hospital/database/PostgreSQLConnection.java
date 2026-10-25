package com.hospital.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnection {

    private static PostgreSQLConnection instance;
    private Connection connection;

    private static final String HOST = configurar("hospital.db.host", "HOSPITAL_DB_HOST", "localhost");
    private static final String PORT = configurar("hospital.db.port", "HOSPITAL_DB_PORT", "5432");
    private static final String DATABASE = configurar("hospital.db.name", "HOSPITAL_DB_NAME", "sistema_hospitalario");
    private static final String USER = configurar("hospital.db.user", "HOSPITAL_DB_USER", "postgres");
    private static final String PASSWORD = configurar("hospital.db.password", "HOSPITAL_DB_PASSWORD", "ZxCvbnm123456");

    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;

    private static String configurar(String propiedad, String variable, String valorPredeterminado) {
        String valor = System.getProperty(propiedad);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(variable);
        }
        return valor == null || valor.isBlank() ? valorPredeterminado : valor;
    }

    private PostgreSQLConnection() {
    }

    public static synchronized PostgreSQLConnection getInstance() {
        if (instance == null) {
            instance = new PostgreSQLConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (PASSWORD.isBlank()) {
                throw new SQLException("Falta HOSPITAL_DB_PASSWORD. Configure la variable de entorno y reinicie VS Code.");
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conectado a PostgreSQL exitosamente - Base de datos: " + DATABASE);
        }
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Conexion PostgreSQL cerrada.");
        }
    }
}
