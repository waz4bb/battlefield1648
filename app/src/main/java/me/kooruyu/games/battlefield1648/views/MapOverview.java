package me.kooruyu.games.battlefield1648.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import me.kooruyu.games.battlefield1648.GameContent;
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

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (drawingThread.isButtomClick(motionEvent)) {
                        getContext().startActivity(new Intent(getContext(), GameContent.class));
                        return true;
                    }
                }
                return false;
            }
        });

        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawingThread = new MapThread(getContext(), surfaceHolder);
        drawingThread.start();

        drawingThread.setRenderState(true);
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
