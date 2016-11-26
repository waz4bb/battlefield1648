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
import me.kooruyu.games.battlefield1648.drawables.Square;
import me.kooruyu.games.battlefield1648.drawables.layers.ItemDescription;
import me.kooruyu.games.battlefield1648.drawables.layers.TurnOverButton;
import me.kooruyu.games.battlefield1648.entities.Enemy;
import me.kooruyu.games.battlefield1648.entities.Player;
import me.kooruyu.games.battlefield1648.events.EventMap;
import me.kooruyu.games.battlefield1648.events.EventObserver;

/**
 * The main canvas thread that takes care of rendering the game
 * parallel to interaction with the MainGameView.
 */

public class CanvasThread extends AbstractCanvasThread {

    private final int mapSizeX = 44;
    private final int mapSizeY = 26;

    //Enables calculations at a fixed rate
    private long lastUpdate;
    private double accumulator;
    private static final double RENDER_STEP = 1000 / 60;

    //for storing restore data
    private boolean pathChanged;

    //Player input
    private boolean wasTouched;
    private boolean wasScaled;
    private boolean wasMoved;
    private MotionEvent touchEvent;
    private float lastScaleFactor;
    private int movedX, movedY;

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
    private Integer[][] nextPathCoords;
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
        enemies.add(new Enemy(10, 21, pathPaint));
        enemies.add(new Enemy(38, 10, pathPaint));
        enemies.add(new Enemy(9, 12, pathPaint));

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

    public void moveTo(int xOffset, int yOffset) {
        movedX = xOffset;
        movedY = yOffset;
        wasMoved = true;
    }

    public void scaleTo(float factor) {
        lastScaleFactor = factor;
        wasScaled = true;
    }

    public void clickAt(MotionEvent touchEvent) {
        this.touchEvent = touchEvent;
        wasTouched = true;
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

        if (wasMoved) {
            wasMoved = false;
            gridMap.moveTo(movedX, movedY);
            recalculateScreenPositions();
        }

        if (wasScaled) {
            wasScaled = false;
            if (playerAnimator == null || !playerAnimator.isRunning()) {
                float currentZoomFactor = gridMap.getZoomFactor() + lastScaleFactor;

                if (((currentZoomFactor - 1) <= 0.05)) currentZoomFactor = 1;

                if (currentZoomFactor < 4 && currentZoomFactor >= 1) {
                    gridMap.zoomTo(currentZoomFactor);

                    recalculateScreenPositions();
                }
            }
        }

        if (wasTouched) {
            wasTouched = false;
            itemDescription.setVisible(false, false);

            //Handles region specific events
            int x = (int) touchEvent.getX();
            int y = (int) touchEvent.getY();

            //handles gridMap events
            if (gridMap.getBounds().contains(x, y)) {

                Vertex v = gridMap.getVertex(x, y);
                if ((playerAnimator == null || !playerAnimator.isRunning())
                        && gridMap.isMovable(player.getX(), player.getY(), v.getX(), v.getY())) {

                    nextPath = gridMap.getPathTo(player.getX(), player.getY(), v.getX(), v.getY());

                    gridMap.clearStartingPosition(player.getPosition());
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

                        Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
                        enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));
                    }
                }
            }
        }

        if (pathChanged && nextPath != null) {

            pathChanged = false;

            playerAnimator = new Animator();
            nextPathCoords = getPathAnimation(nextPath, playerAnimator);
            playerAnimator.addListener(player);

            enemyPaths = new ArrayList<>(enemies.size());
            enemyAnimators = new ArrayList<>(enemies.size());
            for (Enemy enemy : enemies) {
                if (enemy.hasFieldOfView()) {
                    gridMap.getMapDrawable().drawSquareBackgrounds(enemy.getFieldOfView(), FOVpaint);
                }
                if (enemy.hasPath()) {
                    Animator currentAnimator = new Animator();
                    currentAnimator.addListener(enemy);
                    enemyAnimators.add(currentAnimator);
                    enemyPaths.add(getPathAnimation(enemy.getPath(), currentAnimator));
                }
            }
        }
    }

    private Integer[][] getPathAnimation(List<Vertex> path, Animator animator) {
        Integer[][] coords = new Integer[path.size()][2];
        List<AnimationScheduler> animationList = new ArrayList<>(path.size());

        int xBefore = 0;
        int yBefore = 0;

        for (int i = 0; i < path.size(); i++) {
            Square s = gridMap.getSquare(path.get(i).getX(), path.get(i).getY());

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

    private Integer[][] getPathRepresentation(List<Vertex> path) {
        Integer[][] coords = new Integer[path.size()][2];

        for (int i = 0; i < path.size(); i++) {
            Square s = gridMap.getSquare(path.get(i).getX(), path.get(i).getY());

            coords[i][0] = s.getMiddleX();
            coords[i][1] = s.getMiddleY();
        }

        return coords;
    }

    private void recalculateScreenPositions() {
        gridMap.redrawStartingPosition();
        if (nextPath != null) {
            nextPathCoords = getPathRepresentation(nextPath);

            enemyPaths = new ArrayList<>(enemies.size());
            for (Enemy enemy : enemies) {
                if (enemy.hasPath()) {
                    enemyPaths.add(getPathRepresentation(enemy.getPath()));
                }
            }
        }

        Square location = gridMap.getSquare(player.getX(), player.getY());
        player.setScreenLocation(new Vertex(location.getMiddleX(), location.getMiddleY()));

        for (Enemy enemy : enemies) {
            Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
            enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));
        }
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
        Square s = gridMap.getSquare(player.getX(), player.getY());
        player.setScreenLocation(new Vertex(s.getMiddleX(), s.getMiddleY()));

        //set enemy screen positions
        for (Enemy enemy : enemies) {
            Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
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