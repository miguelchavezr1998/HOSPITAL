package com.hospital.controller;

import com.hospital.dao.*;
import com.hospital.model.Cita;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class DashboardHomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardHomeController.class);

    @FXML private FlowPane statsRow;
    @FXML private FlowPane infoCardsRow;

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final CitaDAO citaDAO = new CitaDAO();
    private final HistorialClinicoDAO historialDAO = new HistorialClinicoDAO();

    @FXML
    public void initialize() {
        cargarEstadisticas();
    }

    public void cargarEstadisticas() {
        try {
            long numPacientes = pacienteDAO.contar();
            long numDoctores = doctorDAO.contar();
            List<Cita> citas = citaDAO.listarTodas();
            long numCitas = citas.size();
            long numHistorial = historialDAO.contar();
            Multiset<Cita.Estado> citasPorEstado = HashMultiset.create();
            for (Cita cita : citas) {
                if (cita.getEstado() != null) {
                    citasPorEstado.add(cita.getEstado());
                }
            }

            statsRow.getChildren().clear();

            statsRow.getChildren().add(crearStatCard(
                String.valueOf(numPacientes), "Pacientes", ""));
            statsRow.getChildren().add(crearStatCard(
                String.valueOf(numDoctores), "Doctores", "-success"));
            statsRow.getChildren().add(crearStatCard(
                String.valueOf(numCitas), "Total Citas", "-info"));
            statsRow.getChildren().add(crearStatCard(
                String.valueOf(citasPorEstado.count(Cita.Estado.PROGRAMADA)),
                "Citas Pendientes", "-warning"));
            statsRow.getChildren().add(crearStatCard(
                String.valueOf(citasPorEstado.count(Cita.Estado.COMPLETADA)),
                "Citas Completadas", "-success"));
            statsRow.getChildren().add(crearStatCard(
                String.valueOf(citasPorEstado.count(Cita.Estado.CANCELADA)),
                "Citas Canceladas", "-warning"));
            statsRow.getChildren().add(crearStatCard(
                String.valueOf(numHistorial), "Registros Clinicos", ""));
        } catch (Exception e) {
            LOGGER.error("No se pudieron cargar las estadisticas del dashboard", e);
        }
    }

    private VBox crearStatCard(String numero, String label, String colorClass) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card" + colorClass);
        card.setPadding(new Insets(20));
        card.setMinWidth(180);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefWidth(200);

        Label lblNumero = new Label(numero);
        lblNumero.getStyleClass().add("stat-number");

        Label lblLabel = new Label(label);
        lblLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(lblNumero, lblLabel);
        return card;
    }
}
