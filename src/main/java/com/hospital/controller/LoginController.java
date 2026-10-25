package com.hospital.controller;

import com.hospital.dao.UsuarioDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    private static final org.slf4j.Logger ACCESS_LOGGER =
            LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private Button btnIngresar;
    @FXML private Label lblError;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @SuppressWarnings("unused")
    @FXML
    private void handleLogin() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor complete todos los campos");
            return;
        }

        try {
            if (usuarioDAO.autenticar(usuario, contrasena)) {
                ACCESS_LOGGER.info("Inicio de sesión exitoso para el usuario '{}'", usuario);
                abrirDashboard(usuario);
            } else {
                ACCESS_LOGGER.warn("Intento de inicio de sesión fallido para el usuario '{}'", usuario);
                mostrarError("Usuario o contrasena incorrectos o usuario inactivo");
            }
        } catch (SQLException e) {
            ACCESS_LOGGER.error("Error de base de datos durante el inicio de sesión", e);
            mostrarError("No se pudo conectar con la base de datos. Verifique la configuración de PostgreSQL.");
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void abrirDashboard(String usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUsuario(usuario);

            Stage stage = (Stage) btnIngresar.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle("Sistema Hospitalario - Panel Principal");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No se pudo cargar el panel principal", e);
            mostrarError("Error al cargar el sistema: " + e.getMessage());
        }
    }
}
