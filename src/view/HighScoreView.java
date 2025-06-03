package view;

import utils.HighScoreManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class HighScoreView extends JFrame {

    private JList<String> scoreList;
    private DefaultListModel<String> listModel;
    private JButton backButton;
    private JButton clearButton;
    private HighScoreManager scoreManager;

    public HighScoreView(HighScoreManager scoreManager) {
        this.scoreManager = scoreManager;
        initializeComponents();
        setupLayout();
        setupWindow();
        setupEventListeners();
        loadHighScores();
    }

    private void initializeComponents() {
        listModel = new DefaultListModel<>();
        scoreList = new JList<>(listModel);

        scoreList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        scoreList.setBackground(Color.BLACK);
        scoreList.setForeground(Color.YELLOW);
        scoreList.setSelectionBackground(Color.BLUE);
        scoreList.setSelectionForeground(Color.WHITE);

        backButton = new JButton("Back to Menu");
        clearButton = new JButton("Clear Scores");

        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        backButton.setFont(buttonFont);
        clearButton.setFont(buttonFont);

        backButton.setBackground(Color.GREEN);
        backButton.setForeground(Color.BLACK);
        clearButton.setBackground(Color.RED);
        clearButton.setForeground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("HIGH SCORES", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JScrollPane scrollPane = new JScrollPane(scoreList);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.getViewport().setBackground(Color.BLACK);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(backButton);
        buttonPanel.add(clearButton);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupWindow() {
        setTitle("Pacman - High Scores");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(Color.BLACK);
    }

    private void setupEventListeners() {
        backButton.addActionListener(e -> setVisible(false));

        clearButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to clear all high scores?",
                    "Clear High Scores",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                scoreManager.clearScores();
                loadHighScores();
            }
        });
    }

    public void loadHighScores() {
        listModel.clear();

        List<HighScoreManager.HighScore> scores = scoreManager.getHighScores();

        if (scores.isEmpty()) {
            listModel.addElement("No high scores yet!");
        } else {
            for (int i = 0; i < scores.size(); i++) {
                HighScoreManager.HighScore score = scores.get(i);
                String scoreText = String.format("%2d. %-15s %8d points",
                        i + 1, score.getPlayerName(), score.getScore());
                listModel.addElement(scoreText);
            }
        }
    }

    public void addBackButtonListener(ActionListener listener) {
        backButton.addActionListener(listener);
    }

    public void showHighScores() {
        loadHighScores();
        setVisible(true);
    }
}