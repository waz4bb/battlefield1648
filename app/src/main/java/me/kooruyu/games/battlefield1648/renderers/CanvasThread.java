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
import java.util.ArrayList;
import java.util.List;

import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.animations.AnimationScheduler;
import me.kooruyu.games.battlefield1648.animations.Animator;
import me.kooruyu.games.battlefield1648.animations.VertexAnimator;
import me.kooruyu.games.battlefield1648.cartography.GridMap;
import me.kooruyu.games.battlefield1648.entities.Enemy;
import me.kooruyu.games.battlefield1648.entities.Player;
import me.kooruyu.games.battlefield1648.events.EventMap;
import me.kooruyu.games.battlefield1648.events.EventObserver;
import me.kooruyu.games.battlefield1648.layers.ItemDescription;
import me.kooruyu.games.battlefield1648.layers.TurnOverButton;

/**
 * The main canvas thread that takes care of rendering the game
 * parallel to interaction with the CanvasView.
 */

public class CanvasThread extends AbstractCanvasThread {

    private final int mapSizeX = 44;
    private final int mapSizeY = 26;

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
    private Paint FOVpaint;

    //Frame Counter
    private int frames;
    private int frameUpdate;
    private long lastFPSUpdate;

    //Path
    private ArrayList<Vertex> nextPath;
    private int[][] nextPathCoords;
    private final int MAX_MOVEMENT_LENGTH = 5;
    private final int MOVEMENT_ANIMATION_LENGTH = 10; //in frames per step
    //for enemy paths
    private List<Integer[][]> enemyPaths;

    //Layers
    private GridMap gridMap;
    private ItemDescription itemDescription;
    private TurnOverButton turnOverButton;

    //Entities
    private final int STARTING_X = 2;
    private final int STARTING_Y = 2;
    private Player player;
    private List<Enemy> enemies;

    //Animators
    private Animator playerAnimator;
    private List<Animator> enemyAnimators;

    //Events
    private EventMap events;

    /**
     * Creates a new CanvasThread using the given context and SurfaceHolder
     *
     * @param context       the context of the View it's called by
     * @param surfaceHolder the surface holder containing the canvas to be drawn on and receiving callbacks
     */
    public CanvasThread(Context context, SurfaceHolder surfaceHolder) {
        super(context, surfaceHolder);

        touchFeedbackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        touchFeedbackPaint.setColor(Color.BLACK);
        touchFeedbackPaint.setStyle(Paint.Style.FILL);

        defaultTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultTextPaint.setColor(Color.BLACK);
        defaultTextPaint.setTextSize(100);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(Color.RED);
        pathPaint.setStrokeWidth(8);

        FOVpaint = new Paint();
        FOVpaint.setColor(Color.LTGRAY);

        player = new Player(STARTING_X, STARTING_Y, pathPaint);
        playerAnimator = null;

        nextPathCoords = null;
        nextPath = null;

        pathChanged = false;
        enemyPaths = null;

        enemies = new ArrayList<>();
        //TODO: Debug enemy
        enemies.add(new Enemy(STARTING_X * 8, STARTING_Y + 1, pathPaint));
        enemies.add(new Enemy(STARTING_X * 3, STARTING_Y * 4, pathPaint));
        enemies.add(new Enemy(STARTING_X * 4, STARTING_Y * 8, pathPaint));

        enemyAnimators = new ArrayList<>(enemies.size());
    }

