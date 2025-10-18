package com.project.sudokugame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Main model for the Sudoku game.
 * <p>
 * This class handles generating the full solution board,
 * creating the puzzle with random clues, and provides
 * validation and hint functionality.
 * </p>
 */
public class SudokuModel {
    /** Size of the board (6x6). */
    public static final int SIZE = 6;

    /** Number of rows per block (2). */
    public static final int BLOCK_ROWS = 2;

    /** Number of columns per block (3). */
    public static final int BLOCK_COLS = 3;

    /** Board containing the full Sudoku solution. */
    private final int[][] solution = new int[SIZE][SIZE];

    /** Player-visible board (0 represents an empty cell). */
    private final int[][] puzzle = new int[SIZE][SIZE];

    /** Matrix indicating which cells are given clues (true = cannot be modified). */
    private final boolean[][] given = new boolean[SIZE][SIZE];

    /** Random number generator. */
    private final Random rnd = new Random();

    /**
     * Constructor: creates a new model and generates an initial puzzle.
     */
    public SudokuModel() {
        generateNewPuzzle();
    }

    /**
     * Gets the value of a cell in the full solution.
     *
     * @param r Row of the cell.
     * @param c Column of the cell.
     * @return Value of the cell in the solution.
     */
    public int getSolution(int r, int c) { return solution[r][c]; }

    /**
     * Gets the current value of a cell in the visible puzzle.
     *
     * @param r Row of the cell.
     * @param c Column of the cell.
     * @return Current value of the cell (0 if empty).
     */
    public int getCell(int r, int c) { return puzzle[r][c]; }

    /**
     * Checks if a cell was given as an initial clue.
     *
     * @param r Row of the cell.
     * @param c Column of the cell.
     * @return true if the cell is an initial clue, false if editable.
     */
    public boolean isGiven(int r, int c) { return given[r][c]; }

    /**
     * Sets a new value in a cell of the visible puzzle.
     *
     * @param r Row of the cell.
     * @param c Column of the cell.
     * @param v Value to assign (1–6, or 0 to clear).
     */
    public void setCell(int r, int c, int v) { puzzle[r][c] = v; }

    /**
     * Generates a full Sudoku solution recursively (backtracking).
     *
     * @param r Current row on the board.
     * @param c Current column on the board.
     * @return true if the solution was successfully completed, false otherwise.
     */
    private boolean fillSolution(int r, int c) {
        if (r == SIZE) return true;

        int nr = (c == SIZE - 1) ? r + 1 : r;
        int nc = (c == SIZE - 1) ? 0 : c + 1;

        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) nums.add(i);
        Collections.shuffle(nums, rnd);

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
     * Checks if a value can be placed in a board position without violating Sudoku rules.
     *
     * @param board Board to check.
     * @param r Row to place the value.
     * @param c Column to place the value.
     * @param val Value to check.
     * @return true if the value can be placed, false otherwise.
     */
    private boolean isSafe(int[][] board, int r, int c, int val) {
        for (int i = 0; i < SIZE; i++)
            if (board[r][i] == val || board[i][c] == val) return false;

        int br = (r / BLOCK_ROWS) * BLOCK_ROWS;
        int bc = (c / BLOCK_COLS) * BLOCK_COLS;

        for (int i = 0; i < BLOCK_ROWS; i++)
            for (int j = 0; j < BLOCK_COLS; j++)
                if (board[br + i][bc + j] == val) return false;

        return true;
    }

    /**
     * Generates a new puzzle by copying the solution and leaving a number
     * of random clues on the board.
     */
    public void generateNewPuzzle() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                solution[i][j] = 0;

        fillSolution(0, 0);

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                puzzle[i][j] = solution[i][j];

        int keep = 10 + rnd.nextInt(9);

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

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                given[i][j] = puzzle[i][j] != 0;
    }

    /**
     * Provides a hint to the player.
     * <p>
     * Finds an empty cell and reveals its correct solution value.
     * </p>
     *
     * @return An array with the position and value of the hint [row, column, value],
     * or null if no empty cells exist.
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
