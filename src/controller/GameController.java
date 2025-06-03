package controller;

import model.GameModel;
import model.Player;
import view.GameView;
import view.MainMenuView;
import utils.ThreadManager;
import utils.HighScoreManager;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameController implements KeyListener {

    private GameModel gameModel;
    private GameView gameView;
    private MainMenuView menuView;
    private ThreadManager threadManager;
    private HighScoreManager highScoreManager;
    private boolean gameRunning;

    public GameController() {
        this.gameModel = new GameModel();
        this.threadManager = new ThreadManager();
        this.highScoreManager = new HighScoreManager();
        this.gameRunning = false;

        this.gameView = new GameView(this);
        gameModel.addListener(gameView);
    }

    public void setMenuView(MainMenuView menuView) {
        this.menuView = menuView;
    }

    public void startNewGame(int rows, int cols) {
        try {
            stopCurrentGame();

            gameModel.initializeGame(rows, cols);
            gameView.initializeGameTable(rows, cols);
            gameView.addKeyListener(this);

            if (menuView != null) {
                menuView.hideMenu();
            }

            gameView.showGame();
            gameRunning = true;

            threadManager.startGameThreads(gameModel, gameView);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    gameView,
                    "Error starting game: " + e.getMessage(),
                    "Game Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void startNewGame() {
        if (menuView != null) {
            menuView.showMenu();
            gameView.setVisible(false);
        }
    }

    public void returnToMenu() {
        stopCurrentGame();

        if (gameView != null) {
            gameView.setVisible(false);
            gameView.dispose();
        }

        if (menuView != null) {
            menuView.showMenu();
        }
    }

    public void stopCurrentGame() {
        gameRunning = false;
        threadManager.stopAllThreads();

        if (gameModel != null) {
            gameModel.endGame();
        }
    }

    public void pauseGame() {
        threadManager.pauseThreads();
    }

    public void resumeGame() {
        threadManager.resumeThreads();
    }

    public void saveHighScore(String playerName, int score) {
        highScoreManager.addScore(playerName, score);
    }

    public HighScoreManager getHighScoreManager() {
        return highScoreManager;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameRunning) return;

        int keyCode = e.getKeyCode();

        if (e.isControlDown() && e.isShiftDown() && keyCode == KeyEvent.VK_Q) {
            handleForceExit();
            return;
        }

        Player.Direction direction = null;

        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                direction = Player.Direction.UP;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                direction = Player.Direction.DOWN;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                direction = Player.Direction.LEFT;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                direction = Player.Direction.RIGHT;
                break;
            case KeyEvent.VK_SPACE:
                handlePause();
                break;
            case KeyEvent.VK_ESCAPE:
                handleEscape();
                break;
        }

        if (direction != null) {
            gameModel.movePacman(direction);
        }
    }

    private void handleForceExit() {
        SwingUtilities.invokeLater(() -> {
            if (gameRunning) {
                int option = JOptionPane.showConfirmDialog(
                        gameView,
                        "Are you sure you want to exit to main menu?",
                        "Exit Game",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (option == JOptionPane.YES_OPTION) {
                    returnToMenu();
                }
            }
        });
    }

    private void handlePause() {
        if (threadManager.isPaused()) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    private void handleEscape() {
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(
                    gameView,
                    "Pause game and return to menu?",
                    "Pause Game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                returnToMenu();
            }
        });
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void shutdown() {
        stopCurrentGame();
        threadManager.shutdown();
        highScoreManager.saveScores();
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public GameModel getGameModel() {
        return gameModel;
    }

    public GameView getGameView() {
        return gameView;
    }
}