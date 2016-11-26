package me.kooruyu.games.battlefield1648.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import me.kooruyu.games.battlefield1648.renderers.MapThread;


public class MapOverview extends SurfaceView implements SurfaceHolder.Callback {

    private MapThread drawingThread;
    private SurfaceHolder surfaceHolder;

    public MapOverview(Context context) {
        super(context);

        init();
    }

    public MapOverview(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public MapOverview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        //add callback to the surface holder
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawingThread = new MapThread(getContext(), surfaceHolder);
        drawingThread.setRenderState(true);
        drawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        drawingThread.setSize(width, height);
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
