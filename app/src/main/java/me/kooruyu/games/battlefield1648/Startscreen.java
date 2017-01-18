package me.kooruyu.games.battlefield1648;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import me.kooruyu.games.battlefield1648.gameData.SaveData;

public class Startscreen extends Activity {

    private SaveData save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_start_screen);

        File saveGame = new File(getApplicationContext().getFilesDir(), SaveData.DEFAULT_SAVEFILE);

        if (saveGame.exists()) {
            try {
                save = SaveData.loadSaveGame(saveGame);
            } catch (IOException e) {
                save = new SaveData(SaveData.DEFAULT_USERNAME);
                try {
                    save.writeToFile(saveGame);
                } catch (IOException e1) {
                    throw new RuntimeException("Saving Game failed!");
                }
            }
        } else {
            save = new SaveData(SaveData.DEFAULT_USERNAME);
            try {
                save.writeToFile(saveGame);
            } catch (IOException e) {
                throw new RuntimeException("Saving Game failed!");
            }
        }

        //start game
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame(view);
            }
        });

        //perferences
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Startscreen.this, PreferencesScreen.class);
                startActivity(intent);
            }
        });

        //highscores
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Startscreen.this, RankingScreen.class));
            }
        });
    }

    public void startGame(View v) {
        //startActivity(new Intent(this, Prologue.class));
        startActivity(new Intent(this, GameContent.class));
        overridePendingTransition(0, 0);
        finish();
    }
}
