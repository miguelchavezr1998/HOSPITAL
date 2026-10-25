package com.hospital.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Label lblUsuario;
    @FXML private Label lblSistema;
    @FXML private Button btnToggleSidebar;
    @FXML private Button btnDashboard;
    @FXML private Button btnDoctores;
    @FXML private Button btnPacientes;
    @FXML private Button btnCitas;
    @FXML private Button btnHistorial;
    @FXML private Button btnSalir;

    private boolean sidebarCollapsed = false;
    private boolean autoCollapseEnabled = false;
    private ChangeListener<Number> widthListener;

    public void setUsuario(String usuario) {
        if (lblUsuario != null) {
            lblUsuario.setText(usuario);
        }
    }

    @FXML
    public void initialize() {
        mostrarDashboard();
        configurarAutoCollapse();
    }

    private void configurarAutoCollapse() {
        sidebar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.widthProperty().removeListener(widthListener);
            }
            if (newScene != null) {
                widthListener = (obsWidth, oldWidth, newWidth) -> {
                    double ancho = newWidth.doubleValue();
                    if (ancho < 850 && !sidebarCollapsed) {
                        sidebarCollapsed = true;
                        autoCollapseEnabled = true;
                        aplicarEstadoSidebar();
                    } else if (ancho >= 850 && sidebarCollapsed && autoCollapseEnabled) {
                        sidebarCollapsed = false;
                        autoCollapseEnabled = false;
                        aplicarEstadoSidebar();
                    }
                };
                newScene.widthProperty().addListener(widthListener);
            }
        });
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleToggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        autoCollapseEnabled = false;
        aplicarEstadoSidebar();
    }

    private void aplicarEstadoSidebar() {
        if (sidebarCollapsed) {
            sidebar.getStyleClass().add("sidebar-collapsed");

            btnToggleSidebar.setText("\u2630");
            btnToggleSidebar.getStyleClass().add("toggle-sidebar-btn-collapsed");

            lblSistema.setText("\u2695");
            lblUsuario.setVisible(false);

            Button[] menuButtons = {btnDashboard, btnDoctores, btnPacientes, btnCitas, btnHistorial, btnSalir};
            for (Button btn : menuButtons) {
                btn.getStyleClass().add("menu-item-btn-collapsed");
                String shortText = btn.getText().length() > 2
                    ? btn.getText().substring(0, 2)
                    : btn.getText();
                btn.setText(shortText);
            }
        } else {
            sidebar.getStyleClass().remove("sidebar-collapsed");

            btnToggleSidebar.setText("\u2630   Menu");
            btnToggleSidebar.getStyleClass().remove("toggle-sidebar-btn-collapsed");

            lblSistema.setText("\u2695  Sistema Hospitalario");
            lblUsuario.setVisible(true);

            Button[] menuButtons = {btnDashboard, btnDoctores, btnPacientes, btnCitas, btnHistorial, btnSalir};
            String[] fullTexts = {"\uD83D\uDCCA   Dashboard", "\uD83D\uDC68   Doctores",
                                 "\uD83D\uDC64   Pacientes", "\uD83D\uDCC5   Citas Medicas",
                                 "\uD83D\uDCCB   Historial Clinico", "\u274C   Cerrar Sesion"};
            for (int i = 0; i < menuButtons.length; i++) {
                menuButtons[i].getStyleClass().remove("menu-item-btn-collapsed");
                menuButtons[i].setText(fullTexts[i]);
            }
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleDashboard() {
        actualizarMenuActivo(btnDashboard);
        mostrarDashboard();
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleDoctores() {
        actualizarMenuActivo(btnDoctores);
        cargarVista("/fxml/doctores.fxml");
    }

    @SuppressWarnings("unused")
    @FXML
    private void handlePacientes() {
        actualizarMenuActivo(btnPacientes);
        cargarVista("/fxml/pacientes.fxml");
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleCitas() {
        actualizarMenuActivo(btnCitas);
        cargarVista("/fxml/citas.fxml");
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleHistorial() {
        actualizarMenuActivo(btnHistorial);
        cargarVista("/fxml/historial.fxml");
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleSalir() {
        boolean confirmar = com.hospital.util.AlertHelper.confirmar(
            "Cerrar Sesion", "Desea cerrar la sesion?");
        if (confirmar) {
            try {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                stage.setScene(scene);
                stage.setMaximized(false);
                stage.centerOnScreen();
                stage.setTitle("Sistema Hospitalario - Login");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "No se pudo volver a la pantalla de inicio", e);
            }
        }
    }

    private void actualizarMenuActivo(Button active) {
        Button[] buttons = {btnDashboard, btnDoctores, btnPacientes, btnCitas, btnHistorial};
        for (Button btn : buttons) {
            btn.getStyleClass().remove("menu-item-btn-active");
            if (btn != active) {
                btn.getStyleClass().removeIf(s -> s.equals("menu-item-btn-active"));
            }
        }
        if (!active.getStyleClass().contains("menu-item-btn-active")) {
            active.getStyleClass().add("menu-item-btn-active");
        }
    }

    private void mostrarDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard_home.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No se pudo cargar el dashboard", e);
        }
    }

    private void cargarVista(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No se pudo cargar la vista " + fxmlPath, e);
            com.hospital.util.AlertHelper.mostrarError("Error", "No se pudo cargar la vista: " + e.getMessage());
        }
    }
}
