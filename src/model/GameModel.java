package model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameModel {

    private GameBoard gameBoard;
    private Player pacman;
    private List<Ghost> ghosts;
    private List<PowerUp> activePowerUps;
    private GameState gameState;

    private int score;
    private int lives;
    private long gameStartTime;
    private long gameTime;
    private long lastDotRespawn;
    private long lastPowerUpSpawn;

    private List<GameModelListener> listeners;

    private final Object gameLock = new Object();

    private static final long DOT_RESPAWN_INTERVAL = 5000; // 5 sekund
    private static final long POWERUP_SPAWN_INTERVAL = 3000; // 3 sekundy
    private static final double DOT_RESPAWN_CHANCE = 0.3; // 30% szans na respawn kropki
    private static final double POWERUP_SPAWN_CHANCE = 0.2; // 20% szans na spawn power-up

    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, VICTORY
    }

    public GameModel() {
        this.listeners = new CopyOnWriteArrayList<>();
        this.ghosts = new ArrayList<>();
        this.activePowerUps = new ArrayList<>();
        this.gameState = GameState.MENU;
        this.lives = 3;
        this.score = 0;
        this.lastDotRespawn = 0;
        this.lastPowerUpSpawn = 0;
    }

    public synchronized void initializeGame(int rows, int cols) {
        synchronized (gameLock) {
            this.gameBoard = new GameBoard(rows, cols);

            int[] pacmanPos = gameBoard.getPacmanStartPosition();
            this.pacman = new Player(pacmanPos[0], pacmanPos[1]);

            initializeGhosts();

            this.score = 0;
            this.lives = 3;
            this.gameStartTime = System.currentTimeMillis();
            this.lastDotRespawn = System.currentTimeMillis();
            this.lastPowerUpSpawn = System.currentTimeMillis();
            this.gameState = GameState.PLAYING;

            updateBoardWithEntities();
            notifyModelChanged();
        }
    }

    private void initializeGhosts() {
        ghosts.clear();

        List<int[]> ghostPositions = findGoodGhostPositions();

        for (int i = 0; i < Math.min(4, ghostPositions.size()); i++) {
            int[] pos = ghostPositions.get(i);
            Ghost ghost = new Ghost(i, pos[0], pos[1]);
            ghost.setColor(getGhostColor(i));
            ghosts.add(ghost);
        }
    }

    private List<int[]> findGoodGhostPositions() {
        List<int[]> positions = new ArrayList<>();
        int rows = gameBoard.getRows();
        int cols = gameBoard.getCols();

        int[][] areas = {
                {1, 1},
                {1, cols - 2},
                {rows - 2, 1},
                {rows - 2, cols - 2},
                {rows / 2, 1},
                {rows / 2, cols - 2},
                {1, cols / 2},
                {rows - 2, cols / 2}
        };

        for (int[] area : areas) {
            int[] validPos = findNearestEmptyPosition(area[0], area[1]);
            if (validPos != null) {
                positions.add(validPos);
            }
        }

        return positions;
    }

    private int[] findNearestEmptyPosition(int startRow, int startCol) {
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[gameBoard.getRows()][gameBoard.getCols()];

        queue.offer(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];

            if (gameBoard.isPassable(row, col)) {
                return new int[]{row, col};
            }

            int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                if (gameBoard.isValidPosition(newRow, newCol) && !visited[newRow][newCol]) {
                    visited[newRow][newCol] = true;
                    queue.offer(new int[]{newRow, newCol});
                }
            }
        }

        return null;
    }

    private Ghost.GhostColor getGhostColor(int ghostId) {
        switch (ghostId) {
            case 0: return Ghost.GhostColor.RED;
            case 1: return Ghost.GhostColor.PINK;
            case 2: return Ghost.GhostColor.CYAN;
            case 3: return Ghost.GhostColor.ORANGE;
            default: return Ghost.GhostColor.RED;
        }
    }

    public void updateGame() {
        synchronized (gameLock) {
            if (gameState != GameState.PLAYING) return;

            gameTime = System.currentTimeMillis() - gameStartTime;

            updatePacmanPosition();
            updateGhosts();
            checkCollisions();
            respawnDots();
            spawnPowerUps();
            checkGameEndConditions();
            updatePowerUps();
            updateBoardWithEntities();

            notifyModelChanged();
        }
    }

    private void respawnDots() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDotRespawn < DOT_RESPAWN_INTERVAL) {
            return;
        }

        Random random = new Random();
        List<int[]> emptyCells = findEmptyCells();

        for (int[] pos : emptyCells) {
            if (random.nextDouble() < DOT_RESPAWN_CHANCE) {
                Cell cell = gameBoard.getCell(pos[0], pos[1]);
                if (cell != null && cell.getType() == Cell.CellType.EMPTY) {
                    if (random.nextDouble() < 0.9) {
                        cell.setType(Cell.CellType.DOT);
                    } else {
                        cell.setType(Cell.CellType.POWER_PELLET);
                    }
                }
            }
        }

        lastDotRespawn = currentTime;
    }

    private void spawnPowerUps() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPowerUpSpawn < POWERUP_SPAWN_INTERVAL) {
            return;
        }

        Random random = new Random();
        if (random.nextDouble() < POWERUP_SPAWN_CHANCE) {
            List<int[]> emptyCells = findEmptyCells();
            if (!emptyCells.isEmpty()) {
                int[] pos = emptyCells.get(random.nextInt(emptyCells.size()));
                Cell cell = gameBoard.getCell(pos[0], pos[1]);

                if (cell != null && cell.getType() == Cell.CellType.EMPTY) {
                    PowerUp.PowerUpType[] types = PowerUp.PowerUpType.values();
                    PowerUp.PowerUpType randomType = types[random.nextInt(types.length)];

                    PowerUp powerUp = new PowerUp(randomType);
                    cell.setPowerUp(powerUp);
                    cell.setType(Cell.CellType.POWERUP);
                }
            }
        }

        lastPowerUpSpawn = currentTime;
    }

    private List<int[]> findEmptyCells() {
        List<int[]> emptyCells = new ArrayList<>();

        for (int row = 1; row < gameBoard.getRows() - 1; row++) {
            for (int col = 1; col < gameBoard.getCols() - 1; col++) {
                Cell cell = gameBoard.getCell(row, col);
                if (cell != null && cell.getType() == Cell.CellType.EMPTY) {
                    // Sprawdź czy nie jest za blisko Pacmana lub duchów
                    boolean tooClose = false;

                    int pacmanDistance = Math.abs(pacman.getRow() - row) + Math.abs(pacman.getCol() - col);
                    if (pacmanDistance < 3) tooClose = true;

                    for (Ghost ghost : ghosts) {
                        int ghostDistance = Math.abs(ghost.getRow() - row) + Math.abs(ghost.getCol() - col);
                        if (ghostDistance < 2) {
                            tooClose = true;
                            break;
                        }
                    }

                    if (!tooClose) {
                        emptyCells.add(new int[]{row, col});
                    }
                }
            }
        }

        return emptyCells;
    }

    private void updateBoardWithEntities() {
        // Wyczyść poprzednie pozycje entity (ale zostaw power-upy, kropki itp.)
        for (int row = 0; row < gameBoard.getRows(); row++) {
            for (int col = 0; col < gameBoard.getCols(); col++) {
                Cell cell = gameBoard.getCell(row, col);
                if (cell != null) {
                    if (cell.getType() == Cell.CellType.PACMAN) {
                        cell.setType(Cell.CellType.EMPTY);
                    } else if (cell.getType() == Cell.CellType.GHOST) {
                        cell.setType(Cell.CellType.EMPTY);
                        cell.setGhostId(-1);
                    }
                }
            }
        }

        // Umieść Pacmana (ale zachowaj to co było w komórce)
        Cell pacmanCell = gameBoard.getCell(pacman.getRow(), pacman.getCol());
        if (pacmanCell != null) {
            pacmanCell.setType(Cell.CellType.PACMAN);
        }

        // Umieść duchy
        for (Ghost ghost : ghosts) {
            Cell ghostCell = gameBoard.getCell(ghost.getRow(), ghost.getCol());
            if (ghostCell != null) {
                ghostCell.setType(Cell.CellType.GHOST);
                ghostCell.setGhostId(ghost.getId());
            }
        }
    }

    public synchronized void movePacman(Player.Direction direction) {
        synchronized (gameLock) {
            if (gameState != GameState.PLAYING) return;

            pacman.setNextDirection(direction);
        }
    }

    private void updatePacmanPosition() {
        Player.Direction nextDir = pacman.getNextDirection();
        if (nextDir != null && canMove(pacman.getRow(), pacman.getCol(), nextDir)) {
            pacman.setCurrentDirection(nextDir);
            pacman.setNextDirection(null);
        }

        Player.Direction currentDir = pacman.getCurrentDirection();
        if (currentDir != null && canMove(pacman.getRow(), pacman.getCol(), currentDir)) {
            int[] newPos = getNewPosition(pacman.getRow(), pacman.getCol(), currentDir);
            pacman.setPosition(newPos[0], newPos[1]);

            collectDot(newPos[0], newPos[1]);
        }
    }

    private boolean canMove(int row, int col, Player.Direction direction) {
        int[] newPos = getNewPosition(row, col, direction);

        if (pacman != null && pacman.canPassThroughWalls()) {
            return gameBoard.isValidPosition(newPos[0], newPos[1]);
        }

        return gameBoard.isPassable(newPos[0], newPos[1]);
    }

    private int[] getNewPosition(int row, int col, Player.Direction direction) {
        switch (direction) {
            case UP:    return new int[]{row - 1, col};
            case DOWN:  return new int[]{row + 1, col};
            case LEFT:  return new int[]{row, col - 1};
            case RIGHT: return new int[]{row, col + 1};
            default:    return new int[]{row, col};
        }
    }

    private void collectDot(int row, int col) {
        Cell cell = gameBoard.getCell(row, col);
        if (cell == null) return;

        Cell.CellType cellType = cell.getType();

        int points = 0;
        if (cellType == Cell.CellType.DOT) {
            points = 10;
            cell.clear();
        } else if (cellType == Cell.CellType.POWER_PELLET) {
            points = 50;
            cell.clear();
            activatePowerMode();
        } else if (cellType == Cell.CellType.POWERUP) {
            points = 100;
            PowerUp powerUp = cell.getPowerUp();
            if (powerUp != null) {
                activatePowerUp(powerUp);
            }
            cell.clear();
        }

        if (pacman.hasDoublePoints()) {
            points *= 2;
        }

        score += points;
    }

    private void activatePowerMode() {
        for (Ghost ghost : ghosts) {
            ghost.setFrightened(true);
        }

        PowerUp powerMode = new PowerUp(PowerUp.PowerUpType.POWER_MODE);
        powerMode.activate();
        activePowerUps.add(powerMode);
    }

    private void activatePowerUp(PowerUp powerUp) {
        powerUp.activate();
        activePowerUps.add(powerUp);

        switch (powerUp.getType()) {
            case SPEED_BOOST:
                pacman.setSpeedMultiplier(1.5f);
                break;
            case GHOST_FREEZE:
                for (Ghost ghost : ghosts) {
                    ghost.setFrozen(true);
                }
                break;
            case DOUBLE_POINTS:
                pacman.setHasDoublePoints(true);
                break;
            case EXTRA_LIFE:
                lives++;
                break;
            case WALL_PASS:
                pacman.setCanPassThroughWalls(true);
                break;
        }
    }

    private void updateGhosts() {
        for (Ghost ghost : ghosts) {
            if (ghost.isFrozen()) continue;

            Player.Direction newDirection = ghost.calculateNextMove(gameBoard, pacman);

            if (canMoveGhost(ghost.getRow(), ghost.getCol(), newDirection)) {
                int[] newPos = getNewPosition(ghost.getRow(), ghost.getCol(), newDirection);
                ghost.setPosition(newPos[0], newPos[1]);
            }
        }
    }

    private boolean canMoveGhost(int row, int col, Player.Direction direction) {
        int[] newPos = getNewPosition(row, col, direction);
        return gameBoard.isPassable(newPos[0], newPos[1]);
    }

    private void checkCollisions() {
        for (Ghost ghost : ghosts) {
            if (pacman.getRow() == ghost.getRow() &&
                    pacman.getCol() == ghost.getCol()) {

                if (ghost.isFrightened()) {
                    score += 200;
                    resetGhostPosition(ghost);
                } else {
                    lives--;
                    if (lives <= 0) {
                        gameState = GameState.GAME_OVER;
                    } else {
                        resetPositions();
                    }
                }
            }
        }
    }

    private void resetGhostPosition(Ghost ghost) {
        List<int[]> positions = findGoodGhostPositions();
        if (!positions.isEmpty()) {
            int[] newPos = positions.get(ghost.getId() % positions.size());
            ghost.setPosition(newPos[0], newPos[1]);
        }
        ghost.reset();
    }

    private void checkGameEndConditions() {
        // Gra kończy się gdy gracz traci wszystkie życia, nie gdy zbierze wszystkie kropki
        // (bo kropki się odnawiają)
    }

    private void updatePowerUps() {
        Iterator<PowerUp> iterator = activePowerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            if (powerUp.isExpired()) {
                deactivatePowerUp(powerUp);
                iterator.remove();
            }
        }
    }

    private void deactivatePowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case POWER_MODE:
                for (Ghost ghost : ghosts) {
                    ghost.setFrightened(false);
                }
                break;
            case SPEED_BOOST:
                pacman.setSpeedMultiplier(1.0f);
                break;
            case GHOST_FREEZE:
                for (Ghost ghost : ghosts) {
                    ghost.setFrozen(false);
                }
                break;
            case DOUBLE_POINTS:
                pacman.setHasDoublePoints(false);
                break;
            case WALL_PASS:
                pacman.setCanPassThroughWalls(false);
                break;
        }
    }

    private void resetPositions() {
        int[] pacmanStart = gameBoard.getPacmanStartPosition();
        pacman.setPosition(pacmanStart[0], pacmanStart[1]);

        List<int[]> ghostPositions = findGoodGhostPositions();
        for (int i = 0; i < ghosts.size() && i < ghostPositions.size(); i++) {
            Ghost ghost = ghosts.get(i);
            int[] pos = ghostPositions.get(i);
            ghost.setPosition(pos[0], pos[1]);
        }
    }

    public synchronized void endGame() {
        synchronized (gameLock) {
            gameState = GameState.GAME_OVER;
            notifyModelChanged();
        }
    }

    public interface GameModelListener {
        void onModelChanged(GameModel model);
    }

    public void addListener(GameModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameModelListener listener) {
        listeners.remove(listener);
    }

    private void notifyModelChanged() {
        for (GameModelListener listener : listeners) {
            listener.onModelChanged(this);
        }
    }

    public synchronized GameBoard getGameBoard() { return gameBoard; }
    public synchronized Player getPacman() { return pacman; }
    public synchronized List<Ghost> getGhosts() { return new ArrayList<>(ghosts); }
    public synchronized int getScore() { return score; }
    public synchronized int getLives() { return lives; }
    public synchronized long getGameTime() { return gameTime; }
    public synchronized GameState getGameState() { return gameState; }
    public synchronized List<PowerUp> getActivePowerUps() { return new ArrayList<>(activePowerUps); }
}