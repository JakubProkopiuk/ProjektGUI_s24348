package view;

import controller.MenuController;
import javax.swing.*;
import java.awt.*;

public class MainMenuView extends JFrame {

    private JButton newGameButton;
    private JButton highScoresButton;
    private JButton exitButton;
    private MenuController controller;

    public MainMenuView(MenuController controller) {
        this.controller = controller;
        initializeComponents();
        setupLayout();
        setupWindow();
        setupEventListeners();
    }

    private void initializeComponents() {
        newGameButton = new JButton("New Game");
        highScoresButton = new JButton("High Scores");
        exitButton = new JButton("Exit");

        Font buttonFont = new Font("Arial", Font.BOLD, 16);
        Dimension buttonSize = new Dimension(200, 45);

        // Proste, czyste przyciski z POPRAWNYMI borderami
        newGameButton.setFont(buttonFont);
        newGameButton.setPreferredSize(buttonSize);
        newGameButton.setBackground(new Color(70, 130, 180));
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setBorder(BorderFactory.createRaisedBevelBorder()); // POPRAWNE
        newGameButton.setFocusPainted(false);

        highScoresButton.setFont(buttonFont);
        highScoresButton.setPreferredSize(buttonSize);
        highScoresButton.setBackground(new Color(60, 179, 113));
        highScoresButton.setForeground(Color.WHITE);
        highScoresButton.setBorder(BorderFactory.createRaisedBevelBorder()); // POPRAWNE
        highScoresButton.setFocusPainted(false);

        exitButton.setFont(buttonFont);
        exitButton.setPreferredSize(buttonSize);
        exitButton.setBackground(new Color(220, 20, 60));
        exitButton.setForeground(Color.WHITE);
        exitButton.setBorder(BorderFactory.createRaisedBevelBorder()); // POPRAWNE
        exitButton.setFocusPainted(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        // Prosty tytuł
        JLabel titleLabel = new JLabel("PACMAN", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));

        // Panel przycisków
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.BLACK);

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(createCenteredButton(newGameButton));
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(createCenteredButton(highScoresButton));
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(createCenteredButton(exitButton));
        buttonPanel.add(Box.createVerticalGlue());

        // Instrukcje
        JLabel instructionsLabel = new JLabel(
                "<html><center>Arrow keys or WASD to move<br>SPACE to pause • ESC to exit</center></html>",
                JLabel.CENTER
        );
        instructionsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionsLabel.setForeground(Color.LIGHT_GRAY);
        instructionsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        add(titleLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(instructionsLabel, BorderLayout.SOUTH);
    }

    private JPanel createCenteredButton(JButton button) {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(Color.BLACK);
        panel.add(button);
        return panel;
    }

    private void setupWindow() {
        setTitle("Pacman - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void setupEventListeners() {
        newGameButton.addActionListener(e -> showNewGameDialog());
        highScoresButton.addActionListener(e -> controller.showHighScores());
        exitButton.addActionListener(e -> controller.exitApplication());
    }

    private void showNewGameDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel rowsLabel = new JLabel("Rows (10-40):");
        JTextField rowsField = new JTextField("20");
        JLabel colsLabel = new JLabel("Columns (10-40):");
        JTextField colsField = new JTextField("25");

        panel.add(rowsLabel);
        panel.add(rowsField);
        panel.add(colsLabel);
        panel.add(colsField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "New Game - Board Size",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                int rows = Integer.parseInt(rowsField.getText().trim());
                int cols = Integer.parseInt(colsField.getText().trim());

                if (rows < 10 || rows > 40 || cols < 10 || cols > 40) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Board size must be between 10 and 40!",
                            "Invalid Size",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                controller.startNewGame(rows, cols);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter valid numbers!",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    public void showMenu() {
        setVisible(true);
    }

    public void hideMenu() {
        setVisible(false);
    }
}