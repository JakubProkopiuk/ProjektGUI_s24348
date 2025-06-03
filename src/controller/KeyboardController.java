package controller;

import model.Player;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class KeyboardController implements KeyListener {

    private final GameController gameController;
    private final Set<Integer> pressedKeys;
    private final Set<Integer> consumedKeys;

    private static final Set<Integer> MOVEMENT_KEYS = Set.of(
            KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
            KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D
    );

    private static final Set<Integer> CONTROL_KEYS = Set.of(
            KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER, KeyEvent.VK_Q
    );

    public KeyboardController(GameController gameController) {
        this.gameController = gameController;
        this.pressedKeys = ConcurrentHashMap.newKeySet();
        this.consumedKeys = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (pressedKeys.contains(keyCode)) {
            return;
        }

        pressedKeys.add(keyCode);

        if (handleSpecialKeyCombo(e)) {
            consumedKeys.add(keyCode);
            return;
        }

        if (handleMovementKey(keyCode)) {
            consumedKeys.add(keyCode);
            return;
        }

        if (handleControlKey(keyCode)) {
            consumedKeys.add(keyCode);
            return;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        pressedKeys.remove(keyCode);
        consumedKeys.remove(keyCode);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private boolean handleSpecialKeyCombo(KeyEvent e) {
        if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_Q) {
            gameController.returnToMenu();
            return true;
        }

        return false;
    }

    private boolean handleMovementKey(int keyCode) {
        if (!MOVEMENT_KEYS.contains(keyCode)) {
            return false;
        }

        if (!gameController.isGameRunning()) {
            return false;
        }

        Player.Direction direction = getDirectionFromKey(keyCode);
        if (direction != null) {
            gameController.getGameModel().movePacman(direction);
            return true;
        }

        return false;
    }

    private boolean handleControlKey(int keyCode) {
        if (!CONTROL_KEYS.contains(keyCode)) {
            return false;
        }

        switch (keyCode) {
            case KeyEvent.VK_SPACE:
                if (gameController.isGameRunning()) {
                    gameController.pauseGame();
                }
                return true;

            case KeyEvent.VK_ESCAPE:
                if (gameController.isGameRunning()) {
                    gameController.returnToMenu();
                }
                return true;

            case KeyEvent.VK_ENTER:
                return true;

            default:
                return false;
        }
    }

    private Player.Direction getDirectionFromKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                return Player.Direction.UP;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                return Player.Direction.DOWN;

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                return Player.Direction.LEFT;

            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                return Player.Direction.RIGHT;

            default:
                return null;
        }
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public boolean isMovementKeyPressed() {
        return pressedKeys.stream().anyMatch(MOVEMENT_KEYS::contains);
    }

    public Set<Integer> getPressedKeys() {
        return new HashSet<>(pressedKeys);
    }

    public void clearPressedKeys() {
        pressedKeys.clear();
        consumedKeys.clear();
    }

    public boolean isAnyKeyPressed() {
        return !pressedKeys.isEmpty();
    }

    public Player.Direction getCurrentDirection() {
        for (int keyCode : pressedKeys) {
            if (MOVEMENT_KEYS.contains(keyCode)) {
                Player.Direction direction = getDirectionFromKey(keyCode);
                if (direction != null) {
                    return direction;
                }
            }
        }
        return null;
    }
}