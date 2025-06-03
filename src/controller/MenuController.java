package controller;

import view.MainMenuView;
import view.HighScoreView;
import utils.HighScoreManager;

public class MenuController {

    private MainMenuView menuView;
    private HighScoreView highScoreView;
    private GameController gameController;
    private HighScoreManager highScoreManager;

    public MenuController(HighScoreManager highScoreManager) {
        this.highScoreManager = highScoreManager;
        this.gameController = new GameController();

        this.menuView = new MainMenuView(this);
        this.highScoreView = new HighScoreView(highScoreManager);

        gameController.setMenuView(menuView);

        setupHighScoreViewListeners();
    }

    private void setupHighScoreViewListeners() {
        highScoreView.addBackButtonListener(e -> {
            highScoreView.setVisible(false);
            menuView.showMenu();
        });
    }

    public void startNewGame(int rows, int cols) {
        gameController.startNewGame(rows, cols);
    }

    public void showHighScores() {
        menuView.hideMenu();
        highScoreView.showHighScores();
    }

    public void exitApplication() {
        int option = javax.swing.JOptionPane.showConfirmDialog(
                menuView,
                "Are you sure you want to exit?",
                "Exit Application",
                javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (option == javax.swing.JOptionPane.YES_OPTION) {
            gameController.shutdown();
            System.exit(0);
        }
    }

    public void showMainMenu() {
        if (highScoreView != null) {
            highScoreView.setVisible(false);
        }
        menuView.showMenu();
    }

    public GameController getGameController() {
        return gameController;
    }

    public MainMenuView getMenuView() {
        return menuView;
    }

    public HighScoreView getHighScoreView() {
        return highScoreView;
    }
}