package me.kooruyu.games.battlefield1648;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class RankingScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_screen);

        findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RankingScreen.this, Startscreen.class));
            }
        });
    }
}
