package me.kooruyu.games.battlefield1648.renderers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import me.kooruyu.games.battlefield1648.R;
import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.animations.AnimationScheduler;
import me.kooruyu.games.battlefield1648.animations.SequentialAnimator;
import me.kooruyu.games.battlefield1648.animations.SequentialListAnimator;
import me.kooruyu.games.battlefield1648.animations.VertexAnimator;
import me.kooruyu.games.battlefield1648.cartography.Direction;
import me.kooruyu.games.battlefield1648.cartography.GridMap;
import me.kooruyu.games.battlefield1648.cartography.MapReader;
import me.kooruyu.games.battlefield1648.drawables.Square;
import me.kooruyu.games.battlefield1648.drawables.layers.ItemDescription;
import me.kooruyu.games.battlefield1648.drawables.layers.TextButton;
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

    private static int NO_MODE = 0;
    private static int MOVE_MODE = 1;
    private static int SHOOT_MODE = 2;

    //Mode
    private int mode;

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
    private Paint deathPaint;
    private Paint shootArchPaint;
    private Paint squareHlPaint;

    //Frame Counter
    private int frames;
    private int frameUpdate;
    private long lastFPSUpdate;

    //Path
    private ArrayList<Vertex> nextPath;
    private Integer[][] nextPathCoords;
    private final int MAX_MOVEMENT_LENGTH = 4;
    private final int STANDARD_ENEMY_FOV_LENGTH = MAX_MOVEMENT_LENGTH * 2;
    //in frames per step
    private final int MOVEMENT_ANIMATION_LENGTH = 10;
    private final int CASCADING_ANIMATION_LENGTH = 4;

    //for enemy paths
    private List<Integer[][]> enemyPaths;

    //Layers
    private GridMap gridMap;
    private ItemDescription itemDescription;
    //Buttons
    private TurnOverButton turnOverButton;
    private TextButton moveButton;
    private TextButton shootButton;

    //Entities
    private final int STARTING_X = 2;
    private final int STARTING_Y = 2;
    private Player player;
    private List<Enemy> enemies;

    //Animators
    private SequentialAnimator playerAnimator;
    private List<SequentialAnimator> enemyAnimators;
    private SequentialListAnimator squareCascadeAnimator;

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

        deathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        deathPaint.setColor(Color.DKGRAY);

        shootArchPaint = new Paint();
        shootArchPaint.setColor(Color.YELLOW);

        squareHlPaint = new Paint();
        squareHlPaint.setColor(Color.argb(180, 3, 192, 60));

        FOVpaint = new Paint();
        FOVpaint.setColor(Color.LTGRAY);

        player = new Player(STARTING_X, STARTING_Y, pathPaint);
        playerAnimator = null;
        squareCascadeAnimator = null;

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

        moveButton = null;
        shootButton = null;

        mode = MOVE_MODE;
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
                placeActionButtons(player.getScreenLocation());
                gridMap.setPlayerDestination(player.getPosition());
            }
        }

        if (squareCascadeAnimator != null && squareCascadeAnimator.isRunning()) {
            squareCascadeAnimator.dispatchUpdate();
        }

        for (SequentialAnimator animator : enemyAnimators) {
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
                    placeActionButtons(player.getScreenLocation());
                }
            }
        }

        if (wasTouched) {
            fixedOnTouchEvent();
            wasTouched = false;
        }

        if (pathChanged && nextPath != null) {

            pathChanged = false;

            //Remove old buttons
            moveButton = null;
            shootButton = null;

            playerAnimator = new SequentialAnimator();
            nextPathCoords = getPathAnimation(nextPath, playerAnimator);
            playerAnimator.addListener(player);

            enemyPaths = new ArrayList<>(enemies.size());
            enemyAnimators = new ArrayList<>(enemies.size());
            for (Enemy enemy : enemies) {
                if (enemy.hasPath()) {
                    SequentialAnimator currentAnimator = new SequentialAnimator();
                    currentAnimator.addListener(enemy);
                    enemyAnimators.add(currentAnimator);
                    enemyPaths.add(getPathAnimation(enemy.getPathSnippet(), currentAnimator));
                }
            }
        }
    }


    private void fixedOnTouchEvent() {
        itemDescription.setVisible(false, false);

        //Handles region specific events
        int x = (int) touchEvent.getX();
        int y = (int) touchEvent.getY();

        //handles gridMap events
        if (gridMap.getBounds().contains(x, y)) {

            Vertex touchedPosition = gridMap.getVertex(x, y);

            if ((playerAnimator == null || !playerAnimator.isRunning())) {
                if (mode != MOVE_MODE && moveButton.contains(x, y)) {
                    if (mode == SHOOT_MODE) {
                        gridMap.getMapDrawable().clearSquareBackgrounds(player.getShootArch());
                        redrawEnemyFOVs();
                    }
                    player.setMovablePositions(gridMap.getPathCaster().castAllPaths(player.getPosition(), MAX_MOVEMENT_LENGTH));

                    squareCascadeAnimator = gridMap.getSquareCascadingAnimation(gridMap.getPathCaster().castPathsByLevel(player.getPosition(), MAX_MOVEMENT_LENGTH), CASCADING_ANIMATION_LENGTH, squareHlPaint);
                    mode = MOVE_MODE;


                } else if (mode != SHOOT_MODE && shootButton.contains(x, y)) {
                    if (mode == MOVE_MODE) {
                        gridMap.clearStartingPosition(player.getPosition());
                        gridMap.getMapDrawable().clearSquareBackgrounds(player.getMovablePositions());
                        redrawEnemyFOVs();
                    }
                    player.setShootArch(gridMap.castFOVShadow(player.getPosition(), MAX_MOVEMENT_LENGTH, Direction.ALL));

                    squareCascadeAnimator = gridMap.getSquareCascadingAnimation(gridMap.getShadowCaster().castShadowLevels(player.getX(), player.getY(), MAX_MOVEMENT_LENGTH, Direction.ALL), CASCADING_ANIMATION_LENGTH, shootArchPaint);

                    mode = SHOOT_MODE;


                } else if (mode == MOVE_MODE && (squareCascadeAnimator == null || !squareCascadeAnimator.isRunning()) && player.canMoveTo(touchedPosition.getX(), touchedPosition.getY())) {
                    mode = NO_MODE;

                    nextPath = gridMap.getPathTo(player.getX(), player.getY(), touchedPosition.getX(), touchedPosition.getY());
                    gridMap.clearStartingPosition(player.getPosition());
                    gridMap.getMapDrawable().clearSquareBackgrounds(player.getMovablePositions());


                    player.moveTo(touchedPosition.getX(), touchedPosition.getY());
                    pathChanged = true;

                    //process turn end
                    turnOverButton.addTurn();
                    updateEnemies();


                } else if (mode == SHOOT_MODE) {
                    if (!squareCascadeAnimator.isRunning() && player.getShootArch().contains(touchedPosition)) {
                        for (Enemy enemy : enemies) {
                            if (!enemy.isDead()
                                    && enemy.getX() == touchedPosition.getX()
                                    && enemy.getY() == touchedPosition.getY()) {

                                enemy.kill();
                                enemy.setPaint(deathPaint);
                                gridMap.getMapDrawable().clearSquareBackgrounds(player.getShootArch());
                                updateEnemies();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    private void redrawEnemyFOVs() {
        Set<Vertex> FOVs = new HashSet<>();

        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                FOVs.addAll(enemy.getFieldOfView());
            }
        }

        gridMap.getMapDrawable().drawSquareBackgrounds(FOVs, FOVpaint);
    }


    private void updateEnemies() {
        Set<Vertex> FOVs = new HashSet<>();
        Random rand = new Random(SystemClock.elapsedRealtime());

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                if (enemy.hasFieldOfView()) {
                    gridMap.getMapDrawable().clearSquareBackgrounds(enemy.getFieldOfView());
                    enemy.setFieldOfView(null);
                    enemy.setPath(null);
                }
                continue;
            }

            if (enemy.getStatus() == Enemy.IDLE && !enemy.getFieldOfView().contains(player.getPosition())) {

                if (enemy.hasTraversed()) {
                    Set<Vertex> possiblePaths = gridMap.getPathCaster().castMaximumPaths(enemy.getPosition(), MAX_MOVEMENT_LENGTH * 4);
                    int randomIndex = rand.nextInt(possiblePaths.size());

                    int i = 0;
                    Vertex target = null;

                    for (Vertex v : possiblePaths) {
                        if (randomIndex == i) {
                            target = v;
                            break;
                        }
                        i++;
                    }

                    enemy.setPath(gridMap.getPathTo(enemy.getX(), enemy.getY(), target.getX(), target.getY()));
                    relocateEnemy(enemy, enemy.getPath(), 0);
                    enemy.increasePathIndexBy(MAX_MOVEMENT_LENGTH);
                } else {
                    relocateEnemy(enemy, enemy.getPath(), enemy.getLastPathIndex());
                    enemy.increasePathIndexBy(MAX_MOVEMENT_LENGTH);
                }

                FOVs.addAll(enemy.getFieldOfView());
                continue;
            }

            enemy.setStatus(Enemy.SEARCHING);
            ArrayList<Vertex> path = gridMap.getPathTo(enemy.getX(), enemy.getY(), player.getX(), player.getY());

            if (path == null) {
                FOVs.addAll(enemy.getFieldOfView());
                continue;
            }

            relocateEnemy(enemy, path, 0);
            FOVs.addAll(enemy.getFieldOfView());


            Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
            enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));
        }

        gridMap.getMapDrawable().drawSquareBackgrounds(FOVs, FOVpaint);
    }


    private void relocateEnemy(Enemy enemy, List<Vertex> path, int startingIndex) {
        List<Vertex> snippet = new ArrayList<>();
        for (int i = startingIndex; i < path.size() && i < (MAX_MOVEMENT_LENGTH + startingIndex); i++) {
            snippet.add(path.get(i));
        }
        enemy.setPathSnippet(snippet);

        gridMap.setBlocked(enemy.getPosition(), false);
        enemy.moveTo(snippet.get(snippet.size() - 1));
        gridMap.setBlocked(enemy.getPosition(), true);


        gridMap.getMapDrawable().clearSquareBackgrounds(enemy.getFieldOfView());
        enemy.setFieldOfView(
                gridMap.castFOVShadow(enemy.getPosition(), STANDARD_ENEMY_FOV_LENGTH, Direction.getDirection(
                        enemy.getPreviousX(), enemy.getPreviousY(), enemy.getX(), enemy.getY()
                        )
                )
        );
    }


    private void placeActionButtons(Vertex position) {
        int squareWidth = gridMap.getMapDrawable().getSquareWidht();
        moveButton = new TextButton(position.getX() - 150, position.getY() + squareWidth, 145, 80, "move", FOVpaint);
        shootButton = new TextButton(position.getX() + 5, position.getY() + squareWidth, 145, 80, "shoot", FOVpaint);
    }


    private Integer[][] getPathAnimation(List<Vertex> path, SequentialAnimator animator) {
        Integer[][] coords = new Integer[path.size()][2];
        List<AnimationScheduler> animationList = new ArrayList<>(path.size());

        int xBefore = 0;
        int yBefore = 0;

        for (int i = 0; i < path.size(); i++) {
            Square s = gridMap.getSquare(path.get(i).getX(), path.get(i).getY());

            if (i > 0) {
                animationList.add(new AnimationScheduler(
                        new VertexAnimator(),
                        new Vertex(xBefore, yBefore),
                        new Vertex(s.getMiddleX(), s.getMiddleY()),
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
        if (nextPath != null) {
            nextPathCoords = getPathRepresentation(nextPath);

            enemyPaths = new ArrayList<>(enemies.size());
            for (Enemy enemy : enemies) {
                if (enemy.hasPath()) {
                    enemyPaths.add(getPathRepresentation(enemy.getPathSnippet()));
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
        if (moveButton != null) moveButton.draw(canvas);
        if (shootButton != null) shootButton.draw(canvas);
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
    private void createMap(int width, int height) throws IOException {

        //TODO: debugging events
        events = new EventMap();
        //put a new event at position 15,10
        events.put(new Vertex(15, 11), new EventObserver(itemDescription));
        //set all to disabled as a start state
        events.disableAll();

        gridMap = new GridMap(width, height, events,
                MapReader.readMap(context.getResources().openRawResource(R.raw.test_map)));

        //create initial movement overlay
        gridMap.setPlayerDestination(player.getPosition());
        player.setMovablePositions(gridMap.getPathCaster().castAllPaths(player.getPosition(), MAX_MOVEMENT_LENGTH));
        gridMap.getMapDrawable().drawSquareBackgrounds(player.getMovablePositions(), squareHlPaint);

        //set initial player screen position
        Square s = gridMap.getSquare(player.getX(), player.getY());
        player.setScreenLocation(new Vertex(s.getMiddleX(), s.getMiddleY()));
        //add action buttons
        placeActionButtons(player.getScreenLocation());

        //set enemy screen positions
        for (Enemy enemy : enemies) {
            Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
            enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));
            enemy.setFieldOfView(
                    gridMap.castFOVShadow(
                            enemy.getPosition(), STANDARD_ENEMY_FOV_LENGTH, Direction.SOUTH
                    )
            );

            gridMap.getMapDrawable().drawSquareBackgrounds(enemy.getFieldOfView(), FOVpaint);
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
            try {
                createMap(width, height);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}