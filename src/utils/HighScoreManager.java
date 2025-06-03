package utils;

import java.io.*;
import java.util.*;

public class HighScoreManager {

    private static final String HIGH_SCORES_FILE = "highscores.dat";
    private static final int MAX_HIGH_SCORES = 10;

    private List<HighScore> highScores;

    public static class HighScore implements Serializable, Comparable<HighScore> {
        private static final long serialVersionUID = 1L;

        private final String playerName;
        private final int score;
        private final long timestamp;

        public HighScore(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
            this.timestamp = System.currentTimeMillis();
        }

        public String getPlayerName() { return playerName; }
        public int getScore() { return score; }
        public long getTimestamp() { return timestamp; }

        @Override
        public int compareTo(HighScore other) {
            int scoreComparison = Integer.compare(other.score, this.score);
            if (scoreComparison == 0) {
                return Long.compare(this.timestamp, other.timestamp);
            }
            return scoreComparison;
        }

        @Override
        public String toString() {
            return String.format("%s: %d points", playerName, score);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            HighScore highScore = (HighScore) obj;
            return score == highScore.score &&
                    timestamp == highScore.timestamp &&
                    Objects.equals(playerName, highScore.playerName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playerName, score, timestamp);
        }
    }

    public HighScoreManager() {
        this.highScores = new ArrayList<>();
        loadScores();
    }

    public void addScore(String playerName, int score) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }

        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        HighScore newScore = new HighScore(playerName.trim(), score);
        highScores.add(newScore);

        Collections.sort(highScores);

        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores = highScores.subList(0, MAX_HIGH_SCORES);
        }

        saveScores();
    }

    public List<HighScore> getHighScores() {
        return new ArrayList<>(highScores);
    }

    public boolean isHighScore(int score) {
        if (highScores.size() < MAX_HIGH_SCORES) {
            return true;
        }

        HighScore lowestScore = highScores.get(highScores.size() - 1);
        return score > lowestScore.getScore();
    }

    public int getPosition(int score) {
        for (int i = 0; i < highScores.size(); i++) {
            if (score > highScores.get(i).getScore()) {
                return i + 1;
            }
        }

        if (highScores.size() < MAX_HIGH_SCORES) {
            return highScores.size() + 1;
        }

        return -1;
    }

    public void clearScores() {
        highScores.clear();
        saveScores();
    }

    public void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(HIGH_SCORES_FILE))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadScores() {
        File file = new File(HIGH_SCORES_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(HIGH_SCORES_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                highScores = (List<HighScore>) obj;
                Collections.sort(highScores);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading high scores: " + e.getMessage());
            highScores = new ArrayList<>();
        }
    }

    public int getHighScoreCount() {
        return highScores.size();
    }

    public HighScore getHighestScore() {
        if (highScores.isEmpty()) {
            return null;
        }
        return highScores.get(0);
    }

    public int getMaxHighScores() {
        return MAX_HIGH_SCORES;
    }

    public void exportScores(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("PACMAN HIGH SCORES");
            writer.println("==================");
            writer.println();

            for (int i = 0; i < highScores.size(); i++) {
                HighScore score = highScores.get(i);
                writer.printf("%2d. %-15s %8d points%n",
                        i + 1, score.getPlayerName(), score.getScore());
            }
        }
    }

    public void importScores(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filename))) {
            @SuppressWarnings("unchecked")
            List<HighScore> importedScores = (List<HighScore>) ois.readObject();

            highScores.addAll(importedScores);
            Collections.sort(highScores);

            if (highScores.size() > MAX_HIGH_SCORES) {
                highScores = highScores.subList(0, MAX_HIGH_SCORES);
            }

            saveScores();
        }
    }
}