package view;

import model.Cell;
import javax.swing.table.AbstractTableModel;

public class GameTableModel extends AbstractTableModel {
    private Cell[][] gameBoard;
    private final int rows;
    private final int columns;

    public GameTableModel(int rows, int columns) {
        if (rows < 10 || rows > 100 || columns < 10 || columns > 100) {
            throw new IllegalArgumentException("Rozmiar planszy musi być między 10 a 100!");
        }

        this.rows = rows;
        this.columns = columns;
        this.gameBoard = new Cell[rows][columns];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                gameBoard[row][col] = new Cell(Cell.CellType.EMPTY);
            }
        }
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public int getColumnCount() {
        return columns;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (isValidPosition(rowIndex, columnIndex)) {
            return gameBoard[rowIndex][columnIndex];
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Cell.class;
    }

    public synchronized void updateCell(int row, int col, Cell newCell) {
        if (isValidPosition(row, col)) {
            gameBoard[row][col] = newCell;
            fireTableCellUpdated(row, col);
        }
    }

    public synchronized Cell getCell(int row, int col) {
        if (isValidPosition(row, col)) {
            return gameBoard[row][col];
        }
        return null;
    }

    public synchronized void updateBoard(Cell[][] newBoard) {
        if (newBoard.length == rows && newBoard[0].length == columns) {
            this.gameBoard = newBoard;
            fireTableDataChanged();
        }
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < columns;
    }

    public synchronized Cell[][] getBoardCopy() {
        Cell[][] copy = new Cell[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                copy[row][col] = new Cell(gameBoard[row][col]);
            }
        }
        return copy;
    }

    public synchronized void clearBoard() {
        initializeBoard();
        fireTableDataChanged();
    }
}