package model;

import utils.MazeGenerator;
import java.io.Serializable;

public class GameBoard implements Serializable {
    private static final long serialVersionUID = 1L;

    private Cell[][] board;
    private final int rows;
    private final int cols;
    private int[] pacmanStartPosition;
    private int[] ghostSpawnPosition;
    private int totalDots;
    private int collectedDots;

    public GameBoard(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.board = new Cell[rows][cols];
        this.collectedDots = 0;
        generateBoard();
    }

    private void generateBoard() {
        MazeGenerator generator = new MazeGenerator(rows, cols);
        this.board = generator.generateMaze();

        findStartPositions();
        countDots();
    }

    private void findStartPositions() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (board[row][col].getType() == Cell.CellType.PACMAN) {
                    pacmanStartPosition = new int[]{row, col};
                    board[row][col].setType(Cell.CellType.EMPTY);
                }
            }
        }

        if (pacmanStartPosition == null) {
            pacmanStartPosition = new int[]{rows / 2, cols / 2};
        }

        ghostSpawnPosition = new int[]{1, 1};
    }

    private void countDots() {
        totalDots = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell.CellType type = board[row][col].getType();
                if (type == Cell.CellType.DOT || type == Cell.CellType.POWER_PELLET) {
                    totalDots++;
                }
            }
        }
    }

    public Cell getCell(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return null;
    }

    public synchronized void setCell(int row, int col, Cell cell) {
        if (isValidPosition(row, col)) {
            board[row][col] = cell;
        }
    }

    public Cell.CellType getCellType(int row, int col) {
        Cell cell = getCell(row, col);
        return cell != null ? cell.getType() : Cell.CellType.WALL;
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public boolean isPassable(int row, int col) {
        Cell cell = getCell(row, col);
        return cell != null && cell.isPassable();
    }

    public synchronized void clearCell(int row, int col) {
        Cell cell = getCell(row, col);
        if (cell != null && cell.hasCollectible()) {
            if (cell.getType() == Cell.CellType.DOT ||
                    cell.getType() == Cell.CellType.POWER_PELLET) {
                collectedDots++;
            }
            cell.clear();
        }
    }

    public int[] getPacmanStartPosition() {
        return pacmanStartPosition.clone();
    }

    public int[] getGhostSpawnPosition() {
        return ghostSpawnPosition.clone();
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int getTotalDots() { return totalDots; }
    public int getCollectedDots() { return collectedDots; }
    public int getAllDotsCount() { return totalDots - collectedDots; }

    public Cell[][] getBoardCopy() {
        Cell[][] copy = new Cell[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                copy[row][col] = new Cell(board[row][col]);
            }
        }
        return copy;
    }
}