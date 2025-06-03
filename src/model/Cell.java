package model;

import java.io.Serializable;

public class Cell implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum CellType {
        EMPTY, WALL, DOT, POWER_PELLET, PACMAN, GHOST, POWERUP
    }

    private CellType type;
    private boolean hasBeenVisited;
    private PowerUp powerUp;
    private int ghostId;

    public Cell(CellType type) {
        this.type = type;
        this.hasBeenVisited = false;
        this.powerUp = null;
        this.ghostId = -1;
    }

    public Cell(Cell other) {
        this.type = other.type;
        this.hasBeenVisited = other.hasBeenVisited;
        this.powerUp = other.powerUp;
        this.ghostId = other.ghostId;
    }

    public CellType getType() { return type; }
    public void setType(CellType type) { this.type = type; }

    public boolean hasBeenVisited() { return hasBeenVisited; }
    public void setVisited(boolean visited) { this.hasBeenVisited = visited; }

    public PowerUp getPowerUp() { return powerUp; }
    public void setPowerUp(PowerUp powerUp) {
        this.powerUp = powerUp;
        if (powerUp != null) {
            this.type = CellType.POWERUP;
        }
    }

    public int getGhostId() { return ghostId; }
    public void setGhostId(int ghostId) { this.ghostId = ghostId; }

    public boolean isPassable() {
        return type != CellType.WALL;
    }

    public boolean hasCollectible() {
        return type == CellType.DOT || type == CellType.POWER_PELLET || type == CellType.POWERUP;
    }

    public void clear() {
        if (type == CellType.DOT || type == CellType.POWER_PELLET || type == CellType.POWERUP) {
            this.type = CellType.EMPTY;
            this.powerUp = null;
        }
    }

    @Override
    public String toString() {
        return type.toString();
    }
}