package me.kooruyu.games.battlefield1648;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import me.kooruyu.games.battlefield1648.views.CanvasView;

public class GameContent extends Activity {

    private CanvasView canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        canvas = new CanvasView(this);
        setContentView(R.layout.activity_game_content);
        ((RelativeLayout) findViewById(R.id.activity_game_content)).addView(canvas);
    }
}
