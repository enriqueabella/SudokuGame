package com.project.sudokugame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Modelo principal del juego Sudoku.
 * <p>
 * Esta clase gestiona la generación del tablero completo (solución),
 * la creación del puzzle con pistas aleatorias y las funciones de validación
 * y ayuda del juego.
 * </p>
 */
public class SudokuModel {
    /** Tamaño del tablero (6x6). */
    public static final int SIZE = 6;

    /** Número de filas por bloque (2). */
    public static final int BLOCK_ROWS = 2;

    /** Número de columnas por bloque (3). */
    public static final int BLOCK_COLS = 3;

    /** Tablero con la solución completa del Sudoku. */
    private final int[][] solution = new int[SIZE][SIZE];

    /** Tablero visible del jugador (0 representa una celda vacía). */
    private final int[][] puzzle = new int[SIZE][SIZE];

    /** Matriz que indica qué celdas son pistas dadas (true = no se pueden modificar). */
    private final boolean[][] given = new boolean[SIZE][SIZE];

    /** Generador de números aleatorios. */
    private final Random rnd = new Random();

    /**
     * Constructor: crea un nuevo modelo y genera un puzzle inicial.
     */
    public SudokuModel() {
        generateNewPuzzle();
    }

    /**
     * Obtiene el valor de la celda en la solución completa.
     *
     * @param r Fila de la celda.
     * @param c Columna de la celda.
     * @return Valor de la celda en la solución.
     */
    public int getSolution(int r, int c) { return solution[r][c]; }

    /**
     * Obtiene el valor actual de una celda en el puzzle visible.
     *
     * @param r Fila de la celda.
     * @param c Columna de la celda.
     * @return Valor actual de la celda (0 si está vacía).
     */
    public int getCell(int r, int c) { return puzzle[r][c]; }

    /**
     * Verifica si una celda fue dada como pista inicial.
     *
     * @param r Fila de la celda.
     * @param c Columna de la celda.
     * @return true si la celda es una pista inicial, false si es editable.
     */
    public boolean isGiven(int r, int c) { return given[r][c]; }

    /**
     * Establece un nuevo valor en una celda del puzzle visible.
     *
     * @param r Fila de la celda.
     * @param c Columna de la celda.
     * @param v Valor a asignar (1–6, o 0 si se borra).
     */
    public void setCell(int r, int c, int v) { puzzle[r][c] = v; }

    /**
     * Genera una solución completa del Sudoku de manera aleatoria usando recursividad (backtracking).
     *
     * @param r Fila actual del tablero.
     * @param c Columna actual del tablero.
     * @return true si la solución se completó correctamente, false en caso contrario.
     */
    private boolean fillSolution(int r, int c) {
        if (r == SIZE) return true;

        // Calcula la siguiente posición a llenar
        int nr = (c == SIZE - 1) ? r + 1 : r;
        int nc = (c == SIZE - 1) ? 0 : c + 1;

        // Crea una lista aleatoria de números del 1 al 6
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) nums.add(i);
        Collections.shuffle(nums, rnd);

        // Intenta colocar cada número y continúa recursivamente
        for (int n : nums) {
            if (isSafe(solution, r, c, n)) {
                solution[r][c] = n;
                if (fillSolution(nr, nc)) return true;
                solution[r][c] = 0;
            }
        }
        return false;
    }

    /**
     * Verifica si un valor puede colocarse en una posición del tablero sin violar
     * las reglas del Sudoku.
     *
     * @param board Tablero en el que se realiza la verificación.
     * @param r Fila donde se desea colocar el valor.
     * @param c Columna donde se desea colocar el valor.
     * @param val Valor a verificar.
     * @return true si el valor puede colocarse, false en caso contrario.
     */
    private boolean isSafe(int[][] board, int r, int c, int val) {
        // Verifica fila y columna
        for (int i = 0; i < SIZE; i++)
            if (board[r][i] == val || board[i][c] == val) return false;

        // Calcula el bloque 2x3 correspondiente
        int br = (r / BLOCK_ROWS) * BLOCK_ROWS;
        int bc = (c / BLOCK_COLS) * BLOCK_COLS;

        // Verifica el bloque
        for (int i = 0; i < BLOCK_ROWS; i++)
            for (int j = 0; j < BLOCK_COLS; j++)
                if (board[br + i][bc + j] == val) return false;

        return true;
    }

    /**
     * Genera un nuevo puzzle copiando la solución y dejando algunas pistas
     * aleatorias en el tablero.
     */
    public void generateNewPuzzle() {
        // Limpia el tablero de solución
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                solution[i][j] = 0;

        // Genera la solución completa
        fillSolution(0, 0);

        // Copia la solución al puzzle
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                puzzle[i][j] = solution[i][j];

        // Define cuántas pistas mantener (entre 10 y 18)
        int keep = 10 + rnd.nextInt(9);

        // Vacía el puzzle y selecciona posiciones aleatorias
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                puzzle[i][j] = 0;

        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < SIZE * SIZE; i++) positions.add(i);
        Collections.shuffle(positions, rnd);

        for (int k = 0; k < keep; k++) {
            int pos = positions.get(k);
            int r = pos / SIZE, c = pos % SIZE;
            puzzle[r][c] = solution[r][c];
        }

        // Marca las celdas dadas como pistas
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                given[i][j] = puzzle[i][j] != 0;
    }

    /**
     * Proporciona una pista al jugador.
     * <p>
     * Busca una celda vacía y revela su valor correcto de la solución.
     * </p>
     *
     * @return Un arreglo con la posición y el valor de la pista [fila, columna, valor],
     * o null si no hay celdas vacías.
     */
    public int[] giveHint() {
        List<int[]> empties = new ArrayList<>();
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (puzzle[i][j] == 0)
                    empties.add(new int[]{i, j});

        if (empties.isEmpty()) return null;

        int[] pos = empties.get(rnd.nextInt(empties.size()));
        int r = pos[0], c = pos[1];

        puzzle[r][c] = solution[r][c];
        given[r][c] = true;

        return new int[]{r, c, solution[r][c]};
    }
}
