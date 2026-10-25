package com.hospital.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.HistorialClinicoDAO;
import com.hospital.dao.PacienteDAO;
import com.hospital.model.Doctor;
import com.hospital.model.HistorialClinico;
import com.hospital.model.Paciente;
import com.hospital.util.AlertHelper;
import com.hospital.util.CodigosCie10Comunes;

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

public class HistorialController {

    private static final Logger LOGGER = Logger.getLogger(HistorialController.class.getName());

    @FXML private TableView<HistorialClinico> tableHistorial;
    @FXML private TableColumn<HistorialClinico, String> colFecha;
    @FXML private TableColumn<HistorialClinico, String> colPaciente;
    @FXML private TableColumn<HistorialClinico, String> colDoctor;
    @FXML private TableColumn<HistorialClinico, String> colDiagnostico;
    @FXML private TableColumn<HistorialClinico, String> colTratamiento;
    @FXML private TableColumn<HistorialClinico, String> colPresion;
    @FXML private TableColumn<HistorialClinico, String> colTemp;
    @FXML private TableColumn<HistorialClinico, Void> colAcciones;

    @FXML private VBox tableCard;
    @FXML private VBox formPanel;
    @FXML private Label formTitle;
    @FXML private ComboBox<String> cbPaciente;
    @FXML private ComboBox<String> cbDoctor;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbDiagnostico;
    @FXML private TextArea txtTratamiento;
    @FXML private TextArea txtNotas;
    @FXML private TextField txtPresion;
    @FXML private TextField txtTemperatura;
    @FXML private TextField txtPulso;
    @FXML private TextField txtPeso;
    @FXML private TextField txtEstatura;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private final HistorialClinicoDAO historialDAO = new HistorialClinicoDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private ObservableList<HistorialClinico> listaHistorial;
    private HistorialClinico historialSeleccionado = null;
    private List<Paciente> pacientesList;
    private List<Doctor> doctoresList;

