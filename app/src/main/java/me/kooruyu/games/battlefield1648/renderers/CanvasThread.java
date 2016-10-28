package me.kooruyu.games.battlefield1648.renderers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.SurfaceHolder;

/**
 * The main canvas thread that takes care of rendering the game
 * parallel to interaction with the CanvasView.
 */

public class CanvasThread extends Thread {

    //System references
    private Context context;
    private final SurfaceHolder surfaceHolder;

    private boolean isRunning;

    private int screenWidth = 1;
    private int screenHeight = 1;

    //Enables calculations at a fixed rate
    private long lastUpdate;
    private double accumulator;
    private static final double RENDER_STEP = 1000 / 60;

    //Game Control Booleans
    private boolean wasTouched;

    //Player input
    private int touchX;
    private int touchY;
    private int touchSize;

    //Paints
    private Paint touchFeedbackPaint;
    private Paint defaultTextPaint;

    //Frame Counter
    private int frames;
    private int frameUpdate;
    private long lastFPSUpdate;

    /**
     * Creates a new CanvasThread using the given context and SurfaceHolder
     *
     * @param context       the context of the View it's called by
     * @param surfaceHolder the surface holder containing the canvas to be drawn on and recieving callbacks
     */
    public CanvasThread(Context context, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.context = context;

        touchFeedbackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        touchFeedbackPaint.setColor(Color.RED);
        touchFeedbackPaint.setStyle(Paint.Style.FILL);

        defaultTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultTextPaint.setColor(Color.BLUE);
        defaultTextPaint.setTextSize(100);
    }

    /**
     * Starts the thread and starts rendering implicitly
     */
    @Override
    public void start() {
        setRenderState(true);
        super.start();
    }

    /**
     * Enables or disables updates and rendering
     *
     * @param enabled State to switch to
     */
    public void setRenderState(boolean enabled) {
        isRunning = enabled;

        if (enabled) init();
    }

    /**
     * Creates initial game state
     */
    private void init() {
        //reset frame counter
        lastUpdate = SystemClock.elapsedRealtime();
        frames = 0;
    }

    public void touchAt(int x, int y, int size) {
        touchX = x;
        touchY = y;
        touchSize = size;
        wasTouched = true;
    }

    public void setTouchFeedback(boolean enabled) {
        wasTouched = enabled;
    }

    /**
     * Contains the main game loop which controls updating of game objects
     * and drawing to the screen
     */
    @Override
    public void run() {
        while (isRunning) {
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();

                //TODO: this is necessary because nullpointer exceptions caused by draw() couldn't be handled and might be changed later
                if (canvas == null) continue;

                synchronized (surfaceHolder) {
                    update();
                    draw(canvas);
                }

            } finally {
                //if either an error occurs or the drawing is done release the canvas
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }


    /**
     * Updates the games content and logic
     */
    private void update() {
        long currentUpdate = SystemClock.elapsedRealtime();

        //Variable Time updates here

        //update frame counter every second
        if ((currentUpdate - lastFPSUpdate) >= 1000) {
            frameUpdate = frames;
            frames = 0;
            lastFPSUpdate = currentUpdate;
        }

        accumulator += currentUpdate - lastUpdate;
        lastUpdate = currentUpdate;

        //catches up or slows down depending on the frame rate
        while (accumulator >= RENDER_STEP) {
            fixedUpdate();
            accumulator -= RENDER_STEP;
        }
    }


    /**
     * Updates everything that should be calculated at a fixed rate
     */
    private void fixedUpdate() {

    }


    /**
     * Draws current state to screen
     *
     * @param canvas The canvas to be drawn on
     */
    private void draw(Canvas canvas) {
        //white background
        canvas.drawColor(Color.WHITE);

        //frame counter in the upper left corner
        canvas.drawText(Integer.toString(frameUpdate), 5, defaultTextPaint.getTextSize(), defaultTextPaint);

        if (wasTouched) {
            canvas.drawCircle(touchX, touchY, touchSize, touchFeedbackPaint);
        }

        //add drawn frame
        frames++;
    }

    /**
     * Changes the size of the canvas
     */
    public void setSize(int width, int height) {
        synchronized (surfaceHolder) {
            screenWidth = width;
            screenHeight = height;

            //As soon as we get assets there might be resizing that has to be done here
        }
    }
}