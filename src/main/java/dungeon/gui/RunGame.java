package dungeon.gui; // Make sure package is correct

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert; // For error dialogs
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class RunGame extends Application { // <-- MUST extend Application

    @Override
    public void start(Stage primaryStage) { // <-- This is the entry point for JavaFX
        try {
            // Path to FXML file.
            // This assumes game_gui.fxml is in src/main/resources/dungeon/gui/
            URL fxmlLocation = getClass().getResource("/gui/game_gui.fxml");
            if (fxmlLocation == null) {
                System.err.println("FXML file not found. Check path: /dungeon/gui/game_gui.fxml");
                // Show an alert to the user
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Application Error");
                alert.setHeaderText("Failed to load user interface.");
                alert.setContentText("The FXML file (/dungeon/gui/game_gui.fxml) could not be found. Please check application resources.");
                alert.showAndWait();
                return; // Exit if FXML not found
            }

            Parent root = FXMLLoader.load(fxmlLocation);
            Scene scene = new Scene(root, 800, 600); // Set a default size, matches FXML pref size

            primaryStage.setTitle("MiniDungeon ICT221");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace(); // Log the full error for debugging
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Error loading user interface.");
            alert.setContentText("An I/O error occurred: " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) { // Catch any other unexpected errors during startup
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Unexpected application error.");
            alert.setContentText("An unexpected error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args); // This calls the start() method
    }
}
