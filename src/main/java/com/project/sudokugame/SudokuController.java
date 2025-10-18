package com.project.sudokugame;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;
import java.util.List;

public class SudokuController {

    @FXML private GridPane gridSudoku;
    @FXML private Button btnNuevo;
    @FXML private Button btnAyuda;
    @FXML private Label lblPosicion;
    @FXML private Label lblMensajes;

    private final SudokuModel model = new SudokuModel();
    private final TextField[][] cells = new TextField[SudokuModel.SIZE][SudokuModel.SIZE];

    @FXML
    public void initialize() {
        buildGrid();
        bindButtons();
        refreshView();
    }

    private void buildGrid() {
        gridSudoku.getChildren().clear();
        for (int r = 0; r < SudokuModel.SIZE; r++) {
            for (int c = 0; c < SudokuModel.SIZE; c++) {
                TextField tf = new TextField();
                tf.setPrefSize(50, 50);
                tf.setStyle("-fx-font-size:16; -fx-alignment:center;");
                final int rr = r, cc = c;

                tf.setOnMouseEntered(e -> lblPosicion.setText("Ubicación: fila " + (rr+1) + " col " + (cc+1)));
                tf.setOnMouseExited(e -> lblPosicion.setText("Ubicación: "));

                tf.addEventFilter(KeyEvent.KEY_TYPED, ev -> {
                    String ch = ev.getCharacter();
                    if (!("123456".contains(ch) || ch.equals("\b"))) ev.consume();
                });

                tf.setOnKeyReleased(ev -> {
                    String txt = tf.getText().trim();
                    if (txt.isEmpty()) model.setCell(rr, cc, 0);
                    else {
                        txt = txt.substring(0, 1);
                        tf.setText(txt);
                        int v = Integer.parseInt(txt);
                        if (v < 1 || v > 6) {
                            tf.clear();
                            model.setCell(rr, cc, 0);
                        } else {
                            model.setCell(rr, cc, v);
                        }
                    }
                    validateAll();
                });

                gridSudoku.add(tf, c, r);
                cells[r][c] = tf;
            }
        }
    }

    private void bindButtons() {
        btnNuevo.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Iniciar nuevo juego?", ButtonType.YES, ButtonType.NO);
            a.setHeaderText(null);
            a.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    model.generateNewPuzzle();
                    refreshView();
                    lblMensajes.setText("Nuevo tablero generado.");
                }
            });
        });

        btnAyuda.setOnAction(e -> {
            int[] hint = model.giveHint();
            if (hint == null) {
                lblMensajes.setText("No hay celdas vacías para dar ayuda.");
            } else {
                int r = hint[0], c = hint[1], v = hint[2];
                cells[r][c].setText(String.valueOf(v));
                cells[r][c].setEditable(false);
                cells[r][c].getStyleClass().add("given");
                validateAll();
                lblMensajes.setText("Ayuda colocada en fila " + (r+1) + ", col " + (c+1));
            }
        });
    }

    private void refreshView() {
        for (int r = 0; r < SudokuModel.SIZE; r++) {
            for (int c = 0; c < SudokuModel.SIZE; c++) {
                TextField tf = cells[r][c];
                int v = model.getCell(r, c);
                if (v == 0) tf.setText("");
                else tf.setText(String.valueOf(v));
                if (model.isGiven(r, c)) {
                    tf.setEditable(false);
                    if (!tf.getStyleClass().contains("given")) tf.getStyleClass().add("given");
                } else {
                    tf.setEditable(true);
                    tf.getStyleClass().removeAll("given");
                }
                tf.getStyleClass().removeAll("error");
            }
        }
        validateAll();
    }

    private void validateAll() {
        // quitar errores anteriores
        for (int r=0;r<SudokuModel.SIZE;r++)
            for (int c=0;c<SudokuModel.SIZE;c++)
                cells[r][c].getStyleClass().removeAll("error");

        boolean anyError = false;

        for (int r=0;r<SudokuModel.SIZE;r++) {
            for (int c=0;c<SudokuModel.SIZE;c++) {
                int val = valueAt(r, c);
                if (val == 0) continue;

                // Fila
                for (int cc=0; cc<SudokuModel.SIZE; cc++)
                    if (cc != c && valueAt(r, cc) == val)
                        markError(r, c);

                // Columna
                for (int rr=0; rr<SudokuModel.SIZE; rr++)
                    if (rr != r && valueAt(rr, c) == val)
                        markError(r, c);

                // Bloque
                int br = (r / SudokuModel.BLOCK_ROWS) * SudokuModel.BLOCK_ROWS;
                int bc = (c / SudokuModel.BLOCK_COLS) * SudokuModel.BLOCK_COLS;
                for (int i = 0; i < SudokuModel.BLOCK_ROWS; i++)
                    for (int j = 0; j < SudokuModel.BLOCK_COLS; j++) {
                        int rr = br + i, cc = bc + j;
                        if ((rr != r || cc != c) && valueAt(rr, cc) == val)
                            markError(r, c);
                    }
            }
        }

        for (int r=0;r<SudokuModel.SIZE;r++)
            for (int c=0;c<SudokuModel.SIZE;c++)
                if (cells[r][c].getStyleClass().contains("error"))
                    anyError = true;

        if (anyError) {
            lblMensajes.setText("⚠️   ERROR/ES(resaltado/s en rojo).");
        } else {
            lblMensajes.setText("✅ Sin conflictos.");
            checkWin(); // 👈 Verifica si se completó correctamente
        }
    }

    private void checkWin() {
        // Si todas las celdas están llenas y sin error, el jugador gana
        for (int r = 0; r < SudokuModel.SIZE; r++) {
            for (int c = 0; c < SudokuModel.SIZE; c++) {
                if (valueAt(r, c) == 0) return; // hay vacío, aún no gana
            }
        }

        // Mostrar alerta de victoria
        Alert winAlert = new Alert(Alert.AlertType.INFORMATION);
        winAlert.setHeaderText("🎉 ¡Felicidades!");
        winAlert.setContentText("Completaste el Sudoku correctamente.");
        winAlert.showAndWait();

        lblMensajes.setText("🎉 ¡Ganaste el juego!");
    }

    private int valueAt(int r, int c) {
        String t = cells[r][c].getText().trim();
        if (t.isEmpty()) return 0;
        try { return Integer.parseInt(t.substring(0, 1)); }
        catch (Exception ex) { return 0; }
    }

    private void markError(int r, int c) {
        TextField tf = cells[r][c];
        if (!tf.getStyleClass().contains("error")) tf.getStyleClass().add("error");
    }
}
