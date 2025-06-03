package view;

import model.GameModel;
import model.PowerUp;
import controller.GameController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.List;

public class GameView extends JFrame implements GameModel.GameModelListener {

    private GameTableModel tableModel;
    private JTable gameTable;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel timeLabel;
    private JPanel powerUpPanel;
    private GameController controller;

    public GameView(GameController controller) {
        this.controller = controller;
        initializeComponents();
        setupLayout();
        setupWindow();
    }

    private void initializeComponents() {
        scoreLabel = new JLabel("Score: 0");
        livesLabel = new JLabel("Lives: 3");
        timeLabel = new JLabel("Time: 00:00");
        powerUpPanel = new JPanel(new FlowLayout());

        Font labelFont = new Font("Arial", Font.BOLD, 18);
        scoreLabel.setFont(labelFont);
        livesLabel.setFont(labelFont);
        timeLabel.setFont(labelFont);

        scoreLabel.setForeground(Color.WHITE);
        livesLabel.setForeground(Color.WHITE);
        timeLabel.setForeground(Color.WHITE);
    }

    public void initializeGameTable(int rows, int cols) {
        tableModel = new GameTableModel(rows, cols);
        gameTable = new JTable(tableModel);

        gameTable.setDefaultRenderer(Object.class, new GameCellRenderer());
        gameTable.setRowSelectionAllowed(false);
        gameTable.setColumnSelectionAllowed(false);
        gameTable.setCellSelectionEnabled(false);
        gameTable.setFocusable(true);
        gameTable.setShowGrid(false);
        gameTable.setIntercellSpacing(new Dimension(0, 0));
        gameTable.setBackground(Color.BLACK);

        // Ukryj nagłówki
        gameTable.setTableHeader(null);
        gameTable.setShowVerticalLines(false);
        gameTable.setShowHorizontalLines(false);

        // Ustaw rozmiary komórek
        int cellSize = 30;
        for (int i = 0; i < gameTable.getColumnCount(); i++) {
            gameTable.getColumnModel().getColumn(i).setPreferredWidth(cellSize);
            gameTable.getColumnModel().getColumn(i).setMaxWidth(cellSize);
            gameTable.getColumnModel().getColumn(i).setMinWidth(cellSize);
        }

        gameTable.setRowHeight(cellSize);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBackground(Color.BLACK);
        topPanel.add(scoreLabel);
        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(livesLabel);
        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(timeLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.BLACK);
        JLabel powerUpLabel = new JLabel("Active Power-ups: ");
        powerUpLabel.setForeground(Color.WHITE);
        powerUpLabel.setFont(new Font("Arial", Font.BOLD, 14));
        bottomPanel.add(powerUpLabel, BorderLayout.WEST);
        bottomPanel.add(powerUpPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupWindow() {
        setTitle("Pacman Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.BLACK);
        getContentPane().setBackground(Color.BLACK);
        setResizable(true);
    }

    public void showGame() {
        if (gameTable != null) {
            // Oblicz rozmiary
            int tableWidth = gameTable.getColumnCount() * 30;
            int tableHeight = gameTable.getRowCount() * 30;

            // Rozmiar ekranu
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int maxWidth = (int)(screenSize.width * 0.9);
            int maxHeight = (int)(screenSize.height * 0.8);

            JScrollPane scrollPane;

            // DECYZJA: Czy pokazać suwaki?
            if (tableWidth <= maxWidth && tableHeight <= maxHeight - 150) {
                // MAŁA PLANSZA - BEZ SUWAKÓW
                scrollPane = new JScrollPane(gameTable,
                        JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                scrollPane.setPreferredSize(new Dimension(tableWidth + 10, tableHeight + 10));

            } else {
                // DUŻA PLANSZA - Z SUWAKAMI
                scrollPane = new JScrollPane(gameTable,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                scrollPane.setPreferredSize(new Dimension(
                        Math.min(tableWidth + 20, maxWidth),
                        Math.min(tableHeight + 20, maxHeight - 150)
                ));
            }

            // Usuń wszystkie nagłówki
            scrollPane.setColumnHeaderView(null);
            scrollPane.setRowHeaderView(null);
            scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, null);
            scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, null);
            scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, null);
            scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, null);

            scrollPane.getViewport().setBackground(Color.BLACK);
            scrollPane.setBorder(null);

            add(scrollPane, BorderLayout.CENTER);
        }

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        if (gameTable != null) {
            gameTable.requestFocusInWindow();
        }
    }

    public void addKeyListener(KeyListener keyListener) {
        if (gameTable != null) {
            gameTable.addKeyListener(keyListener);
        }
        super.addKeyListener(keyListener);
    }

    @Override
    public void onModelChanged(GameModel model) {
        SwingUtilities.invokeLater(() -> {
            updateUI(model);
        });
    }

    private void updateUI(GameModel model) {
        scoreLabel.setText("Score: " + model.getScore());
        livesLabel.setText("Lives: " + model.getLives());

        long timeSeconds = model.getGameTime() / 1000;
        long minutes = timeSeconds / 60;
        long seconds = timeSeconds % 60;
        timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

        updatePowerUpPanel(model.getActivePowerUps());
        updateGameBoard(model);

        if (model.getGameState() == GameModel.GameState.GAME_OVER) {
            showGameOverDialog(model.getScore());
        } else if (model.getGameState() == GameModel.GameState.VICTORY) {
            showVictoryDialog(model.getScore());
        }
    }

    private void updatePowerUpPanel(List<PowerUp> activePowerUps) {
        powerUpPanel.removeAll();

        for (PowerUp powerUp : activePowerUps) {
            JLabel powerUpLabel = new JLabel(
                    powerUp.getType().getSymbol() + " " + powerUp.getRemainingTimeSeconds() + "s"
            );
            powerUpLabel.setForeground(Color.MAGENTA);
            powerUpLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
            powerUpPanel.add(powerUpLabel);
        }

        powerUpPanel.revalidate();
        powerUpPanel.repaint();
    }

    private void updateGameBoard(GameModel model) {
        if (tableModel != null && model.getGameBoard() != null) {
            tableModel.updateBoard(model.getGameBoard().getBoardCopy());
        }
    }

    private void showGameOverDialog(int finalScore) {
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Game Over! Final Score: " + finalScore + "\nPlay again?",
                    "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                controller.startNewGame();
            } else {
                controller.returnToMenu();
            }
        });
    }

    private void showVictoryDialog(int finalScore) {
        SwingUtilities.invokeLater(() -> {
            String playerName = JOptionPane.showInputDialog(
                    this,
                    "Victory! Final Score: " + finalScore + "\nEnter your name for high score:",
                    "Victory!",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (playerName != null && !playerName.trim().isEmpty()) {
                controller.saveHighScore(playerName.trim(), finalScore);
            }

            controller.returnToMenu();
        });
    }

    public GameTableModel getTableModel() {
        return tableModel;
    }

    public JTable getGameTable() {
        return gameTable;
    }
}