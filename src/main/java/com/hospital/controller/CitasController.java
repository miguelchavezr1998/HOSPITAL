package com.hospital.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hospital.dao.CitaDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PacienteDAO;
import com.hospital.model.Cita;
import com.hospital.model.Doctor;
import com.hospital.model.Paciente;
import com.hospital.util.AlertHelper;
import com.hospital.util.ExcelExporter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CitasController {

    private static final Logger LOGGER = Logger.getLogger(CitasController.class.getName());

    @FXML private TableView<Cita> tableCitas;
    @FXML private TableColumn<Cita, String> colFecha;
    @FXML private TableColumn<Cita, String> colHora;
    @FXML private TableColumn<Cita, String> colPaciente;
    @FXML private TableColumn<Cita, String> colDoctor;
    @FXML private TableColumn<Cita, String> colMotivo;
    @FXML private TableColumn<Cita, String> colEstado;
    @FXML private TableColumn<Cita, Void> colAcciones;

    @FXML private VBox tableCard;
    @FXML private VBox formPanel;
    @FXML private Label formTitle;
    @FXML private ComboBox<String> cbPaciente;
    @FXML private ComboBox<String> cbDoctor;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbHora;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TextField txtMotivo;
    @FXML private TextArea txtObservaciones;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroEstado;
    @FXML private Label lblTotal;

    private final CitaDAO citaDAO = new CitaDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private ObservableList<Cita> listaCitas;
    private Cita citaSeleccionada = null;
    private List<Paciente> pacientesList;
    private List<Doctor> doctoresList;

    @FXML
    public void initialize() {
        configurarComboBoxes();
        configurarTabla();
        cargarDatos();
    }

    private void configurarComboBoxes() {
        cbHora.getItems().addAll(
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
            "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
            "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
        );

        cbEstado.getItems().addAll("Programada", "Completada", "Cancelada");
        cbEstado.setValue("Programada");

        cbFiltroEstado.getItems().addAll("Todas", "Programada", "Completada", "Cancelada");
        cbFiltroEstado.setValue("Todas");
        cbFiltroEstado.setOnAction(e -> handleFiltrarEstado());

        cargarPacientesCombo();
        cargarDoctoresCombo();
    }

    private void cargarPacientesCombo() {
        pacientesList = pacienteDAO.listarTodos();
        cbPaciente.getItems().clear();
        for (Paciente p : pacientesList) {
            cbPaciente.getItems().add(p.getNombreCompleto() + " (" + p.getCedula() + ")");
        }
    }

    private void cargarDoctoresCombo() {
        doctoresList = doctorDAO.listarTodos();
        cbDoctor.getItems().clear();
        for (Doctor d : doctoresList) {
            cbDoctor.getItems().add(d.getNombreCompleto() + " - " + d.getEspecialidad());
        }
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFecha();
            return new SimpleStringProperty(fecha != null ? fecha.toString() : "");
        });
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("pacienteNombre"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorNombre"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colEstado.setCellValueFactory(cellData -> {
            Cita.Estado estado = cellData.getValue().getEstado();
            return new SimpleStringProperty(estado != null ? estado.getLabel() : "");
        });

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
                            Cita cita = getTableView().getItems().get(getIndex());
                            editarCita(cita);
                        });
                        btnEliminar.setOnAction(e -> {
                            Cita cita = getTableView().getItems().get(getIndex());
                            eliminarCita(cita);
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
            listaCitas = FXCollections.observableArrayList(citaDAO.listarTodas());
            tableCitas.setItems(listaCitas);
            lblTotal.setText("Total: " + listaCitas.size() + " citas");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No se pudieron cargar las citas", e);
            AlertHelper.mostrarError("Error de conexion", "No se pudo conectar a PostgreSQL. Verifique que el servicio esté corriendo.");
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleNueva() {
        citaSeleccionada = null;
        formTitle.setText("Nueva Cita");
        limpiarFormulario();
        cargarPacientesCombo();
        cargarDoctoresCombo();
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
    }

    private void editarCita(Cita cita) {
        citaSeleccionada = cita;
        formTitle.setText("Editar Cita");
        cargarPacientesCombo();
        cargarDoctoresCombo();
        cargarFormulario(cita);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
    }

    private void eliminarCita(Cita cita) {
        boolean confirmar = AlertHelper.confirmar("Eliminar Cita",
            "Desea eliminar esta cita?");
        if (confirmar) {
            citaDAO.eliminar(cita.getId());
            AlertHelper.mostrarExito("Eliminado", "Cita eliminada exitosamente");
            cargarDatos();
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) return;

        try {
            Cita cita = citaSeleccionada != null ? citaSeleccionada : new Cita();

            int indicePaciente = cbPaciente.getSelectionModel().getSelectedIndex();
            if (indicePaciente >= 0 && indicePaciente < pacientesList.size()) {
                Paciente p = pacientesList.get(indicePaciente);
                cita.setPacienteId(p.getId());
                cita.setPacienteNombre(p.getNombreCompleto());
            }

            int indiceDoctor = cbDoctor.getSelectionModel().getSelectedIndex();
            if (indiceDoctor >= 0 && indiceDoctor < doctoresList.size()) {
                Doctor d = doctoresList.get(indiceDoctor);
                cita.setDoctorId(d.getId());
                // Formato coherente con doctor_nombre en la BD: "Nombre Apellido" (sin prefijo)
                cita.setDoctorNombre(d.getNombre() + " " + d.getApellido());
            }

            cita.setFecha(dpFecha.getValue());
            cita.setHora(cbHora.getValue());
            cita.setMotivo(txtMotivo.getText().trim());
            cita.setEstado(Cita.Estado.fromString(cbEstado.getValue()));
            cita.setObservaciones(txtObservaciones.getText().trim());

            if (citaSeleccionada != null) {
                boolean ok = citaDAO.actualizar(cita);
                if (!ok) {
                    AlertHelper.mostrarError("No se pudo actualizar",
                        "Verifique que el doctor no tenga ya una cita en esa fecha y hora, "
                        + "que la hora tenga formato HH:mm (ejemplo: 09:30) y que la conexión "
                        + "a la base de datos esté activa.");
                    return;
                }
                AlertHelper.mostrarExito("Actualizada", "Cita actualizada exitosamente");
            } else {
                boolean ok = citaDAO.insertar(cita);
                if (!ok) {
                    AlertHelper.mostrarError("No se pudo guardar",
                        "Verifique que el doctor no tenga ya una cita en esa fecha y hora, "
                        + "que la hora tenga formato HH:mm (ejemplo: 09:30) y que la conexión "
                        + "a la base de datos esté activa.");
                    return;
                }
                AlertHelper.mostrarExito("Creada", "Cita programada exitosamente");
            }

            cerrarFormulario();
            cargarDatos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No se pudo guardar la cita", e);
            AlertHelper.mostrarError("Error", "No se pudo guardar la cita: " + e.getMessage());
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
        citaSeleccionada = null;
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
            listaCitas = FXCollections.observableArrayList(citaDAO.buscar(texto));
            tableCitas.setItems(listaCitas);
            lblTotal.setText("Resultados: " + listaCitas.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No se pudieron buscar las citas", e);
            AlertHelper.mostrarError("Error", "No se pudo buscar: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cbFiltroEstado.setValue("Todas");
        cargarDatos();
    }

    @FXML
    private void handleFiltrarEstado() {
        String estado = cbFiltroEstado.getValue();
        if (estado == null || estado.equals("Todas")) {
            cargarDatos();
        } else {
            listaCitas = FXCollections.observableArrayList(citaDAO.buscarPorEstado(estado));
            tableCitas.setItems(listaCitas);
            lblTotal.setText("Resultados: " + listaCitas.size());
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleExportarExcel() {
        if (tableCitas.getItems().isEmpty()) {
            AlertHelper.mostrarAdvertencia("Sin registros", "No hay citas para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar citas a Excel");
        fileChooser.setInitialFileName("citas.xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"));
        File archivo = fileChooser.showSaveDialog(tableCitas.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".xlsx");
        }

        List<List<String>> filas = new ArrayList<>();
        for (Cita cita : tableCitas.getItems()) {
            filas.add(Arrays.asList(
                    cita.getFecha() != null ? cita.getFecha().toString() : "",
                    cita.getHora(), cita.getPacienteNombre(), cita.getDoctorNombre(), cita.getMotivo(),
                    cita.getEstado() != null ? cita.getEstado().getLabel() : "",
                    cita.getObservaciones()));
        }

        try {
            ExcelExporter.exportar("Citas",
                    List.of("Fecha", "Hora", "Paciente", "Doctor", "Motivo", "Estado", "Observaciones"),
                    filas, archivo.toPath());
            AlertHelper.mostrarExito("Exportacion completada",
                    "Se exportaron " + filas.size() + " citas a:\n" + archivo.getAbsolutePath());
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "No se pudieron exportar las citas a Excel", e);
            AlertHelper.mostrarError("Error de exportacion", "No se pudo crear el archivo Excel: " + e.getMessage());
        }
    }

    private boolean validarFormulario() {
        if (cbPaciente.getSelectionModel().getSelectedIndex() < 0) {
            AlertHelper.mostrarAdvertencia("Validacion", "Seleccione un paciente");
            return false;
        }
        if (cbDoctor.getSelectionModel().getSelectedIndex() < 0) {
            AlertHelper.mostrarAdvertencia("Validacion", "Seleccione un doctor");
            return false;
        }
        if (dpFecha.getValue() == null) {
            AlertHelper.mostrarAdvertencia("Validacion", "Seleccione una fecha");
            return false;
        }
        if (cbHora.getValue() == null) {
            AlertHelper.mostrarAdvertencia("Validacion", "Seleccione una hora");
            return false;
        }
        if (txtMotivo.getText().trim().isEmpty()) {
            AlertHelper.mostrarAdvertencia("Validacion", "El motivo es requerido");
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        cbPaciente.setValue(null);
        cbDoctor.setValue(null);
        dpFecha.setValue(null);
        cbHora.setValue(null);
        cbEstado.setValue("Programada");
        txtMotivo.clear();
        txtObservaciones.clear();
    }

    private void cargarFormulario(Cita c) {
        for (int i = 0; i < pacientesList.size(); i++) {
            if (pacientesList.get(i).getId().equals(c.getPacienteId())) {
                cbPaciente.getSelectionModel().select(i);
                break;
            }
        }
        for (int i = 0; i < doctoresList.size(); i++) {
            if (doctoresList.get(i).getId().equals(c.getDoctorId())) {
                cbDoctor.getSelectionModel().select(i);
                break;
            }
        }
        dpFecha.setValue(c.getFecha());
        // Asegurar que la hora de la cita exista entre las opciones del combo
        String hora = c.getHora();
        if (hora != null && !hora.isEmpty() && !cbHora.getItems().contains(hora)) {
            cbHora.getItems().add(hora);
        }
        cbHora.setValue(hora);
        cbEstado.setValue(c.getEstado().getLabel());
        txtMotivo.setText(c.getMotivo());
        txtObservaciones.setText(c.getObservaciones());
    }
}
