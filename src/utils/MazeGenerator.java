package utils;

import model.Cell;
import java.util.*;

public class MazeGenerator {

    private final int rows;
    private final int cols;
    private Cell[][] maze;
    private Random random;

    private static final int[][] DIRECTIONS = {{-2, 0}, {0, 2}, {2, 0}, {0, -2}};

    public MazeGenerator(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.maze = new Cell[rows][cols];
        this.random = new Random();
    }

    public Cell[][] generateMaze() {
        initializeWithWalls();
        createMazeStructure();
        addBorders();
        addDots();
        addStartingPositions();
        return maze;
    }

    private void initializeWithWalls() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                maze[row][col] = new Cell(Cell.CellType.WALL);
            }
        }
    }

    private void createMazeStructure() {
        Stack<int[]> stack = new Stack<>();

        int startRow = 1;
        int startCol = 1;

        maze[startRow][startCol] = new Cell(Cell.CellType.EMPTY);
        maze[startRow][startCol].setVisited(true);

        stack.push(new int[]{startRow, startCol});

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int currentRow = current[0];
            int currentCol = current[1];

            List<int[]> neighbors = getUnvisitedNeighbors(currentRow, currentCol);

            if (!neighbors.isEmpty()) {
                int[] neighbor = neighbors.get(random.nextInt(neighbors.size()));
                int newRow = neighbor[0];
                int newCol = neighbor[1];

                int wallRow = currentRow + (newRow - currentRow) / 2;
                int wallCol = currentCol + (newCol - currentCol) / 2;

                maze[newRow][newCol] = new Cell(Cell.CellType.EMPTY);
                maze[newRow][newCol].setVisited(true);
                maze[wallRow][wallCol] = new Cell(Cell.CellType.EMPTY);
                maze[wallRow][wallCol].setVisited(true);

                stack.push(new int[]{newRow, newCol});
            } else {
                stack.pop();
            }
        }
    }

    private List<int[]> getUnvisitedNeighbors(int row, int col) {
        List<int[]> neighbors = new ArrayList<>();

        for (int[] direction : DIRECTIONS) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            if (isValidCell(newRow, newCol) && !maze[newRow][newCol].hasBeenVisited()) {
                neighbors.add(new int[]{newRow, newCol});
            }
        }

        return neighbors;
    }

    private boolean isValidCell(int row, int col) {
        return row > 0 && row < rows - 1 &&
                col > 0 && col < cols - 1 &&
                row % 2 == 1 && col % 2 == 1;
    }

    private void addBorders() {
        for (int col = 0; col < cols; col++) {
            maze[0][col] = new Cell(Cell.CellType.WALL);
            maze[rows - 1][col] = new Cell(Cell.CellType.WALL);
        }

        for (int row = 0; row < rows; row++) {
            maze[row][0] = new Cell(Cell.CellType.WALL);
            maze[row][cols - 1] = new Cell(Cell.CellType.WALL);
        }
    }

    private void addDots() {
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < cols - 1; col++) {
                if (maze[row][col].getType() == Cell.CellType.EMPTY) {
                    if (random.nextDouble() < 0.9) {
                        maze[row][col] = new Cell(Cell.CellType.DOT);
                    } else {
                        maze[row][col] = new Cell(Cell.CellType.POWER_PELLET);
                    }
                }
            }
        }
    }

    private void addStartingPositions() {
        int centerRow = rows / 2;
        int centerCol = cols / 2;

        int[] pacmanStart = findNearestEmptyCell(centerRow, centerCol);
        if (pacmanStart != null) {
            maze[pacmanStart[0]][pacmanStart[1]] = new Cell(Cell.CellType.PACMAN);
        }

        int[] ghostSpawn = findNearestEmptyCell(1, 1);
        if (ghostSpawn != null && !Arrays.equals(ghostSpawn, pacmanStart)) {
            maze[ghostSpawn[0]][ghostSpawn[1]] = new Cell(Cell.CellType.EMPTY);
        }
    }

    private int[] findNearestEmptyCell(int targetRow, int targetCol) {
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[rows][cols];

        queue.offer(new int[]{targetRow, targetCol});
        visited[targetRow][targetCol] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];

            if (maze[row][col].getType() == Cell.CellType.EMPTY ||
                    maze[row][col].getType() == Cell.CellType.DOT) {
                return new int[]{row, col};
            }

            int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                if (newRow >= 0 && newRow < rows &&
                        newCol >= 0 && newCol < cols &&
                        !visited[newRow][newCol]) {
                    visited[newRow][newCol] = true;
                    queue.offer(new int[]{newRow, newCol});
                }
            }
        }

        return null;
    }

    public boolean isValidMaze() {
        int emptyCount = 0;
        int dotCount = 0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell.CellType type = maze[row][col].getType();
                if (type == Cell.CellType.EMPTY || type == Cell.CellType.PACMAN) {
                    emptyCount++;
                } else if (type == Cell.CellType.DOT || type == Cell.CellType.POWER_PELLET) {
                    dotCount++;
                }
            }
        }

        double emptyRatio = (double)(emptyCount + dotCount) / (rows * cols);
        return emptyRatio >= 0.1;
    }
}