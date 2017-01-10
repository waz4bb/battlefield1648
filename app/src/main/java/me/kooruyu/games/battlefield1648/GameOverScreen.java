package me.kooruyu.games.battlefield1648;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import me.kooruyu.games.battlefield1648.util.GameData;

public class GameOverScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over_screen);

        //unpack data
        GameData gameData;

        Bundle extras = getIntent().getExtras();
        setFields((GameData) extras.getSerializable("GAME_DATA"));

        findViewById(R.id.main_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GameOverScreen.this, Startscreen.class));
            }
        });
    }

    private void setFields(GameData gameData) {
        ((TextView) findViewById(R.id.items_input)).setText(Integer.toString(gameData.getCollectedItems().size()));
        ((TextView) findViewById(R.id.itemsTotal)).setText(Integer.toString(gameData.itemsTotal));
        ((TextView) findViewById(R.id.turnsCount)).setText(Integer.toString(gameData.getNumTurns()));
        ((TextView) findViewById(R.id.turnsCount)).setText(Integer.toString(gameData.getNumTurns()));
        ((TextView) findViewById(R.id.discovered_bool)).setText((gameData.getNumTurns() == 0) ? "Nein" : String.format(Locale.ENGLISH, "Für %d turns", gameData.getNumTurns()));
        ((TextView) findViewById(R.id.survived_bool)).setText((gameData.hasFailed()) ? "Nein" : "Ja");
        ((TextView) findViewById(R.id.kills_count)).setText(Integer.toString(gameData.getEnemiesShot()));
        ((TextView) findViewById(R.id.fate_result)).setText(gameData.getFate());
        if (gameData.getCollectedItems().size() > 0) {
            StringBuilder itemsBuilder = new StringBuilder();
            for (String item : gameData.getCollectedItems()) {
                itemsBuilder.append(item);
                itemsBuilder.append(",");
            }

            ((TextView) findViewById(R.id.itemslist_input)).setText(itemsBuilder.substring(0, itemsBuilder.length() - 2));
        }
        ((TextView) findViewById(R.id.itemslist_input)).setText("Keine");

        double score;
        if (gameData.hasFailed() && gameData.getCollectedItems().size() == 0) {
            score = 0;
        } else {
            score = (Math.pow(0.91, gameData.getNumTurns() - 120) + 1) //0.91 ^ (numTurns - 120) - 1
                    * ((gameData.getCollectedItems().size() == 0) ? 0.5 : Math.pow(1.2, gameData.getCollectedItems().size())) //numItems ^ 1.2 or 1 if numItems is 0
                    * ((gameData.hasFailed()) ? 0.5 : 1)
                    * ((gameData.getEnemiesShot() == 0) ? 2 : (gameData.getEnemiesShot() == 1) ? 1 : 0.5);
        }

        ((TextView) findViewById(R.id.count_input)).setText(String.format(Locale.ENGLISH, "%.1f", score));
    }
}
