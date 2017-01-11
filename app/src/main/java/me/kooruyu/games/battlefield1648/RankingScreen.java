package me.kooruyu.games.battlefield1648;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import me.kooruyu.games.battlefield1648.gameData.HighScoreEntry;
import me.kooruyu.games.battlefield1648.gameData.SaveData;

public class RankingScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_screen);

        File saveFile = new File(getApplicationContext().getFilesDir(), SaveData.DEFAULT_SAVEFILE);

        SaveData save;
        try {
            save = SaveData.loadSaveGame(saveFile);
        } catch (IOException e) {
            throw new RuntimeException("Savegame could not be loaded!");
        }

        if (!save.getHighscores().isEmpty()) {
            fillTable(save);
        }

        findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RankingScreen.this, Startscreen.class));
            }
        });
    }

    private void fillTable(SaveData save) {
        int i = 0;
        HighScoreEntry entry = save.getHighscores().get(i++);
        ((TextView) findViewById(R.id.first_name)).setText(entry.username);
        ((TextView) findViewById(R.id.first_credit)).setText(Double.toString(entry.score));

        if (i >= save.getHighscores().size()) return;
        entry = save.getHighscores().get(i++);
        ((TextView) findViewById(R.id.second_name)).setText(entry.username);
        ((TextView) findViewById(R.id.second_credit)).setText(Double.toString(entry.score));

        if (i >= save.getHighscores().size()) return;
        entry = save.getHighscores().get(i++);
        ((TextView) findViewById(R.id.third_name)).setText(entry.username);
        ((TextView) findViewById(R.id.third_credit)).setText(Double.toString(entry.score));

        if (i >= save.getHighscores().size()) return;
        entry = save.getHighscores().get(i++);
        ((TextView) findViewById(R.id.fourth_name)).setText(entry.username);
        ((TextView) findViewById(R.id.fourth_credit)).setText(Double.toString(entry.score));

        if (i >= save.getHighscores().size()) return;
        entry = save.getHighscores().get(i);
        ((TextView) findViewById(R.id.fifth_name)).setText(entry.username);
        ((TextView) findViewById(R.id.fifth_credit)).setText(Double.toString(entry.score));
    }
}
