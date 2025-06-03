package view;

import model.Cell;
import model.PowerUp;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class GameCellRenderer extends DefaultTableCellRenderer {

    private static final int CELL_SIZE = 30;

    private static final Color WALL_COLOR = new Color(0, 0, 200);
    private static final Color EMPTY_COLOR = Color.BLACK;
    private static final Color DOT_COLOR = Color.YELLOW;
    private static final Color PACMAN_COLOR = Color.YELLOW;
    private static final Color GHOST_RED = Color.RED;
    private static final Color GHOST_PINK = Color.PINK;
    private static final Color GHOST_CYAN = Color.CYAN;
    private static final Color GHOST_ORANGE = Color.ORANGE;
    private static final Color POWERUP_COLOR = Color.MAGENTA;

    private int animationFrame = 0;
    private long lastAnimationUpdate = 0;
    private static final int ANIMATION_SPEED = 600;

    public GameCellRenderer() {
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        updateAnimation();

        if (value instanceof Cell) {
            Cell cell = (Cell) value;
            renderCell(cell);
        } else {
            setBackground(EMPTY_COLOR);
            setText("");
        }

        // Wymuś repaint dla animacji
        table.repaint();

        return this;
    }

    private void renderCell(Cell cell) {
        setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        setText("");
        setIcon(null);

        switch (cell.getType()) {
            case WALL:
                setBackground(WALL_COLOR);
                setText("█");
                setForeground(WALL_COLOR);
                break;

            case EMPTY:
                setBackground(EMPTY_COLOR);
                setText("");
                break;

            case DOT:
                setBackground(EMPTY_COLOR);
                setText("•");
                setForeground(DOT_COLOR);
                break;

            case POWER_PELLET:
                setBackground(EMPTY_COLOR);
                setText("●");
                setForeground(DOT_COLOR);
                break;

            case PACMAN:
                setBackground(EMPTY_COLOR);
                renderPacman();
                break;

            case GHOST:
                setBackground(EMPTY_COLOR);
                renderGhost(cell.getGhostId());
                break;

            case POWERUP:
                setBackground(EMPTY_COLOR);
                renderPowerUp(cell.getPowerUp());
                break;

            default:
                setBackground(EMPTY_COLOR);
                setText("");
        }
    }

    private void renderPacman() {
        setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));

        // Animacja Pacmana - otwarte/zamknięte usta
        if (animationFrame % 2 == 0) {
            setText("😮"); // Otwarte usta
        } else {
            setText("😯"); // Zamknięte usta
        }
        setForeground(PACMAN_COLOR);

        // Fallback jeśli emotki nie działają
        if (getText().equals("😮") || getText().equals("😯")) {
            // Emotki działają
        } else {
            // Fallback do zwykłych znaków
            setFont(new Font("Arial", Font.BOLD, 16));
            if (animationFrame % 2 == 0) {
                setText("◔");
            } else {
                setText("●");
            }
        }
    }

    private void renderGhost(int ghostId) {
        setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        Color ghostColor;

        switch (ghostId) {
            case 0:
                ghostColor = GHOST_RED;
                setText("👻");
                break;
            case 1:
                ghostColor = GHOST_PINK;
                setText("👻");
                break;
            case 2:
                ghostColor = GHOST_CYAN;
                setText("👻");
                break;
            case 3:
                ghostColor = GHOST_ORANGE;
                setText("👻");
                break;
            default:
                ghostColor = GHOST_RED;
                setText("👻");
        }

        setForeground(ghostColor);

        // Fallback jeśli emotki nie działają
        if (getText().equals("👻")) {
            // Emotka działa
        } else {
            // Fallback
            setFont(new Font("Arial", Font.BOLD, 16));
            setText("G");
            setForeground(ghostColor);
        }
    }

    private void renderPowerUp(PowerUp powerUp) {
        setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        if (powerUp != null) {
            // Użyj emoji dla power-upów
            switch (powerUp.getType()) {
                case SPEED_BOOST:
                    setText("⚡");
                    break;
                case GHOST_FREEZE:
                    setText("❄️");
                    break;
                case DOUBLE_POINTS:
                    setText("💎");
                    break;
                case EXTRA_LIFE:
                    setText("❤️");
                    break;
                case POWER_MODE:
                    setText("⭐");
                    break;
                case WALL_PASS:
                    setText("👻");
                    break;
                case POINT_MAGNET:
                    setText("🧲");
                    break;
                default:
                    setText(powerUp.getType().getSymbol());
            }
            setForeground(POWERUP_COLOR);
        }
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAnimationUpdate > ANIMATION_SPEED) {
            animationFrame++;
            lastAnimationUpdate = currentTime;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(CELL_SIZE, CELL_SIZE);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(CELL_SIZE, CELL_SIZE);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(CELL_SIZE, CELL_SIZE);
    }
}