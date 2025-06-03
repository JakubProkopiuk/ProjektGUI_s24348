import controller.MenuController;
import utils.HighScoreManager;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                setupLookAndFeel();
                startApplication();
            } catch (Exception e) {
                handleStartupError(e);
            }
        });
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
    }

    private static void startApplication() {
        try {
            HighScoreManager highScoreManager = new HighScoreManager();
            MenuController menuController = new MenuController(highScoreManager);

            setupShutdownHook(menuController);

            menuController.showMainMenu();

        } catch (Exception e) {
            throw new RuntimeException("Failed to start application", e);
        }
    }

    private static void setupShutdownHook(MenuController menuController) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (menuController.getGameController() != null) {
                    menuController.getGameController().shutdown();
                }
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));
    }

    private static void handleStartupError(Exception e) {
        String errorMessage = "Failed to start Pacman Game:\n" + e.getMessage();

        System.err.println(errorMessage);
        e.printStackTrace();

        JOptionPane.showMessageDialog(
                null,
                errorMessage,
                "Startup Error",
                JOptionPane.ERROR_MESSAGE
        );

        System.exit(1);
    }
}