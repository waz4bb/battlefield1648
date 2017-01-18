package me.kooruyu.games.battlefield1648.renderers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.Bundle;
import android.view.SurfaceHolder;

import me.kooruyu.games.battlefield1648.GameContent;
import me.kooruyu.games.battlefield1648.R;


public class PrologueThread extends AbstractCanvasThread {

    private static final int MOVIE_ID = R.mipmap.prolog;
    private final float TIME_SCALE = .8f;

    private Movie movie;

    private long movieStart;
    private int currentAnimationTime;

    private float scaleX;
    private float scaleY;

    /**
     * Creates a new CanvasThread using the given context and SurfaceHolder
     *
     * @param context       the context of the View it's called by
     * @param surfaceHolder the surface holder containing the canvas to be drawn on and receiving callbacks
     */
    public PrologueThread(Context context, SurfaceHolder surfaceHolder) {
        super(context, surfaceHolder);

        init();
    }

    public void setImageResource(int mvId) {
        movie = Movie.decodeStream(context.getResources().openRawResource(mvId));
        //requestLayout();
    }

    @Override
    void init() {
        movieStart = 0;
        currentAnimationTime = 0;

        setImageResource(MOVIE_ID);
    }

    @Override
    void update() {
        if (movie != null) {
            updateAnimationTime();
        }
    }

    @Override
    void draw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        canvas.scale(scaleX, scaleY);
        movie.setTime(currentAnimationTime);
        movie.draw(canvas, scaleX, scaleY);
    }

    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();

        if (movieStart == 0) {
            movieStart = now;
        }
        currentAnimationTime = (int) (((now - movieStart) * TIME_SCALE) % movie.duration());
        System.out.println(currentAnimationTime);
    }

    public void startGame() {
        context.startActivity(new Intent(context, GameContent.class));
    }

    @Override
    public void restoreState(Bundle savedState) {

    }

    @Override
    public Bundle saveState() {
        return null;
    }

    @Override
    public void setSize(int width, int height) {
        scaleX = (float) width / movie.width();
        scaleY = (float) height / movie.height();
    }
}
