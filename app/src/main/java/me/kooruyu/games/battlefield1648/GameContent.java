package me.kooruyu.games.battlefield1648;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import me.kooruyu.games.battlefield1648.views.MainGameView;

public class GameContent extends Activity {

    private MainGameView canvas;
    private Bundle previousState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game_content);

        canvas = (MainGameView) findViewById(R.id.canvas);
        previousState = null;
    }


    @Override
    protected void onStop() {
        super.onStop();

        previousState = canvas.saveState();
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (previousState != null) {
            canvas.restoreState(previousState);
        }
    }
}
