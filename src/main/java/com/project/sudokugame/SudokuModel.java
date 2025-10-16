package com.project.sudokugame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Modelo: guarda solución completa y tablero (0 = vacío).
 * Genera una solución por backtracking y crea un puzzle con pistas aleatorias.
 */
public class SudokuModel {
    public static final int SIZE = 6;
    public static final int BLOCK_ROWS = 2;
    public static final int BLOCK_COLS = 3;

    private final int[][] solution = new int[SIZE][SIZE];
    private final int[][] puzzle = new int[SIZE][SIZE]; // 0 = empty
    private final boolean[][] given = new boolean[SIZE][SIZE];

    private final Random rnd = new Random();

    public SudokuModel() {
        generateNewPuzzle();
    }

    public int getSolution(int r, int c) { return solution[r][c]; }
    public int getCell(int r, int c) { return puzzle[r][c]; }
    public boolean isGiven(int r, int c) { return given[r][c]; }
    public void setCell(int r, int c, int v) { puzzle[r][c] = v; }

    /**
     * Genera solución completa aleatoria con backtracking.
     */
    private boolean fillSolution(int r, int c) {
        if (r == SIZE) return true;
        int nr = (c == SIZE-1) ? r+1 : r;
        int nc = (c == SIZE-1) ? 0 : c+1;

        List<Integer> nums = new ArrayList<>();
        for (int i=1;i<=SIZE;i++) nums.add(i);
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
/*Si la celda está en (r=4, c=1):

br = (4 / 2) * 2 = 2 * 2 = 4

bc = (1 / 3) * 3 = 0 * 3 = 0
➡ El bloque empieza en (4,0) y cubre filas 4–5 y columnas 0–2.*/
    private boolean isSafe(int[][] board, int r, int c, int val) {
        for (int i=0;i<SIZE;i++) if (board[r][i]==val || board[i][c]==val) return false;
        int br = (r / BLOCK_ROWS) * BLOCK_ROWS;
        int bc = (c / BLOCK_COLS) * BLOCK_COLS;
        for (int i=0;i<BLOCK_ROWS;i++) for (int j=0;j<BLOCK_COLS;j++)
            if (board[br+i][bc+j]==val) return false;
        return true;
    }

    /**
     * Crea un puzzle copiando la solución y dejando aleatoriamente algunas pistas.
     * No garantiza unicidad del puzzle (no es obligatorio para la actividad).
     */
    public void generateNewPuzzle() {
        // generar solución
        for (int i=0;i<SIZE;i++) for (int j=0;j<SIZE;j++) solution[i][j]=0;
        fillSolution(0,0);

        // copiar solución en puzzle
        for (int i=0;i<SIZE;i++) for (int j=0;j<SIZE;j++) puzzle[i][j]=solution[i][j];

        // decide cuántas pistas dejar (entre 10 y 18 para dificultad mediana)
        int keep = 10 + rnd.nextInt(9);
        // primero vaciamos todo
        for (int i=0;i<SIZE;i++) for (int j=0;j<SIZE;j++) puzzle[i][j]=0;
        // elegir posiciones al azar para mantener
        List<Integer> positions = new ArrayList<>();
        for (int i=0;i<SIZE*SIZE;i++) positions.add(i);
        Collections.shuffle(positions, rnd);
        for (int k=0;k<keep;k++) {
            int pos = positions.get(k);
            int r = pos / SIZE, c = pos % SIZE;
            puzzle[r][c] = solution[r][c];
        }
        // marca givens
        for (int i=0;i<SIZE;i++) for (int j=0;j<SIZE;j++) given[i][j] = puzzle[i][j]!=0;
    }

    /**
     * Usa la solución para dar una ayuda: devuelve (r,c,value) o null si no hay vacíos.
     */
    public int[] giveHint() {
        List<int[]> empties = new ArrayList<>();
        for (int i=0;i<SIZE;i++) for (int j=0;j<SIZE;j++) if (puzzle[i][j]==0)
            empties.add(new int[]{i,j});
        if (empties.isEmpty()) return null;
        int[] pos = empties.get(rnd.nextInt(empties.size()));
        int r = pos[0], c = pos[1];
        puzzle[r][c] = solution[r][c];
        given[r][c] = true;
        return new int[]{r,c,solution[r][c]};
    }
}