    /**
     * Creates initial game state
     */
    @Override
    void init() {
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
     * Updates the games content and logic
     */
    @Override
    void update() {
        long currentUpdate = SystemClock.elapsedRealtime();

        //Variable Time updates here

        //update frame counter every second
        if ((currentUpdate - lastFPSUpdate) >= 1000) {
            frameUpdate = frames;
            frames = 0;
            lastFPSUpdate = currentUpdate;
        }

        //catches up or slows down depending on the frame rate
        accumulator += currentUpdate - lastUpdate;
        lastUpdate = currentUpdate;

        while (accumulator >= RENDER_STEP) {
            fixedUpdate();
            accumulator -= RENDER_STEP;
        }
    }


    /**
     * Updates everything that should be calculated at a fixed rate
     */
    private void fixedUpdate() {
        if (playerAnimator != null && playerAnimator.isRunning()) {
            playerAnimator.dispatchUpdate();
            //if the animation just ended draw movement indicator and check for events
            if (!playerAnimator.isRunning()) {
                gridMap.setPlayerDestination(player.getPosition());
            }
        }

        for (Animator animator : enemyAnimators) {
            animator.dispatchUpdate();
        }

        if (wasTouched && touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
            itemDescription.setVisible(false, false);

            //Handles region specific events
            int x = (int) touchEvent.getX();
            int y = (int) touchEvent.getY();

            //handles gridMap events
            if (gridMap.getBounds().contains(x, y)) {

                Vertex v = gridMap.touchSquareAt(x, y);
                if ((playerAnimator == null || !playerAnimator.isRunning())
                        && gridMap.isMovable(player.getX(), player.getY(), v.getX(), v.getY())) {

                    nextPath = gridMap.getPathTo(player.getX(), player.getY(), v.getX(), v.getY());

                    gridMap.clearStartingPosition(player.getX(), player.getY());
                    player.moveTo(v.getX(), v.getY());

                    pathChanged = true;
                    turnOverButton.addTurn();

                    for (Enemy enemy : enemies) {
                        ArrayList<Vertex> path = gridMap.getPathTo(enemy.getX(), enemy.getY(), player.getX(), player.getY());

                        if (path == null) continue;

                        List<Vertex> snippet = new ArrayList<>();
                        for (int i = 0; i < path.size() && i < MAX_MOVEMENT_LENGTH; i++) {
                            snippet.add(path.get(i));
                        }
                        enemy.setPath(snippet);

                        gridMap.setBlocked(enemy.getPosition(), false);
                        enemy.moveTo(snippet.get(snippet.size() - 1));
                        gridMap.setBlocked(enemy.getPosition(), true);

                        GridMap.Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
                        enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));
                    }
                }
            }
        }

