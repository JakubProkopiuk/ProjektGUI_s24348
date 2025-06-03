package utils;

import java.io.*;
import java.util.*;
import java.util.List;

public class HighScoreManager {

    private static final String HIGH_SCORE_FILE = "highscores.dat";
    private static final int MAX_HIGH_SCORES = 10;

    private List<HighScore> highScores;

    public HighScoreManager() {
        this.highScores = new ArrayList<>();
        loadScores();
    }

    public static class HighScore implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String playerName;
        private final int score;
        private final Date date;

        public HighScore(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
            this.date = new Date();
        }

        public String getPlayerName() { return playerName; }
        public int getScore() { return score; }
        public Date getDate() { return date; }

        @Override
        public String toString() {
            return String.format("%s - %d points", playerName, score);
        }
    }

    public synchronized void addScore(String playerName, int score) {
        HighScore newScore = new HighScore(playerName, score);
        highScores.add(newScore);

        highScores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores = highScores.subList(0, MAX_HIGH_SCORES);
        }

        saveScores();

        System.out.println("Score saved: " + newScore);
    }

    public List<HighScore> getHighScores() {
        return new ArrayList<>(highScores);
    }

    public boolean isHighScore(int score) {
        if (highScores.size() < MAX_HIGH_SCORES) {
            return true;
        }

        return score > highScores.get(highScores.size() - 1).getScore();
    }

    public void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(HIGH_SCORE_FILE))) {
            oos.writeObject(highScores);
            System.out.println("High scores saved to " + HIGH_SCORE_FILE);
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadScores() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(HIGH_SCORE_FILE))) {
            highScores = (List<HighScore>) ois.readObject();
            System.out.println("High scores loaded: " + highScores.size() + " entries");
        } catch (FileNotFoundException e) {
            System.out.println("No high score file found, starting fresh");
            highScores = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading high scores: " + e.getMessage());
            highScores = new ArrayList<>();
        }
    }

    public void clearScores() {
        highScores.clear();
        saveScores();
    }

    public int getHighestScore() {
        if (highScores.isEmpty()) {
            return 0;
        }
        return highScores.get(0).getScore();
    }

    public int getScoreCount() {
        return highScores.size();
    }
}