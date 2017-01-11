package me.kooruyu.games.battlefield1648;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import java.io.File;
import java.io.IOException;

import me.kooruyu.games.battlefield1648.gameData.SaveData;

public class PreferencesScreen extends Activity {

    private File saveFile;
    private SaveData save;
    private EditText nameField;
    private boolean difficultyChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_screen);

        difficultyChanged = false;

        saveFile = new File(getApplicationContext().getFilesDir(), SaveData.DEFAULT_SAVEFILE);

        try {
            save = SaveData.loadSaveGame(saveFile);
        } catch (IOException e) {
            throw new RuntimeException("Savegame could not be loaded!");
        }

        nameField = ((EditText) findViewById(R.id.name));

        if (save.getDifficulty().equals(SaveData.NORMAL)) {
            ((RadioButton) findViewById(R.id.radioButton)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.radioButton2)).setChecked(true);
        }

        nameField.setText(save.getUsername());

        findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameField.getText().length() != 0) {
                    save.setUsername(nameField.getText().toString());
                }
                save.setDifficulty((((RadioButton) findViewById(R.id.radioButton)).isChecked() ? SaveData.NORMAL : SaveData.HARD));
                try {
                    save.writeToFile(saveFile);
                } catch (IOException e) {
                    throw new RuntimeException("Savegame could not be saved!");
                }
                startActivity(new Intent(PreferencesScreen.this, Startscreen.class));
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save = new SaveData(SaveData.DEFAULT_USERNAME);
                try {
                    nameField.setText(SaveData.DEFAULT_USERNAME);
                    save.writeToFile(saveFile);
                } catch (IOException e) {
                    throw new RuntimeException("Savegame could not be reset and saved!");
                }
            }
        });
    }

}
