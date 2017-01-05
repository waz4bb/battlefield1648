package me.kooruyu.games.battlefield1648.renderers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import me.kooruyu.games.battlefield1648.R;
import me.kooruyu.games.battlefield1648.drawables.layers.TurnOverButton;


public class PrologueThread extends AbstractCanvasThread {

    private Bitmap map;
    private TurnOverButton testButton;
    private Paint buttonPaint;

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

    public boolean isButtonClick(MotionEvent event) {
        return testButton.contains((int) event.getX(), (int) event.getY());
    }

    @Override
    void init() {
        buttonPaint = new Paint();
        buttonPaint.setColor(Color.rgb(94, 235, 171));
        map = BitmapFactory.decodeResource(context.getResources(), R.mipmap.main_map);
        testButton = null;
    }

    @Override
    void update() {

    }

    @Override
    void draw(Canvas canvas) {
        canvas.drawBitmap(map, 0, 0, null);
        if (testButton != null) testButton.draw(canvas);
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
        map = Bitmap.createScaledBitmap(map, width, height, true);
        testButton = new TurnOverButton(width / 8, height / 8, width / 4, height / 4, buttonPaint);
    }
}
