package me.kooruyu.games.battlefield1648.renderers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.Serializable;
import java.util.LinkedList;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.entities.Player;
import me.kooruyu.games.battlefield1648.layers.GridMap;

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

    private final int mapSizeX = 44; //prev: 22
    private final int mapSizeY = 28; //prev: 14

    //Enables calculations at a fixed rate
    private long lastUpdate;
    private double accumulator;
    private static final double RENDER_STEP = 1000 / 60;

    //Game Control Booleans
    private boolean wasTouched;


    //for storing restore data
    private boolean pathChanged;

    //Player input
    private float[][] pointers;
    private MotionEvent touchEvent;

    //Paints
    private Paint touchFeedbackPaint;
    private Paint defaultTextPaint;
    private Paint pathPaint;

    //Frame Counter
    private int frames;
    private int frameUpdate;
    private long lastFPSUpdate;

    //Path
    private LinkedList<Vertex> nextPath;
    private int[][] nextPathCoords;

    //Layers
    private GridMap gridMap;

    //Entities
    private Player player;
    private final int STARTING_X = 2;
    private final int STARTING_Y = 2;

    /**
     * Creates a new CanvasThread using the given context and SurfaceHolder
     *
     * @param context       the context of the View it's called by
     * @param surfaceHolder the surface holder containing the canvas to be drawn on and receiving callbacks
     */
    public CanvasThread(Context context, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.context = context;

        touchFeedbackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        touchFeedbackPaint.setColor(Color.BLACK);
        touchFeedbackPaint.setStyle(Paint.Style.FILL);

        defaultTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultTextPaint.setColor(Color.BLACK);
        defaultTextPaint.setTextSize(100);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(Color.RED);
        pathPaint.setStrokeWidth(8);

        player = new Player(STARTING_X, STARTING_Y);

        nextPathCoords = null;
        nextPath = null;

        pathChanged = false;
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
        //reset frame counter and elapsed time
        lastUpdate = SystemClock.elapsedRealtime();
        frames = 0;
    }

    public void touchAt(MotionEvent touchEvent, float[][] pointers) {
        this.touchEvent = touchEvent;
        this.pointers = pointers;
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
        if (pathChanged && nextPath != null) {
            pathChanged = false;
            nextPathCoords = new int[nextPath.size()][2];
            GridMap.Square s;

            int i = 0;
            for (Vertex v : nextPath) {
                s = gridMap.getSquare(v.getX(), v.getY());

                nextPathCoords[i][0] = s.getMiddleX();
                nextPathCoords[i][1] = s.getMiddleY();

                i++;
            }
        }
    }

    /**
     * Draws current state to screen
     *
     * @param canvas The canvas to be drawn on
     */
    private void draw(Canvas canvas) {
        //white background
        canvas.drawColor(Color.WHITE);

        gridMap.draw(canvas);

        //frame counter in the upper left corner
        canvas.drawText(Integer.toString(frameUpdate), 5, defaultTextPaint.getTextSize(), defaultTextPaint);

        if (nextPathCoords != null) {
            for (int i = 0; i < nextPathCoords.length - 1; i++) {
                canvas.drawLine(
                        nextPathCoords[i][0],
                        nextPathCoords[i][1],
                        nextPathCoords[i + 1][0],
                        nextPathCoords[i + 1][1],
                        pathPaint
                );

                canvas.drawCircle(nextPathCoords[i][0], nextPathCoords[i][1], 15, touchFeedbackPaint);
            }
        }

        GridMap.Square playerPosition = gridMap.getSquare(player.getX(), player.getY());
        canvas.drawCircle(playerPosition.getMiddleX(), playerPosition.getMiddleY(), 20, pathPaint);

        if (wasTouched) {
            //clones the pointers array in case the pointers change while drawing
            float[][] tmp = pointers.clone();

            //TODO: move path calculation to update
            if (touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
                int x = (int) touchEvent.getX();
                int y = (int) touchEvent.getY();
                if (gridMap.getBounds().contains(x, y)) {
                    Vertex v = gridMap.touchSquareAt(x, y);
                    nextPath = gridMap.getPathTo(v.getX(), v.getY());
                    player.moveTo(v.getX(), v.getY());
                    gridMap.setStartingPosition(v.getX(), v.getY());
                    pathChanged = true;
                }
                //itemDescription.setVisible(!itemDescription.isVisible(),false);
            }

            for (int i = 0; i < tmp.length; i++) {
                canvas.drawCircle(
                        tmp[i][0],
                        tmp[i][1],
                        tmp[i][2],
                        touchFeedbackPaint
                );

                //connect the current pointer with the next pointer unless it's the last pointer
                if (i != tmp.length - 1) {
                    canvas.drawLine(
                            tmp[i][0], tmp[i][1],
                            tmp[i + 1][0], tmp[i + 1][1],
                            touchFeedbackPaint
                    );
                }
                //if there is more than one pointer connect the last pointer with the first pointer
                else if (tmp.length > 1) {
                    canvas.drawLine(
                            tmp[i][0], tmp[i][1],
                            tmp[0][0], tmp[0][1],
                            touchFeedbackPaint
                    );
                }
            }
        }

        //add drawn frame
        frames++;
    }


    /**
     * Restore relevant saved variables here
     * @param savedState the state to restore
     */
    public synchronized void restoreState(Bundle savedState) {
        synchronized (surfaceHolder) {
            player = new Player(savedState.getInt("mPlayerX"), savedState.getInt("mPlayerY"));
            Serializable path = savedState.getSerializable("mPath");
            nextPath = (path == null) ? null : (LinkedList<Vertex>) path;
            pathChanged = true;
        }
    }


    /**
     * Save relevant variables here
     *
     * @return a bundle representing the current state
     */
    public Bundle saveState() {
        Bundle map = new Bundle();

        synchronized (surfaceHolder) {
            map.putSerializable("mPath", nextPath);
            map.putInt("mPlayerX", player.getX());
            map.putInt("mPlayerY", player.getY());
        }

        return map;
    }


    /**
     * Changes the size of the canvas
     */
    public void setSize(int width, int height) {
        synchronized (surfaceHolder) {
            screenWidth = width;
            screenHeight = height;

            //As soon as we get assets there might be resizing that has to be done here
            gridMap = new GridMap(mapSizeX, mapSizeY, width, height);
        }
        gridMap.setStartingPosition(player.getX(), player.getY());
    }
}