    @FXML
    public void initialize() {
        cbDiagnostico.setEditable(true);
        cbDiagnostico.setItems(FXCollections.observableArrayList(CodigosCie10Comunes.LISTA));
        configurarTabla();
        cargarDatos();
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
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("pacienteNombre"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorNombre"));
        colDiagnostico.setCellValueFactory(new PropertyValueFactory<>("diagnostico"));
        colTratamiento.setCellValueFactory(new PropertyValueFactory<>("tratamiento"));
        colPresion.setCellValueFactory(new PropertyValueFactory<>("presionArterial"));
        colTemp.setCellValueFactory(new PropertyValueFactory<>("temperatura"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
                    private final Button btnVer = new Button("Ver");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox hbox = new HBox(6, btnVer, btnEliminar);

                    {
                        btnVer.getStyleClass().add("btn-outline");
                        btnEliminar.getStyleClass().add("btn-danger");
                        btnVer.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                        btnEliminar.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");

                        btnVer.setOnAction(e -> {
                            HistorialClinico h = getTableView().getItems().get(getIndex());
                            editarHistorial(h);
                        });
                        btnEliminar.setOnAction(e -> {
                            HistorialClinico h = getTableView().getItems().get(getIndex());
                            eliminarHistorial(h);
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
            listaHistorial = FXCollections.observableArrayList(historialDAO.listarTodos());
            tableHistorial.setItems(listaHistorial);
            lblTotal.setText("Total: " + listaHistorial.size() + " registros");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No se pudieron cargar los historiales clinicos", e);
            AlertHelper.mostrarError("Error de conexion", "No se pudo conectar a PostgreSQL. Verifique que el servicio esté corriendo.");
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleNuevo() {
        historialSeleccionado = null;
        formTitle.setText("Nuevo Registro Clinico");
        limpiarFormulario();
        cargarPacientesCombo();
        cargarDoctoresCombo();
        dpFecha.setValue(LocalDate.now());
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
    }

    private void editarHistorial(HistorialClinico historial) {
        historialSeleccionado = historial;
        formTitle.setText("Editar Registro Clinico");
        cargarPacientesCombo();
        cargarDoctoresCombo();
        cargarFormulario(historial);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
    }

    private void eliminarHistorial(HistorialClinico historial) {
        boolean confirmar = AlertHelper.confirmar("Eliminar Registro",
            "Desea eliminar este registro clinico?");
        if (confirmar) {
            historialDAO.eliminar(historial.getId());
            AlertHelper.mostrarExito("Eliminado", "Registro eliminado exitosamente");
            cargarDatos();
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) return;

        try {
            HistorialClinico historial = historialSeleccionado != null ? historialSeleccionado : new HistorialClinico();

            int indicePaciente = cbPaciente.getSelectionModel().getSelectedIndex();
            if (indicePaciente >= 0 && indicePaciente < pacientesList.size()) {
                Paciente p = pacientesList.get(indicePaciente);
                historial.setPacienteId(p.getId());
                historial.setPacienteNombre(p.getNombreCompleto());
            }

            int indiceDoctor = cbDoctor.getSelectionModel().getSelectedIndex();
            if (indiceDoctor >= 0 && indiceDoctor < doctoresList.size()) {
                Doctor d = doctoresList.get(indiceDoctor);
                historial.setDoctorId(d.getId());
                // Formato coherente con doctor_nombre en la BD: "Nombre Apellido" (sin prefijo)
                historial.setDoctorNombre(d.getNombre() + " " + d.getApellido());
            }

            historial.setFecha(dpFecha.getValue());
            String diagnosticoIngresado = obtenerDiagnosticoIngresado();
            historial.setDiagnostico(CodigosCie10Comunes.extraerDescripcion(diagnosticoIngresado));
            historial.setDiagnosticoCie10(CodigosCie10Comunes.extraerCodigo(diagnosticoIngresado));
            historial.setTratamiento(txtTratamiento.getText().trim());
            historial.setNotas(txtNotas.getText().trim());
            historial.setPresionArterial(txtPresion.getText().trim());
            historial.setTemperatura(txtTemperatura.getText().trim());
            historial.setPulso(txtPulso.getText().trim());
            historial.setPeso(txtPeso.getText().trim());
            historial.setEstatura(txtEstatura.getText().trim());

            if (historialSeleccionado != null) {
                historialDAO.actualizar(historial);
                AlertHelper.mostrarExito("Actualizado", "Registro actualizado exitosamente");
            } else {
                historialDAO.insertar(historial);
                AlertHelper.mostrarExito("Registrado", "Registro clinico creado exitosamente");
            }

            cerrarFormulario();
            cargarDatos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No se pudo guardar el historial clinico", e);
            AlertHelper.mostrarError("Error", "No se pudo guardar el registro: " + e.getMessage());
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
        historialSeleccionado = null;
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
            listaHistorial = FXCollections.observableArrayList(historialDAO.buscar(texto));
            tableHistorial.setItems(listaHistorial);
            lblTotal.setText("Resultados: " + listaHistorial.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No se pudieron buscar historiales clinicos", e);
            AlertHelper.mostrarError("Error", "No se pudo buscar: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cargarDatos();
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
        if (obtenerDiagnosticoIngresado().isEmpty()) {
            AlertHelper.mostrarAdvertencia("Validacion", "El diagnostico es requerido");
            return false;
        }
        if (txtTratamiento.getText().trim().isEmpty()) {
            AlertHelper.mostrarAdvertencia("Validacion", "El tratamiento es requerido");
            return false;
        }
        return true;
    }

    private String obtenerDiagnosticoIngresado() {
        String texto = cbDiagnostico.getEditor().getText();
        if (texto == null || texto.isBlank()) {
            texto = cbDiagnostico.getValue();
        }
        return texto == null ? "" : texto.trim();
    }

    private void limpiarFormulario() {
        cbPaciente.setValue(null);
        cbDoctor.setValue(null);
        dpFecha.setValue(null);
        cbDiagnostico.setValue(null);
        cbDiagnostico.getEditor().clear();
        txtTratamiento.clear();
        txtNotas.clear();
        txtPresion.clear();
        txtTemperatura.clear();
        txtPulso.clear();
        txtPeso.clear();
        txtEstatura.clear();
    }

    private void cargarFormulario(HistorialClinico h) {
        for (int i = 0; i < pacientesList.size(); i++) {
            if (pacientesList.get(i).getId().equals(h.getPacienteId())) {
                cbPaciente.getSelectionModel().select(i);
                break;
            }
        }
        for (int i = 0; i < doctoresList.size(); i++) {
            if (doctoresList.get(i).getId().equals(h.getDoctorId())) {
                cbDoctor.getSelectionModel().select(i);
                break;
            }
        }
        dpFecha.setValue(h.getFecha());
        String diagnostico = h.getDiagnostico();
        String codigo = h.getDiagnosticoCie10();
        if (codigo != null && !codigo.isBlank()) {
            String itemCatalogo = CodigosCie10Comunes.LISTA.stream()
                .filter(item -> codigo.equals(CodigosCie10Comunes.extraerCodigo(item)))
                .findFirst()
                .orElse(codigo + " - " + (diagnostico == null ? "" : diagnostico));
            cbDiagnostico.setValue(itemCatalogo);
            cbDiagnostico.getEditor().setText(itemCatalogo);
        } else {
            cbDiagnostico.setValue(diagnostico);
            cbDiagnostico.getEditor().setText(diagnostico == null ? "" : diagnostico);
        }
        txtTratamiento.setText(h.getTratamiento());
        txtNotas.setText(h.getNotas());
        txtPresion.setText(h.getPresionArterial());
        txtTemperatura.setText(h.getTemperatura());
        txtPulso.setText(h.getPulso());
        txtPeso.setText(h.getPeso());
        txtEstatura.setText(h.getEstatura());
    }
}
