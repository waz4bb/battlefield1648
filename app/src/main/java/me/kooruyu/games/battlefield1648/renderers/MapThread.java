package me.kooruyu.games.battlefield1648.renderers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.SurfaceHolder;

import me.kooruyu.games.battlefield1648.R;


public class MapThread extends AbstractCanvasThread {

    private Drawable map;

    /**
     * Creates a new CanvasThread using the given context and SurfaceHolder
     *
     * @param context       the context of the View it's called by
     * @param surfaceHolder the surface holder containing the canvas to be drawn on and receiving callbacks
     */
    public MapThread(Context context, SurfaceHolder surfaceHolder) {
        super(context, surfaceHolder);

        map = context.getResources().getDrawable(R.drawable.main_map, null);
    }

    @Override
    void init() {

    }

    @Override
    void update() {

    }

    @Override
    void draw(Canvas canvas) {
        map.draw(canvas);
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
        map.setBounds(0, 0, width, height);
    }
}
