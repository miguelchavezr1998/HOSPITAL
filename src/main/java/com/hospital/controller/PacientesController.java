package com.hospital.controller;

import com.hospital.dao.PacienteDAO;
import com.hospital.model.Paciente;
import com.hospital.util.AlertHelper;
import com.hospital.util.ExcelExporter;
import com.hospital.validation.PacienteValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacientesController {

    private static final Logger LOGGER = Logger.getLogger(PacientesController.class.getName());

    @FXML private TableView<Paciente> tablePacientes;
    @FXML private TableColumn<Paciente, String> colCedula;
    @FXML private TableColumn<Paciente, String> colNombre;
    @FXML private TableColumn<Paciente, String> colFechaNac;
    @FXML private TableColumn<Paciente, String> colSexo;
    @FXML private TableColumn<Paciente, String> colTelefono;
    @FXML private TableColumn<Paciente, String> colSangre;
    @FXML private TableColumn<Paciente, String> colAlergias;
    @FXML private TableColumn<Paciente, Void> colAcciones;

    @FXML private VBox tableCard;
    @FXML private VBox formPanel;
    @FXML private Label formTitle;
    @FXML private TextField txtCedula;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<String> cbSexo;
    @FXML private ComboBox<String> cbTipoSangre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtAlergias;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final PacienteValidator pacienteValidator =
            new PacienteValidator();
    private ObservableList<Paciente> listaPacientes;
    private Paciente pacienteSeleccionado = null;

    @FXML
    public void initialize() {
        configurarComboBoxes();
        configurarTabla();
        cargarDatos();
    }

    private void configurarComboBoxes() {
        // Valores coherentes con los datos existentes en la tabla pacientes (sexo)
        cbSexo.getItems().addAll("Masculino", "Femenino", "Otro");
        cbTipoSangre.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    }

    private void configurarTabla() {
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colFechaNac.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaNacimiento();
            return new SimpleStringProperty(fecha != null ? fecha.toString() : "");
        });
        colSexo.setCellValueFactory(new PropertyValueFactory<>("sexo"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colSangre.setCellValueFactory(new PropertyValueFactory<>("tipoSangre"));
        colAlergias.setCellValueFactory(cellData -> {
            List<String> alergias = cellData.getValue().getAlergias();
            return new SimpleStringProperty(alergias != null ? String.join(", ", alergias) : "");
        });

        colAcciones.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Paciente, Void> call(TableColumn<Paciente, Void> param) {
                return new TableCell<>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox hbox = new HBox(6, btnEditar, btnEliminar);

                    {
                        btnEditar.getStyleClass().add("btn-outline");
                        btnEliminar.getStyleClass().add("btn-danger");
                        btnEditar.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                        btnEliminar.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");

                        btnEditar.setOnAction(e -> {
                            Paciente paciente = getTableView().getItems().get(getIndex());
                            editarPaciente(paciente);
                        });
                        btnEliminar.setOnAction(e -> {
                            Paciente paciente = getTableView().getItems().get(getIndex());
                            eliminarPaciente(paciente);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : hbox);
                    }
                };
            }
        });
    }

    private void cargarDatos() {
        try {
            listaPacientes = FXCollections.observableArrayList(pacienteDAO.listarTodos());
            tablePacientes.setItems(listaPacientes);
            lblTotal.setText("Total: " + listaPacientes.size() + " pacientes");
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.mostrarError("Error de conexion", "No se pudo conectar a PostgreSQL. Verifique que el servicio esté corriendo.");
        }
    }

    @FXML
    private void handleNuevo() {
        pacienteSeleccionado = null;
        formTitle.setText("Nuevo Paciente");
        limpiarFormulario();
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
    }

    private void editarPaciente(Paciente paciente) {
        pacienteSeleccionado = paciente;
        formTitle.setText("Editar Paciente");
        cargarFormulario(paciente);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
    }

    private void eliminarPaciente(Paciente paciente) {
        boolean confirmar = AlertHelper.confirmar("Eliminar Paciente",
            "Desea eliminar a " + paciente.getNombreCompleto() + "?");
        if (confirmar) {
            pacienteDAO.eliminar(paciente.getId());
            AlertHelper.mostrarExito("Eliminado", "Paciente eliminado exitosamente");
            cargarDatos();
        }
    }

    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) return;

        try {
            Paciente paciente = pacienteSeleccionado != null ? pacienteSeleccionado : new Paciente();
            paciente.setCedula(txtCedula.getText().trim());
            paciente.setNombre(txtNombre.getText().trim());
            paciente.setApellido(txtApellido.getText().trim());
            paciente.setFechaNacimiento(dpFechaNacimiento.getValue());
            paciente.setSexo(cbSexo.getValue());
            paciente.setTipoSangre(cbTipoSangre.getValue());
            paciente.setTelefono(txtTelefono.getText().trim());
            paciente.setEmail(txtEmail.getText().trim());
            paciente.setDireccion(txtDireccion.getText().trim());

            String alergiasStr = txtAlergias.getText().trim();
            if (!alergiasStr.isEmpty()) {
                paciente.setAlergias(Arrays.asList(alergiasStr.split(",")));
            } else {
                paciente.setAlergias(new ArrayList<>());
            }

            if (pacienteSeleccionado != null) {
                pacienteDAO.actualizar(paciente);
                AlertHelper.mostrarExito("Actualizado", "Paciente actualizado exitosamente");
            } else {
                pacienteDAO.insertar(paciente);
                AlertHelper.mostrarExito("Registrado", "Paciente registrado exitosamente");
            }

            cerrarFormulario();
            cargarDatos();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.mostrarError("Error", "No se pudo guardar el paciente: " + e.getMessage());
        }
    }

    @FXML
    private void handleCerrarForm() {
        cerrarFormulario();
    }

    private void cerrarFormulario() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        tableCard.setVisible(true);
        tableCard.setManaged(true);
        pacienteSeleccionado = null;
    }

    @FXML
    private void handleBuscar() {
        try {
            String texto = txtBuscar.getText().trim();
            if (texto.isEmpty()) {
                cargarDatos();
                return;
            }
            listaPacientes = FXCollections.observableArrayList(pacienteDAO.buscar(texto));
            tablePacientes.setItems(listaPacientes);
            lblTotal.setText("Resultados: " + listaPacientes.size());
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.mostrarError("Error", "No se pudo buscar: " + e.getMessage());
        }
    }

    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cargarDatos();
    }

    @FXML
    private void handleExportarExcel() {
        if (tablePacientes.getItems().isEmpty()) {
            AlertHelper.mostrarAdvertencia("Sin registros", "No hay pacientes para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar pacientes a Excel");
        fileChooser.setInitialFileName("pacientes.xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"));
        File archivo = fileChooser.showSaveDialog(tablePacientes.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".xlsx");
        }

        List<List<String>> filas = new ArrayList<>();
        for (Paciente paciente : tablePacientes.getItems()) {
            String alergias = paciente.getAlergias() != null
                    ? String.join(", ", paciente.getAlergias()) : "";
            filas.add(Arrays.asList(
                    paciente.getCedula(), paciente.getNombreCompleto(),
                    paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento().toString() : "",
                    paciente.getSexo(), paciente.getTipoSangre(), paciente.getTelefono(),
                    paciente.getEmail(), paciente.getDireccion(), alergias,
                    paciente.getFechaRegistro() != null ? paciente.getFechaRegistro().toString() : ""));
        }

        try {
            ExcelExporter.exportar("Pacientes",
                    List.of("Cedula", "Nombre completo", "Fecha de nacimiento", "Sexo", "Tipo de sangre",
                            "Telefono", "Email", "Direccion", "Alergias", "Fecha de registro"),
                    filas, archivo.toPath());
            AlertHelper.mostrarExito("Exportacion completada",
                    "Se exportaron " + filas.size() + " pacientes a:\n" + archivo.getAbsolutePath());
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "No se pudieron exportar los pacientes a Excel", e);
            AlertHelper.mostrarError("Error de exportacion", "No se pudo crear el archivo Excel: " + e.getMessage());
        }
    }

    private boolean validarFormulario() {
        String camposObligatorios = pacienteValidator.validarCamposObligatorios(
                cbSexo.getValue(),
                txtTelefono.getText(),
                txtDireccion.getText()
        );

        if (camposObligatorios != null) {
            AlertHelper.mostrarAdvertencia(
                    "Validacion de paciente",
                    camposObligatorios
            );
            return false;
        }

        String mensaje = pacienteValidator.validar(
                txtCedula.getText(),
                txtNombre.getText(),
                txtApellido.getText(),
                txtEmail.getText(),
                txtTelefono.getText(),
                dpFechaNacimiento.getValue()
        );

        if (mensaje != null) {
            AlertHelper.mostrarAdvertencia(
                    "Validacion de paciente",
                    mensaje
            );
            return false;
        }

        return true;
    }

    private void limpiarFormulario() {
        txtCedula.clear();
        txtNombre.clear();
        txtApellido.clear();
        dpFechaNacimiento.setValue(null);
        cbSexo.setValue(null);
        cbTipoSangre.setValue(null);
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        txtAlergias.clear();
    }

    private void cargarFormulario(Paciente p) {
        txtCedula.setText(p.getCedula());
        txtNombre.setText(p.getNombre());
        txtApellido.setText(p.getApellido());
        dpFechaNacimiento.setValue(p.getFechaNacimiento());
        cbSexo.setValue(p.getSexo());
        cbTipoSangre.setValue(p.getTipoSangre());
        txtTelefono.setText(p.getTelefono());
        txtEmail.setText(p.getEmail());
        txtDireccion.setText(p.getDireccion());
        if (p.getAlergias() != null) {
            txtAlergias.setText(String.join(", ", p.getAlergias()));
        }
    }
}
