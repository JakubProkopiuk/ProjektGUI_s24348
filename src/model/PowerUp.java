package model;

import java.io.Serializable;

public class PowerUp implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum PowerUpType {
        SPEED_BOOST("Speed Boost +50%", 10000, "⚡"),
        GHOST_FREEZE("Freeze Ghosts", 5000, "❄️"),
        DOUBLE_POINTS("Double Points", 15000, "💎"),
        EXTRA_LIFE("Extra Life", 0, "❤️"),
        POWER_MODE("Super Power Mode", 20000, "⭐"),
        WALL_PASS("Phase Through Walls", 8000, "👻"),
        POINT_MAGNET("Point Magnet", 12000, "🧲");

        private final String description;
        private final int durationMs;
        private final String symbol;

        PowerUpType(String description, int durationMs, String symbol) {
            this.description = description;
            this.durationMs = durationMs;
            this.symbol = symbol;
        }

        public String getDescription() { return description; }
        public int getDurationMs() { return durationMs; }
        public String getSymbol() { return symbol; }
        public boolean isInstant() { return durationMs == 0; }
    }

    private final PowerUpType type;
    private final long createdTime;
    private long activatedTime;
    private boolean isActive;
    private boolean isCollected;

    public PowerUp(PowerUpType type) {
        this.type = type;
        this.createdTime = System.currentTimeMillis();
        this.activatedTime = 0;
        this.isActive = false;
        this.isCollected = false;
    }

    public PowerUpType getType() { return type; }
    public long getCreatedTime() { return createdTime; }
    public boolean isActive() { return isActive; }
    public boolean isCollected() { return isCollected; }

    public synchronized void activate() {
        if (!isCollected) {
            this.isActive = true;
            this.isCollected = true;
            this.activatedTime = System.currentTimeMillis();
        }
    }

    public boolean isExpired() {
        if (!isActive || type.isInstant()) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - activatedTime) >= type.getDurationMs();
    }

    public int getRemainingTimeSeconds() {
        if (!isActive || type.isInstant()) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - activatedTime;
        long remaining = type.getDurationMs() - elapsed;

        return Math.max(0, (int)(remaining / 1000));
    }

    public synchronized void deactivate() {
        this.isActive = false;
    }

    @Override
    public String toString() {
        return String.format("PowerUp{type=%s, active=%s, remaining=%ds}",
                type, isActive, getRemainingTimeSeconds());
    }
}