        if (pathChanged && nextPath != null) {

            pathChanged = false;
            nextPathCoords = new int[nextPath.size()][2];

            int xBefore, yBefore;
            xBefore = yBefore = 0;

            List<AnimationScheduler> pathAnimations = new ArrayList<>(nextPath.size());

            for (int i = 0; i < nextPath.size(); i++) {
                GridMap.Square s = gridMap.getSquare(nextPath.get(i).getX(), nextPath.get(i).getY());

                if (i > 0) {
                    pathAnimations.add(new AnimationScheduler(
                            new VertexAnimator(),
                            new Vertex(s.getMiddleX(), s.getMiddleY()),
                            new Vertex(xBefore, yBefore),
                            MOVEMENT_ANIMATION_LENGTH
                    ));
                }
                xBefore = s.getMiddleX();
                yBefore = s.getMiddleY();

                nextPathCoords[i][0] = xBefore;
                nextPathCoords[i][1] = yBefore;
            }

            playerAnimator = new Animator();
            playerAnimator.addAnimatorSequence(pathAnimations);
            playerAnimator.addListener(player);

            enemyPaths = new ArrayList<>(enemies.size());
            enemyAnimators = new ArrayList<>(enemies.size());
            for (Enemy enemy : enemies) {
                if (enemy.hasFieldOfView()) {
                    gridMap.drawSquareBackgrounds(enemy.getFieldOfView(), FOVpaint);
                }
                if (enemy.hasPath()) {
                    Animator currentAnimator = new Animator();
                    currentAnimator.addListener(enemy);
                    enemyAnimators.add(currentAnimator);
                    enemyPaths.add(getPathRepresentation(enemy.getPath(), currentAnimator));
                }
            }
        }
    }

    private Integer[][] getPathRepresentation(List<Vertex> path, Animator animator) {
        Integer[][] coords = new Integer[path.size()][2];
        List<AnimationScheduler> animationList = new ArrayList<>(path.size());

        int xBefore = 0;
        int yBefore = 0;

        for (int i = 0; i < path.size(); i++) {
            GridMap.Square s = gridMap.getSquare(path.get(i).getX(), path.get(i).getY());

            if (i > 0) {
                animationList.add(new AnimationScheduler(
                        new VertexAnimator(),
                        new Vertex(s.getMiddleX(), s.getMiddleY()),
                        new Vertex(xBefore, yBefore),
                        MOVEMENT_ANIMATION_LENGTH
                ));
            }

            coords[i][0] = xBefore = s.getMiddleX();
            coords[i][1] = yBefore = s.getMiddleY();
        }

        animator.addAnimatorSequence(animationList);

        return coords;
    }

    /**
     * Draws current state to screen
     *
     * @param canvas The canvas to be drawn on
     */
    @Override
    void draw(Canvas canvas) {
        //white background
        canvas.drawColor(Color.WHITE);

        gridMap.draw(canvas);

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

            for (Integer[][] graphicalPath : enemyPaths) {
                for (int i = 0; i < graphicalPath.length - 1; i++) {
                    canvas.drawLine(
                            graphicalPath[i][0],
                            graphicalPath[i][1],
                            graphicalPath[i + 1][0],
                            graphicalPath[i + 1][1],
                            pathPaint
                    );

                    canvas.drawCircle(graphicalPath[i][0], graphicalPath[i][1], 15, touchFeedbackPaint);
                }
                canvas.drawCircle(graphicalPath[graphicalPath.length - 1][0], graphicalPath[graphicalPath.length - 1][1], 15, touchFeedbackPaint);
            }

            if (playerAnimator.isRunning()) {
                canvas.drawCircle(
                        nextPathCoords[nextPathCoords.length - 1][0],
                        nextPathCoords[nextPathCoords.length - 1][1],
                        15, touchFeedbackPaint
                );
            }
        }

        //draw entities
        player.draw(canvas);
        //draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(canvas);
        }

        //draw layers/buttons
        turnOverButton.draw(canvas);
        itemDescription.draw(canvas);

        if (wasTouched) {
            //clones the pointers array in case the pointers change while drawing
            float[][] tmp = pointers.clone();

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

        //frame counter in the upper left corner
        canvas.drawText(Integer.toString(frameUpdate), 5, defaultTextPaint.getTextSize(), defaultTextPaint);

        //add drawn frame
        frames++;
    }

    /**
     * Helper method which initializes the map and entities on it
     *
     * @param width  The width of the screen
     * @param height The height of the screen
     */
    private void createMap(int width, int height) {

        //TODO: debugging events
        events = new EventMap();
        //put a new event at position 15,10
        events.put(new Vertex(15, 11), new EventObserver(itemDescription));
        //set all to disabled as a start state
        events.disableAll();

        gridMap = new GridMap(mapSizeX, mapSizeY, width, height, MAX_MOVEMENT_LENGTH, events);

        //create initial movement overlay
        gridMap.highlightSquares(player.getPosition(), MAX_MOVEMENT_LENGTH);

        //set initial player screen position
        GridMap.Square s = gridMap.getSquare(player.getX(), player.getY());
        player.setScreenLocation(new Vertex(s.getMiddleX(), s.getMiddleY()));

        //set enemy screen positions
        for (Enemy enemy : enemies) {
            GridMap.Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
            enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));
        }

        //block initial entity locations
        for (Enemy enemy : enemies) {
            gridMap.setBlocked(enemy.getPosition(), true);
        }

        //add temporary marker on map to highlight debug event location
        Paint customMarker = new Paint();
        customMarker.setColor(Color.RED);
        gridMap.getSquare(15, 11).setPaint(customMarker);
    }

    /**
     * Restore relevant saved variables here
     * @param savedState the state to restore
     */
    @Override
    public synchronized void restoreState(Bundle savedState) {
        synchronized (surfaceHolder) {
            player = new Player(savedState.getInt("mPlayerX"), savedState.getInt("mPlayerY"), pathPaint);
            Serializable path = savedState.getSerializable("mPath");
            nextPath = (path == null) ? null : (ArrayList<Vertex>) path;
            pathChanged = true;
        }
    }


    /**
     * Save relevant variables here
     *
     * @return a bundle representing the current state
     */
    @Override
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
    @Override
    public void setSize(int width, int height) {
        synchronized (surfaceHolder) {

            //create layers to fit the new screen size
            itemDescription = new ItemDescription(
                    "Test Item", "This is a item for testing purposes.",
                    width * .1f, height * .1f, width * .9f, height * .9f
            );

            Paint buttonPaint = new Paint();
            buttonPaint.setColor(Color.GREEN);

            turnOverButton = new TurnOverButton(width - 450, 50, 400, 200, buttonPaint);

            //initialize map
            createMap(width, height);
        }
    }
}