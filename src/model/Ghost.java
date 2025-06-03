package model;

import java.io.Serializable;
import java.util.Random;

public class Ghost implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum GhostColor {
        RED, PINK, CYAN, ORANGE
    }

    private final int id;
    private int row;
    private int col;
    private Player.Direction currentDirection;
    private GhostColor color;
    private boolean isFrightened;
    private boolean isFrozen;
    private long lastPowerUpCreation;
    private long lastDirectionChange;
    private transient Random random; // KLUCZOWE: transient dla Serializable

    private static final long POWERUP_CREATION_INTERVAL = 2500;
    private static final double POWERUP_CREATION_CHANCE = 0.5;
    private static final long DIRECTION_CHANGE_INTERVAL = 400;

    public Ghost(int id, int startRow, int startCol) {
        this.id = id;
        this.row = startRow;
        this.col = startCol;
        this.color = GhostColor.RED;
        this.isFrightened = false;
        this.isFrozen = false;
        this.lastPowerUpCreation = System.currentTimeMillis();
        this.lastDirectionChange = System.currentTimeMillis();

        // BEZPIECZNA INICJALIZACJA RANDOM
        initializeRandom();

        // Ustaw losowy kierunek startowy
        this.currentDirection = getRandomDirection();
    }

    // NOWA METODA: Bezpieczna inicjalizacja Random
    private void initializeRandom() {
        try {
            this.random = new Random(System.currentTimeMillis() + id * 1000);
        } catch (Exception e) {
            // Fallback w przypadku błędu
            this.random = new Random();
        }
    }

    // METODA SPRAWDZAJĄCA czy Random jest zainicjowany
    private void ensureRandomInitialized() {
        if (this.random == null) {
            initializeRandom();
        }
    }

    public int getId() { return id; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    public synchronized void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Player.Direction getCurrentDirection() { return currentDirection; }
    public void setCurrentDirection(Player.Direction direction) { this.currentDirection = direction; }

    public GhostColor getColor() { return color; }
    public void setColor(GhostColor color) { this.color = color; }

    public boolean isFrightened() { return isFrightened; }
    public void setFrightened(boolean frightened) { this.isFrightened = frightened; }

    public boolean isFrozen() { return isFrozen; }
    public void setFrozen(boolean frozen) { this.isFrozen = frozen; }

    public Player.Direction calculateNextMove(GameBoard board, Player pacman) {
        ensureRandomInitialized(); // SPRAWDŹ czy Random jest OK

        if (isFrozen) {
            return currentDirection;
        }

        long currentTime = System.currentTimeMillis();
        boolean shouldChangeDirection = (currentTime - lastDirectionChange) >= DIRECTION_CHANGE_INTERVAL;

        if (!shouldChangeDirection) {
            return currentDirection;
        }

        Player.Direction newDirection;

        try {
            if (isFrightened) {
                newDirection = getRandomDirection();
            } else {
                switch (id) {
                    case 0: // Czerwony - goni bezpośrednio
                        newDirection = calculateDirectionTowardsPacman(pacman);
                        break;
                    case 1: // Różowy - próbuje obejść
                        newDirection = random.nextDouble() < 0.8 ?
                                calculateAmbushDirection(pacman) :
                                calculateDirectionTowardsPacman(pacman);
                        break;
                    case 2: // Niebieski - chaotyczny
                        newDirection = random.nextDouble() < 0.6 ?
                                calculateDirectionTowardsPacman(pacman) :
                                getRandomDirection();
                        break;
                    case 3: // Pomarańczowy - utrzymuje dystans
                        int distance = Math.abs(pacman.getRow() - row) + Math.abs(pacman.getCol() - col);
                        if (distance < 5) {
                            newDirection = random.nextDouble() < 0.4 ?
                                    calculateFleeDirection(pacman) :
                                    calculateDirectionTowardsPacman(pacman);
                        } else {
                            newDirection = calculateDirectionTowardsPacman(pacman);
                        }
                        break;
                    default:
                        newDirection = calculateDirectionTowardsPacman(pacman);
                }
            }
        } catch (Exception e) {
            // Fallback w przypadku błędu - idź w stronę Pacmana
            newDirection = calculateDirectionTowardsPacman(pacman);
        }

        this.lastDirectionChange = currentTime;
        this.currentDirection = newDirection;
        return newDirection;
    }

    private Player.Direction calculateDirectionTowardsPacman(Player pacman) {
        ensureRandomInitialized();

        int deltaRow = pacman.getRow() - this.row;
        int deltaCol = pacman.getCol() - this.col;

        try {
            // Dodaj element losowości
            if (Math.abs(deltaRow) == Math.abs(deltaCol) && random.nextBoolean()) {
                return random.nextBoolean() ?
                        (deltaRow > 0 ? Player.Direction.DOWN : Player.Direction.UP) :
                        (deltaCol > 0 ? Player.Direction.RIGHT : Player.Direction.LEFT);
            }
        } catch (Exception e) {
            // Jeśli błąd z random - użyj prostego algorytmu
        }

        if (Math.abs(deltaRow) > Math.abs(deltaCol)) {
            return deltaRow > 0 ? Player.Direction.DOWN : Player.Direction.UP;
        } else {
            return deltaCol > 0 ? Player.Direction.RIGHT : Player.Direction.LEFT;
        }
    }

    private Player.Direction calculateFleeDirection(Player pacman) {
        int deltaRow = pacman.getRow() - this.row;
        int deltaCol = pacman.getCol() - this.col;

        if (Math.abs(deltaRow) > Math.abs(deltaCol)) {
            return deltaRow > 0 ? Player.Direction.UP : Player.Direction.DOWN;
        } else {
            return deltaCol > 0 ? Player.Direction.LEFT : Player.Direction.RIGHT;
        }
    }

    private Player.Direction calculateAmbushDirection(Player pacman) {
        ensureRandomInitialized();

        int targetRow = pacman.getRow();
        int targetCol = pacman.getCol();

        Player.Direction pacmanDir = pacman.getCurrentDirection();
        if (pacmanDir != null) {
            int prediction = 3;
            try {
                prediction = 3 + random.nextInt(3); // 3-5 kroków
            } catch (Exception e) {
                prediction = 4; // Fallback
            }

            switch (pacmanDir) {
                case UP: targetRow -= prediction; break;
                case DOWN: targetRow += prediction; break;
                case LEFT: targetCol -= prediction; break;
                case RIGHT: targetCol += prediction; break;
            }
        }

        int deltaRow = targetRow - this.row;
        int deltaCol = targetCol - this.col;

        if (Math.abs(deltaRow) > Math.abs(deltaCol)) {
            return deltaRow > 0 ? Player.Direction.DOWN : Player.Direction.UP;
        } else {
            return deltaCol > 0 ? Player.Direction.RIGHT : Player.Direction.LEFT;
        }
    }

    private Player.Direction getRandomDirection() {
        ensureRandomInitialized();

        Player.Direction[] directions = Player.Direction.values();
        try {
            return directions[random.nextInt(directions.length)];
        } catch (Exception e) {
            // Fallback - zwróć UP
            return Player.Direction.UP;
        }
    }

    public boolean tryCreatePowerUp(GameBoard board) {
        ensureRandomInitialized();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPowerUpCreation >= POWERUP_CREATION_INTERVAL) {
            try {
                if (random.nextDouble() < POWERUP_CREATION_CHANCE) {
                    this.lastPowerUpCreation = currentTime;
                    return true;
                }
            } catch (Exception e) {
                // W przypadku błędu, czasami zwróć true
                if ((currentTime / 1000) % 5 == 0) { // Co 5 sekund
                    this.lastPowerUpCreation = currentTime;
                    return true;
                }
            }
            this.lastPowerUpCreation = currentTime;
        }
        return false;
    }

    public void reset() {
        this.isFrightened = false;
        this.isFrozen = false;
        ensureRandomInitialized();
        this.currentDirection = getRandomDirection();
    }

    // Metoda wywoływana po deserializacji
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        initializeRandom(); // Przywróć Random po deserializacji
    }

    @Override
    public String toString() {
        return String.format("Ghost{id=%d, pos=(%d,%d), color=%s, frightened=%s}",
                id, row, col, color, isFrightened);
    }
}