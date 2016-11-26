package me.kooruyu.games.battlefield1648.renderers;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.SurfaceHolder;

public abstract class AbstractCanvasThread extends Thread {

    //System references
    final Context context;
    final SurfaceHolder surfaceHolder; //should be directly accessible by a renderer extending this class

    private boolean isRunning;

    /**
     * Creates a new CanvasThread using the given context and SurfaceHolder
     *
     * @param context       the context of the View it's called by
     * @param surfaceHolder the surface holder containing the canvas to be drawn on and receiving callbacks
     */
    public AbstractCanvasThread(Context context, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.context = context;
    }

    /**
     * Creates initial game state
     */
    abstract void init();

    /**
     * Updates the games content and logic
     */
    abstract void update();

    /**
     * Draws current state to screen
     *
     * @param canvas The canvas to be drawn on
     */
    abstract void draw(Canvas canvas);

    /**
     * Contains the main game loop which controls updating of game objects
     * and drawing to the screen
     */
    @Override
    public void run() {
        while (isRunning) {
            Canvas canvas = null;
            try {
                //update before locking the canvas to avoid locking the view
                update();

                canvas = surfaceHolder.lockCanvas();

                //TODO: this is necessary because nullpointer exceptions caused by draw() couldn't be handled and might be changed later
                if (canvas == null) continue;

                synchronized (surfaceHolder) {
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
     * Enables or disables updates and rendering
     *
     * @param enabled State to switch to
     */
    public void setRenderState(boolean enabled) {
        isRunning = enabled;

        if (enabled) init();
    }

    /**
     * Restore relevant saved variables here
     *
     * @param savedState the state to restore
     */
    public abstract void restoreState(Bundle savedState);

    /**
     * Save relevant variables here
     *
     * @return a bundle representing the current state
     */
    public abstract Bundle saveState();

    /**
     * Changes the size of the canvas
     */
    public abstract void setSize(int width, int height);
}
