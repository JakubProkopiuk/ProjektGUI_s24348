package model;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private int row;
    private int col;
    private Direction currentDirection;
    private Direction nextDirection;
    private float speedMultiplier;
    private boolean canPassThroughWalls;
    private boolean hasDoublePoints;
    private int animationFrame;
    private long lastMoveTime;

    public Player(int startRow, int startCol) {
        this.row = startRow;
        this.col = startCol;
        this.currentDirection = null;
        this.nextDirection = null;
        this.speedMultiplier = 1.0f;
        this.canPassThroughWalls = false;
        this.hasDoublePoints = false;
        this.animationFrame = 0;
        this.lastMoveTime = 0;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public synchronized void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        this.lastMoveTime = System.currentTimeMillis();
    }

    public Direction getCurrentDirection() { return currentDirection; }
    public void setCurrentDirection(Direction direction) { this.currentDirection = direction; }

    public Direction getNextDirection() { return nextDirection; }
    public void setNextDirection(Direction direction) { this.nextDirection = direction; }

    public float getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(float multiplier) { this.speedMultiplier = multiplier; }

    public boolean canPassThroughWalls() { return canPassThroughWalls; }
    public void setCanPassThroughWalls(boolean canPass) { this.canPassThroughWalls = canPass; }

    public boolean hasDoublePoints() { return hasDoublePoints; }
    public void setHasDoublePoints(boolean hasDouble) { this.hasDoublePoints = hasDouble; }

    public int getAnimationFrame() { return animationFrame; }

    public void updateAnimation() {
        animationFrame = (animationFrame + 1) % 4;
    }

    public long getLastMoveTime() { return lastMoveTime; }

    public boolean isMoving() {
        return currentDirection != null;
    }

    public void stop() {
        this.currentDirection = null;
        this.nextDirection = null;
    }

    public void reset(int startRow, int startCol) {
        this.row = startRow;
        this.col = startCol;
        this.currentDirection = null;
        this.nextDirection = null;
        this.speedMultiplier = 1.0f;
        this.canPassThroughWalls = false;
        this.hasDoublePoints = false;
        this.animationFrame = 0;
    }

    @Override
    public String toString() {
        return String.format("Player{pos=(%d,%d), dir=%s, speed=%.1f}",
                row, col, currentDirection, speedMultiplier);
    }
}