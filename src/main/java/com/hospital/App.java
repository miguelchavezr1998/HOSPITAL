package com.hospital;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hospital.database.PostgreSQLConnection;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle("Sistema Hospitalario - Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(360);
            primaryStage.setMinHeight(560);
            primaryStage.show();

            primaryStage.setOnCloseRequest(e -> {
                try {
                    PostgreSQLConnection.getInstance().close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "No se pudo cerrar la conexion a PostgreSQL", ex);
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al iniciar la aplicacion", e);
        }
    }

    public static void main(String[] args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        launch(args);
    }
}
