package me.kooruyu.games.battlefield1648.gameData;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaveData implements Serializable {
    public static final String DEFAULT_SAVEFILE = "default.save";
    public static final String DEFAULT_USERNAME = "Soldat";

    public static final String HARD = "hard";
    public static final String NORMAL = "normal";

    private List<HighScoreEntry> highscores;
    private String username;
    private String difficulty;

    public SaveData(String username) {
        highscores = new ArrayList<>();
        this.username = username;
        this.difficulty = NORMAL;
    }

    private SaveData(String username, String difficulty, List<HighScoreEntry> highscores) {
        this.username = username;
        this.highscores = highscores;
        this.difficulty = difficulty;
    }

    public List<HighScoreEntry> getHighscores() {
        return highscores;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void writeToFile(File file) throws IOException {
        PrintWriter writer = new PrintWriter(file);

        writer.println(username);
        if (difficulty == null) {
            difficulty = NORMAL;
        }
        writer.println(difficulty);
        for (HighScoreEntry entry : highscores) {
            writer.println(String.format(Locale.ENGLISH, "%s\t:\t%.1f", entry.username, entry.score));
        }

        writer.close();
    }

    public static SaveData loadSaveGame(File file) throws IOException {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("This should never happen: " + e.getMessage());
        }

        String username = reader.readLine();
        String difficulty = reader.readLine();
        List<HighScoreEntry> highscores = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] entry = line.split("\t:\t");
            highscores.add(new HighScoreEntry(entry[0], Double.parseDouble(entry[1])));
        }
        reader.close();

        return new SaveData(username, difficulty, highscores);
    }
}
