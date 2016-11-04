package me.kooruyu.games.battlefield1648.views;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import me.kooruyu.games.battlefield1648.renderers.CanvasThread;


public class CanvasView extends SurfaceView implements SurfaceHolder.Callback {

    private Bundle restoredState;
    private SurfaceHolder surfaceHolder;
    private CanvasThread drawingThread;

    private int screenWidth = 1;
    private int screenHeight = 1;

    public CanvasView(Context context) {
        super(context);

        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        //add callback to the surface holder
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        restoredState = null;

        setFocusable(true);
    }

    public void restoreState(Bundle previousState) {
        restoredState = previousState;
    }

    public Bundle saveState() {
        return drawingThread.saveState();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //disable touch feedback when the pointers aren't on the screen anymore
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP) {
            drawingThread.setTouchFeedback(false);
            return true;
        }

        int pointerCount = event.getPointerCount();
        float[][] pointers = new float[pointerCount][3];

        //store pointer coordinates and strength
        for (int i = 0; i < pointerCount; i++) {
            pointers[i][0] = event.getX(i);
            pointers[i][1] = event.getY(i);
            pointers[i][2] = ((event.getSize(i) * Math.min(screenHeight, screenWidth)) / 5);
        }

        //queue drawing of touch feedback
        drawingThread.touchAt(event, pointers);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        drawingThread.setRenderState(hasWindowFocus);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawingThread = new CanvasThread(super.getContext(), surfaceHolder);
        if (restoredState != null) {
            drawingThread.restoreState(restoredState);
        }
        drawingThread.setRenderState(true);
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
                e.printStackTrace();
            }
        }
    }
}