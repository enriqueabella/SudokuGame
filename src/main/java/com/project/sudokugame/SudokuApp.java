package com.project.sudokugame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the Sudoku 6x6 JavaFX application.
 * <p>
 * This class loads the main FXML layout and initializes
 * the primary window (stage) for the game interface.
 * </p>
 */
public class SudokuApp extends Application {

    /**
     * Starts the JavaFX application.
     * <p>
     * Loads the FXML file, sets up the main scene,
     * and displays the application window.
     * </p>
     *
     * @param stage the primary stage provided by the JavaFX runtime
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/com/project/sudokugame/sudoku.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Sudoku 6x6");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Launches the Sudoku application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch();
    }
}
