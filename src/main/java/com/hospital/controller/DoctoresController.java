package com.hospital.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;
import com.hospital.util.AlertHelper;
import com.hospital.util.ExcelExporter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoctoresController {

    private static final Logger LOGGER = Logger.getLogger(DoctoresController.class.getName());

    @FXML private TableView<Doctor> tableDoctores;
    @FXML private TableColumn<Doctor, String> colCedula;
    @FXML private TableColumn<Doctor, String> colNombre;
    @FXML private TableColumn<Doctor, String> colEspecialidad;
    @FXML private TableColumn<Doctor, String> colTelefono;
    @FXML private TableColumn<Doctor, String> colEmail;
    @FXML private TableColumn<Doctor, String> colHorario;
    @FXML private TableColumn<Doctor, Void> colAcciones;

    @FXML private VBox tableCard;
    @FXML private VBox formPanel;
    @FXML private Label formTitle;
    @FXML private TextField txtCedula;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEspecialidad;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtHorario;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private ObservableList<Doctor> listaDoctores;
    private Doctor doctorSeleccionado = null;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarDatos();
    }

    private void configurarTabla() {
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colEspecialidad.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox hbox = new HBox(6, btnEditar, btnEliminar);

                    {
                        btnEditar.getStyleClass().add("btn-outline");
                        btnEliminar.getStyleClass().add("btn-danger");
                        btnEditar.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                        btnEliminar.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");

                        btnEditar.setOnAction(e -> {
                            Doctor doctor = getTableView().getItems().get(getIndex());
                            editarDoctor(doctor);
                        });
                        btnEliminar.setOnAction(e -> {
                            Doctor doctor = getTableView().getItems().get(getIndex());
                            eliminarDoctor(doctor);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : hbox);
                    }
        });
    }

    private void cargarDatos() {
        try {
            listaDoctores = FXCollections.observableArrayList(doctorDAO.listarTodos());
            tableDoctores.setItems(listaDoctores);
            lblTotal.setText("Total: " + listaDoctores.size() + " doctores");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No se pudieron cargar los doctores", e);
            AlertHelper.mostrarError("Error de conexion", "No se pudo conectar a PostgreSQL. Verifique que el servicio esté corriendo.");
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleNuevo() {
        doctorSeleccionado = null;
        formTitle.setText("Nuevo Doctor");
        limpiarFormulario();
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
    }

    private void editarDoctor(Doctor doctor) {
        doctorSeleccionado = doctor;
        formTitle.setText("Editar Doctor");
        cargarFormulario(doctor);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
    }

    private void eliminarDoctor(Doctor doctor) {
        boolean confirmar = AlertHelper.confirmar("Eliminar Doctor",
            "Desea eliminar a " + doctor.getNombreCompleto() + "?");
        if (confirmar) {
            try {
                doctorDAO.eliminar(doctor.getId());
                AlertHelper.mostrarExito("Eliminado", "Doctor eliminado exitosamente");
                cargarDatos();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "No se pudo eliminar el doctor", e);
                AlertHelper.mostrarError("Error", "No se pudo eliminar: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) return;

        try {
            Doctor doctor = doctorSeleccionado != null ? doctorSeleccionado : new Doctor();
            doctor.setCedula(txtCedula.getText().trim());
            doctor.setNombre(txtNombre.getText().trim());
            doctor.setApellido(txtApellido.getText().trim());
            doctor.setEspecialidad(txtEspecialidad.getText().trim());
            doctor.setTelefono(txtTelefono.getText().trim());
            doctor.setEmail(txtEmail.getText().trim());
            doctor.setHorario(txtHorario.getText().trim());

            if (doctorSeleccionado != null) {
                doctorDAO.actualizar(doctor);
                AlertHelper.mostrarExito("Actualizado", "Doctor actualizado exitosamente");
            } else {
                doctorDAO.insertar(doctor);
                AlertHelper.mostrarExito("Registrado", "Doctor registrado exitosamente");
            }

            cerrarFormulario();
            cargarDatos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No se pudo guardar el doctor", e);
            AlertHelper.mostrarError("Error", "No se pudo guardar el doctor: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleCerrarForm() {
        cerrarFormulario();
    }

    private void cerrarFormulario() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        tableCard.setVisible(true);
        tableCard.setManaged(true);
        doctorSeleccionado = null;
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleBuscar() {
        try {
            String texto = txtBuscar.getText().trim();
            if (texto.isEmpty()) {
                cargarDatos();
                return;
            }
            listaDoctores = FXCollections.observableArrayList(doctorDAO.buscar(texto));
            tableDoctores.setItems(listaDoctores);
            lblTotal.setText("Resultados: " + listaDoctores.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No se pudieron buscar los doctores", e);
            AlertHelper.mostrarError("Error", "No se pudo buscar: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cargarDatos();
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleExportarExcel() {
        if (tableDoctores.getItems().isEmpty()) {
            AlertHelper.mostrarAdvertencia("Sin registros", "No hay doctores para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar doctores a Excel");
        fileChooser.setInitialFileName("doctores.xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"));
        File archivo = fileChooser.showSaveDialog(tableDoctores.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".xlsx");
        }

        List<List<String>> filas = new ArrayList<>();
        for (Doctor doctor : tableDoctores.getItems()) {
            filas.add(Arrays.asList(doctor.getCedula(), doctor.getNombreCompleto(),
                    doctor.getEspecialidad(), doctor.getTelefono(), doctor.getEmail(), doctor.getHorario()));
        }

        try {
            ExcelExporter.exportar("Doctores",
                    List.of("Cedula", "Nombre completo", "Especialidad", "Telefono", "Email", "Horario"),
                    filas, archivo.toPath());
            AlertHelper.mostrarExito("Exportacion completada",
                    "Se exportaron " + filas.size() + " doctores a:\n" + archivo.getAbsolutePath());
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "No se pudieron exportar los doctores a Excel", e);
            AlertHelper.mostrarError("Error de exportacion", "No se pudo crear el archivo Excel: " + e.getMessage());
        }
    }

    private boolean validarFormulario() {
        if (StringUtils.isBlank(txtCedula.getText())) {
            AlertHelper.mostrarAdvertencia("Validacion", "La cedula es requerida");
            return false;
        }
        if (StringUtils.isBlank(txtNombre.getText())) {
            AlertHelper.mostrarAdvertencia("Validacion", "El nombre es requerido");
            return false;
        }
        if (StringUtils.isBlank(txtApellido.getText())) {
            AlertHelper.mostrarAdvertencia("Validacion", "El apellido es requerido");
            return false;
        }
        if (StringUtils.isBlank(txtEspecialidad.getText())) {
            AlertHelper.mostrarAdvertencia("Validacion", "La especialidad es requerida");
            return false;
        }
        if (StringUtils.isBlank(txtEmail.getText())) {
            AlertHelper.mostrarAdvertencia("Validacion", "El email es requerido");
            return false;
        }
        if (StringUtils.isBlank(txtHorario.getText())) {
            AlertHelper.mostrarAdvertencia("Validacion", "El horario es requerido");
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        txtCedula.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtEspecialidad.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtHorario.clear();
    }

    private void cargarFormulario(Doctor d) {
        txtCedula.setText(d.getCedula());
        txtNombre.setText(d.getNombre());
        txtApellido.setText(d.getApellido());
        txtEspecialidad.setText(d.getEspecialidad());
        txtTelefono.setText(d.getTelefono());
        txtEmail.setText(d.getEmail());
        txtHorario.setText(d.getHorario());
    }
}
