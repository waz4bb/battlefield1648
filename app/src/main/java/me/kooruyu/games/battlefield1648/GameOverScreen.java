package me.kooruyu.games.battlefield1648;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import me.kooruyu.games.battlefield1648.util.GameData;

public class GameOverScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over_screen);

        //unpack data
        GameData gameData;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            gameData = (GameData) extras.getSerializable("GAME_DATA");
        } else {
            gameData = (GameData) savedInstanceState.getSerializable("GAME_DATA");
        }

        ((TextView) findViewById(R.id.textView)).setText(gameData.toString());
    }
}
