package com.gym.app;

import com.gym.app.util.DatabaseHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {

    // The primary Scene object for the application
    private static Scene scene;

    /**
     * The main entry point for the JavaFX application.
     * This method is called after the system is ready for the application to begin
     * running.
     *
     * @param stage The primary stage for this application, onto which the
     *              application scene can be set.
     * @throws IOException If loading the FXML file fails.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize the SQLite database connection and create tables if they don't
        // exist
        DatabaseHelper.initializeDatabase();

        // Create the scene by loading the 'login' FXML view.
        // The default window size is set to 1000 pixels width and 700 pixels height.
        scene = new Scene(loadFXML("login"), 1000, 700);

        // Optional: Load global CSS styles if needed (currently commented out)
        // scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Set the scene to the primary stage (window)
        stage.setScene(scene);

        // Set the title of the application window
        stage.setTitle("Gym Management System");

        // Optional: Set transparent stage style (currently commented out)
        // stage.initStyle(StageStyle.TRANSPARENT);

        // Display the stage to the user
        stage.show();
    }

    /**
     * Switch the root view of the current scene.
     * Use this method to navigate between different screens (e.g., Login ->
     * Dashboard).
     *
     * @param fxml The name of the FXML file (without extension) to load.
     * @throws IOException If the FXML file cannot be loaded.
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Load an FXML file and return its Parent node.
     *
     * @param fxml The name of the FXML file (without extension).
     * @return The Parent node of the loaded FXML hierarchy.
     * @throws IOException If the file is not found or invalid.
     */
    private static Parent loadFXML(String fxml) throws IOException {
        // Create an FXMLLoader instance pointing to the requested resource
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/" + fxml + ".fxml"));
        // Load the FXML and return the root parent
        return fxmlLoader.load();
    }

    /**
     * The main method is the entry point for the Java application.
     * It launches the JavaFX runtime.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch();
    }
}
