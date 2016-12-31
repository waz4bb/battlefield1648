package me.kooruyu.games.battlefield1648.views;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import me.kooruyu.games.battlefield1648.renderers.CanvasThread;


public class MainGameView extends SurfaceView implements SurfaceHolder.Callback {

    private Bundle restoredState;
    private SurfaceHolder surfaceHolder;
    private CanvasThread drawingThread;

    private View.OnTouchListener gestureListener;
    private int screenWidth = 1;
    private int screenHeight = 1;

    public MainGameView(Context context) {
        super(context);

        init();
    }

    public MainGameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public MainGameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        //add gestures
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new TouchListener());
        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new PinchListener());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event) || scaleGestureDetector.onTouchEvent(event);
            }
        };

        setOnTouchListener(gestureListener);

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
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        drawingThread.setRenderState(hasWindowFocus);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawingThread = new CanvasThread(getContext(), surfaceHolder);
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

    private class PinchListener implements ScaleGestureDetector.OnScaleGestureListener {

        private static final float SCALE_FACTOR = .05f;
        private static final float MAXIMUM_SCALE = 1f * SCALE_FACTOR;
        private static final float MINIMUM_SCALE = .25f * SCALE_FACTOR;

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float factor = scaleGestureDetector.getScaleFactor() * SCALE_FACTOR;

            if (factor < MINIMUM_SCALE && factor > MAXIMUM_SCALE) {
                return false;
            }

            if ((scaleGestureDetector.getCurrentSpan() - scaleGestureDetector.getPreviousSpan()) < 0) {
                factor *= -1;
            }

            drawingThread.scaleTo(factor);

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

        }
    }

    private class TouchListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            drawingThread.clickAt(motionEvent);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent init, MotionEvent current, float v, float v1) {
            if (current.getPointerCount() == 1) {
                drawingThread.moveTo((int) ((current.getX() - init.getX()) * .1), (int) ((current.getY() - init.getY()) * .1));
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {
            return false;
        }
    }
}