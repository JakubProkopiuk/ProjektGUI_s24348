package utils;

import model.GameModel;
import view.GameView;
import javax.swing.SwingUtilities;

public class ThreadManager {

    private Thread gameThread;
    private Thread animationThread;
    private Thread powerUpThread;
    private Thread renderThread;

    private volatile boolean gameRunning;
    private volatile boolean threadsPaused;
    private final Object pauseLock = new Object();

    // SZYBSZE AKTUALIZACJE
    private static final int GAME_UPDATE_DELAY = 80;      // Zmniejszone z 100 na 80
    private static final int ANIMATION_UPDATE_DELAY = 150; // Zmniejszone z 200 na 150
    private static final int POWERUP_CHECK_DELAY = 500;   // Zmniejszone z 1000 na 500
    private static final int RENDER_UPDATE_DELAY = 40;    // Zmniejszone z 50 na 40

    public ThreadManager() {
        this.gameRunning = false;
        this.threadsPaused = false;
    }

    public void startGameThreads(GameModel gameModel, GameView gameView) {
        stopAllThreads();

        gameRunning = true;
        threadsPaused = false;

        startGameLogicThread(gameModel);
        startAnimationThread(gameView);
        startPowerUpThread(gameModel);
        startRenderThread(gameView);
    }

    private void startGameLogicThread(GameModel gameModel) {
        gameThread = new Thread(() -> {
            while (gameRunning) {
                checkPause();

                try {
                    gameModel.updateGame();
                    Thread.sleep(GAME_UPDATE_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in game logic thread: " + e.getMessage());
                }
            }
        }, "GameLogicThread");

        gameThread.setDaemon(true);
        gameThread.start();
    }

    private void startAnimationThread(GameView gameView) {
        animationThread = new Thread(() -> {
            while (gameRunning) {
                checkPause();

                try {
                    SwingUtilities.invokeLater(() -> {
                        if (gameView.getGameTable() != null) {
                            gameView.getGameTable().repaint();
                        }
                    });

                    Thread.sleep(ANIMATION_UPDATE_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in animation thread: " + e.getMessage());
                }
            }
        }, "AnimationThread");

        animationThread.setDaemon(true);
        animationThread.start();
    }

    private void startPowerUpThread(GameModel gameModel) {
        powerUpThread = new Thread(() -> {
            while (gameRunning) {
                checkPause();

                try {
                    Thread.sleep(POWERUP_CHECK_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in power-up thread: " + e.getMessage());
                }
            }
        }, "PowerUpThread");

        powerUpThread.setDaemon(true);
        powerUpThread.start();
    }

    private void startRenderThread(GameView gameView) {
        renderThread = new Thread(() -> {
            while (gameRunning) {
                checkPause();

                try {
                    SwingUtilities.invokeLater(() -> {
                        if (gameView.getTableModel() != null) {
                            gameView.getTableModel().fireTableDataChanged();
                        }
                    });

                    Thread.sleep(RENDER_UPDATE_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in render thread: " + e.getMessage());
                }
            }
        }, "RenderThread");

        renderThread.setDaemon(true);
        renderThread.start();
    }

    private void checkPause() {
        synchronized (pauseLock) {
            while (threadsPaused && gameRunning) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public void pauseThreads() {
        synchronized (pauseLock) {
            threadsPaused = true;
        }
    }

    public void resumeThreads() {
        synchronized (pauseLock) {
            threadsPaused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isPaused() {
        return threadsPaused;
    }

    public void stopAllThreads() {
        gameRunning = false;

        synchronized (pauseLock) {
            threadsPaused = false;
            pauseLock.notifyAll();
        }

        interruptThread(gameThread);
        interruptThread(animationThread);
        interruptThread(powerUpThread);
        interruptThread(renderThread);

        waitForThread(gameThread);
        waitForThread(animationThread);
        waitForThread(powerUpThread);
        waitForThread(renderThread);
    }

    private void interruptThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    private void waitForThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void shutdown() {
        stopAllThreads();
    }

    public boolean isRunning() {
        return gameRunning;
    }

    public int getActiveThreadCount() {
        int count = 0;
        if (gameThread != null && gameThread.isAlive()) count++;
        if (animationThread != null && animationThread.isAlive()) count++;
        if (powerUpThread != null && powerUpThread.isAlive()) count++;
        if (renderThread != null && renderThread.isAlive()) count++;
        return count;
    }
}