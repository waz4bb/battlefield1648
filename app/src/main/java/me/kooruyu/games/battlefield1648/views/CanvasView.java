package me.kooruyu.games.battlefield1648.views;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import me.kooruyu.games.battlefield1648.renderers.CanvasThread;


public class CanvasView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    private CanvasThread drawingThread;

    private int screenWidth = 1;
    private int screenHeight = 1;

    public CanvasView(Context context) {
        super(context);

        //add callback to the surface holder
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        //create a seperate thread for drawing
        drawingThread = new CanvasThread(context, surfaceHolder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //disable touch feedback when the pointers aren't on the screen anymore
        if (event.getAction() == MotionEvent.ACTION_UP) {
            drawingThread.setTouchFeedback(false);
            return true;
        }

        //queue drawing of touch feedback at the (first) pointers coordinates with the transformed event size
        drawingThread.touchAt((int) event.getX(), (int) event.getY(), (int) ((event.getSize() * Math.min(screenHeight, screenWidth)) / 5));
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        drawingThread.setSize(width, height);
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        drawingThread.setRenderState(false);

        while (retry) {
            try {
                drawingThread.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }
}
