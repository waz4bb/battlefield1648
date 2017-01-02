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

import me.kooruyu.games.battlefield1648.animations.AnimationScheduler;
import me.kooruyu.games.battlefield1648.animations.SequentialAnimator;
import me.kooruyu.games.battlefield1648.animations.SequentialListAnimator;
import me.kooruyu.games.battlefield1648.animations.VertexAnimator;
import me.kooruyu.games.battlefield1648.cartography.CampData;
import me.kooruyu.games.battlefield1648.cartography.Direction;
import me.kooruyu.games.battlefield1648.cartography.GridMap;
import me.kooruyu.games.battlefield1648.cartography.MapGenerator;
import me.kooruyu.games.battlefield1648.cartography.Vertex;
import me.kooruyu.games.battlefield1648.drawables.Square;
import me.kooruyu.games.battlefield1648.drawables.layers.ItemDescription;
import me.kooruyu.games.battlefield1648.drawables.layers.TextButton;
import me.kooruyu.games.battlefield1648.entities.Enemy;
import me.kooruyu.games.battlefield1648.entities.MovableEntity;
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

    private static int NUM_ENEMIES = 4;

    //Mode
    private int mode;

    //Enables calculations at a fixed rate
    private long lastUpdate;
    private double accumulator;
    private static final double RENDER_STEP = 1000 / 60;

    //for storing restore data
    private boolean playerPathChanged;
    private boolean enemyPathsChanged;

    //Player input
    private boolean wasTouched;
    private boolean wasScaled;
    private boolean wasMoved;
    private MotionEvent touchEvent;
    private float lastScaleFactor;
    private int movedX, movedY;

    //Paints
    private Paint defaultTextPaint;
    private Paint pathPaint;
    private Paint FOVpaint;
    private Paint deathPaint;
    private Paint shootArchPaint;
    private Paint squareHlPaint;
    private Paint playerFOVpaint;
    private Paint enemyHeardPaint;

    //Frame Counter
    private int frames;
    private int frameUpdate;
    private long lastFPSUpdate;

    //Path
    private ArrayList<Vertex> nextPath;
    private Integer[][] nextPathCoords;
    private final int MAX_MOVEMENT_LENGTH = 4;
    private final int STANDARD_ENEMY_FOV_LENGTH = MAX_MOVEMENT_LENGTH * 2;
    private float zoomFactor;
    private int xOffset, yOffset;
    //in frames per step
    private final int MOVEMENT_ANIMATION_LENGTH = 10;
    private final int CASCADING_ANIMATION_LENGTH = 4;

    //for enemy paths
    private List<Integer[][]> enemyPaths;

    //Layers
    private GridMap gridMap;
    private ItemDescription itemDescription;
    //Buttons
    private TextButton moveButton;
    private TextButton shootButton;
    private TextButton waitButton;

    //Map Size
    private static final int MAP_SIZE_X = 60;
    private static final int MAP_SIZE_Y = 40;

    //Entities
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

        defaultTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultTextPaint.setColor(Color.WHITE);
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
        FOVpaint.setColor(Color.argb(255, 142, 239, 218));

        playerFOVpaint = new Paint();
        playerFOVpaint.setColor(Color.LTGRAY);

        enemyHeardPaint = new Paint();
        enemyHeardPaint.setColor(Color.argb(255, 142, 239, 218));

        playerAnimator = null;
        squareCascadeAnimator = null;

        nextPathCoords = null;
        nextPath = null;

        playerPathChanged = false;
        enemyPathsChanged = false;
        enemyPaths = null;

        moveButton = null;
        shootButton = null;
        waitButton = null;

        zoomFactor = 1;
        xOffset = yOffset = 0;

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
                gridMap.setPlayerDestination(player.getPosition());
                gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);
                redrawEnemyFOVs();
            }
        }

        if (squareCascadeAnimator != null && squareCascadeAnimator.isRunning()) {
            squareCascadeAnimator.dispatchUpdate();
        }

        for (SequentialAnimator animator : enemyAnimators) {
            animator.dispatchUpdate();
        }

        if (wasTouched) {
            fixedOnTouchEvent();
            wasTouched = false;
        }

        if (playerPathChanged && nextPath != null) {

            playerPathChanged = false;

            playerAnimator = new SequentialAnimator();
            nextPathCoords = getPathAnimation(nextPath, playerAnimator);
            playerAnimator.addListener(player);
        }
        if (enemyPathsChanged) {

            enemyPathsChanged = false;

            enemyPaths = new ArrayList<>(enemies.size());
            enemyAnimators = new ArrayList<>(enemies.size());
            for (Enemy enemy : enemies) {
                if (enemy.hasPath() && player.inFieldOfView(enemy.getPosition())) {
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

            //FIXME: out of bounds exceptions when zoomed in
            Vertex touchedPosition = gridMap.getVertex(x, y);

            if ((playerAnimator == null || !playerAnimator.isRunning())) {
                if (mode != MOVE_MODE && moveButton.contains(x, y)) {
                    if (mode == SHOOT_MODE) {
                        gridMap.getMapDrawable().clearSquareBackgrounds(player.getShootArch());
                    }
                    player.setMovablePositions(gridMap.getPathCaster().castAllPaths(player.getPosition(), MAX_MOVEMENT_LENGTH));

                    gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);
                    redrawEnemyFOVs();
                    squareCascadeAnimator = gridMap.getSquareCascadingAnimation(gridMap.getPathCaster().getPathTraversal(player.getPosition(), MAX_MOVEMENT_LENGTH), CASCADING_ANIMATION_LENGTH / 2, squareHlPaint);

                    mode = MOVE_MODE;


                } else if (mode != SHOOT_MODE && shootButton.contains(x, y)) {
                    if (mode == MOVE_MODE) {
                        gridMap.clearStartingPosition(player.getPosition());
                        gridMap.getMapDrawable().clearSquareBackgrounds(player.getMovablePositions());
                        redrawEnemyFOVs();
                    }
                    player.setShootArch(gridMap.castFOVShadow(player.getPosition(), MAX_MOVEMENT_LENGTH, Direction.ALL));

                    gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);
                    redrawEnemyFOVs();
                    squareCascadeAnimator = gridMap.getSquareCascadingAnimation(gridMap.getShadowCaster().castShadowLevels(player.getX(), player.getY(), MAX_MOVEMENT_LENGTH, Direction.ALL), CASCADING_ANIMATION_LENGTH, shootArchPaint);

                    mode = SHOOT_MODE;

                } else if (waitButton.contains(x, y)) {
                    if (mode == SHOOT_MODE) {
                        gridMap.getMapDrawable().clearSquareBackgrounds(player.getShootArch());
                    } else if (mode == MOVE_MODE) {
                        gridMap.clearStartingPosition(player.getPosition());
                        gridMap.getMapDrawable().clearSquareBackgrounds(player.getMovablePositions());
                        redrawEnemyFOVs();
                    }

                    updateEnemies();
                    gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);
                    redrawEnemyFOVs();

                    enemyPathsChanged = true;

                    mode = NO_MODE;

                } else if (mode == MOVE_MODE && (squareCascadeAnimator == null || !squareCascadeAnimator.isRunning()) && player.canMoveTo(touchedPosition.x, touchedPosition.y)) {
                    mode = NO_MODE;

                    nextPath = gridMap.getPathTo(player.getX(), player.getY(), touchedPosition.x, touchedPosition.y);
                    gridMap.clearStartingPosition(player.getPosition());
                    gridMap.getMapDrawable().clearSquareBackgrounds(player.getMovablePositions());
                    gridMap.getMapDrawable().clearSquareBackgrounds(player.getFieldOfView());


                    player.moveTo(touchedPosition.x, touchedPosition.y);
                    player.setFieldOfView(gridMap.castFOVShadow(player.getPosition(), MAX_MOVEMENT_LENGTH * 4, Direction.ALL));

                    playerPathChanged = true;
                    enemyPathsChanged = true;

                    //process turn end
                    updateEnemies();

                } else if (mode == SHOOT_MODE) {
                    if (!squareCascadeAnimator.isRunning() && player.getShootArch().contains(touchedPosition)) {
                        for (Enemy enemy : enemies) {
                            if (!enemy.isDead()
                                    && enemy.getX() == touchedPosition.x
                                    && enemy.getY() == touchedPosition.y) {

                                enemy.kill();
                                enemy.setPaint(deathPaint);
                                enemy.setPath(null);

                                gridMap.getMapDrawable().clearSquareBackgrounds(player.getShootArch());
                                if (enemy.hasFieldOfView()) {
                                    gridMap.getMapDrawable().clearSquareBackgrounds(enemy.getFieldOfView());
                                    enemy.setFieldOfView(null);
                                }
                                gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);

                                //shooting takes a turn
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
            if (!enemy.isDead() && player.inFieldOfView(enemy.getPosition())) {
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
                continue;
            }

            //TODO: figure out ideal sound values
            enemy.markHeard(gridMap.castSoundRay(enemy.getPosition(), player.getPosition(), MovableEntity.MOVEMENT_SOUND) > 0);

            if (enemy.getStatus() == Enemy.IDLE && !enemy.inFieldOfView(player.getPosition())) {

                if (enemy.hasTraversed()) {
                    Set<Vertex> possiblePaths = gridMap.getPathCaster().castMaximumPaths(enemy.getPosition(), (int) (MAX_MOVEMENT_LENGTH * 4.5));
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

                    enemy.setPath(gridMap.getPathTo(enemy.getX(), enemy.getY(), target.x, target.y));

                    relocateEnemy(enemy, enemy.getPath(), 0);
                    enemy.increasePathIndexBy(MAX_MOVEMENT_LENGTH);

                } else {
                    relocateEnemy(enemy, enemy.getPath(), enemy.getLastPathIndex());
                    enemy.increasePathIndexBy(MAX_MOVEMENT_LENGTH);
                }

                if (player.inFieldOfView(enemy.getPosition())) {
                    FOVs.addAll(enemy.getFieldOfView());
                    enemy.setVisible(true, false);
                } else {
                    enemy.setVisible(false, false);
                }
                continue;
            }

            enemy.setStatus(Enemy.SEARCHING);
            ArrayList<Vertex> path = gridMap.getPathTo(enemy.getX(), enemy.getY(), player.getX(), player.getY());

            if (path == null) {
                if (player.inFieldOfView(enemy.getPosition())) {
                    FOVs.addAll(enemy.getFieldOfView());
                    enemy.setVisible(true, false);
                } else {
                    enemy.setVisible(false, false);
                }

                continue;
            }

            relocateEnemy(enemy, path, 0);
            if (player.inFieldOfView(enemy.getPosition())) {
                FOVs.addAll(enemy.getFieldOfView());
                enemy.setVisible(true, false);
            } else {
                enemy.setVisible(false, false);
            }
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
        Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
        enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));

        gridMap.setBlocked(enemy.getPosition(), true);


        gridMap.getMapDrawable().clearSquareBackgrounds(enemy.getFieldOfView());
        enemy.setFieldOfView(
                gridMap.castFOVShadow(enemy.getPosition(), STANDARD_ENEMY_FOV_LENGTH, Direction.getDirection(
                        enemy.getPreviousX(), enemy.getPreviousY(), enemy.getX(), enemy.getY()
                        )
                )
        );

        if (enemy.inFieldOfView(player.getPosition())) {
            enemy.setStatus(Enemy.SEARCHING);
        }
    }


    private void placeActionButtons(int width, int height) {
        int x = -xOffset;
        int y = -yOffset;
        int buttonHeight = (height / 5) * 4;
        moveButton.resize(x, y + buttonHeight, x + (width / 3), y + height);
        waitButton.resize(x + (width / 3), y + buttonHeight, x + ((width / 3) * 2), y + height);
        shootButton.resize(x + ((width / 3) * 2), y + buttonHeight, x + width, y + height);
    }


    private Integer[][] getPathAnimation(List<Vertex> path, SequentialAnimator animator) {
        Integer[][] coords = new Integer[path.size()][2];
        List<AnimationScheduler> animationList = new ArrayList<>(path.size());

        int xBefore = 0;
        int yBefore = 0;

        for (int i = 0; i < path.size(); i++) {
            Square s = gridMap.getSquare(path.get(i).x, path.get(i).y);

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
            Square s = gridMap.getSquare(path.get(i).x, path.get(i).y);

            coords[i][0] = s.getMiddleX();
            coords[i][1] = s.getMiddleY();
        }

        return coords;
    }

    /**
     * Draws current state to screen
     *
     * @param canvas The canvas to be drawn on
     */
    @Override
    void draw(Canvas canvas) {
        //apply transformations
        if (playerAnimator == null || !playerAnimator.isRunning()) {
            if (wasScaled) {
                if (playerAnimator == null || !playerAnimator.isRunning()) {
                    float currentZoomFactor = zoomFactor + lastScaleFactor;

                    if (((currentZoomFactor - 1) <= 0.05)) currentZoomFactor = 1;

                    if (currentZoomFactor < 2 && currentZoomFactor >= 1) {
                        zoomFactor = currentZoomFactor;
                        gridMap.zoomTo(zoomFactor);
                    }
                }
            }
            if (wasMoved) {
                if ((xOffset + movedX) > 0) {
                    xOffset = 0;
                } else {
                    xOffset += movedX;
                }

                if ((yOffset + movedY) > 0) {
                    yOffset = 0;
                } else {
                    yOffset += movedY;
                }

                gridMap.moveTo(-xOffset, -yOffset);
            }
        }
        canvas.scale(zoomFactor, zoomFactor);
        canvas.translate(xOffset, yOffset);
        if (wasScaled || wasMoved) {
            placeActionButtons(canvas.getClipBounds().width(), canvas.getClipBounds().height());
            wasScaled = false;
            wasMoved = false;
        }

        //white background
        canvas.drawColor(Color.BLACK);

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
                }
            }
        }

        //draw entities
        player.draw(canvas);
        //draw enemies
        for (Enemy enemy : enemies) {
            enemy.draw(canvas);
        }

        //draw layers/buttons
        moveButton.draw(canvas);
        waitButton.draw(canvas);
        shootButton.draw(canvas);
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
        //put a new event at position 15,11
        events.put(new Vertex(15, 11), new EventObserver(itemDescription));
        //set all to disabled as a start state
        events.disableAll();

        CampData rawMap;

        do {
            rawMap = new MapGenerator(new Random().nextLong()).generateCamp(MAP_SIZE_X, MAP_SIZE_Y);
        } while (rawMap.rooms.size() <= 4);
        //reject maps with too few rooms - this is highly unlikely to run multiple times

        enemies = new ArrayList<>(NUM_ENEMIES);
        enemyAnimators = new ArrayList<>(NUM_ENEMIES);

        List<Vertex> enemyPositions = rawMap.getRandomRoomPositions(NUM_ENEMIES);
        for (Vertex location : enemyPositions) {
            enemies.add(new Enemy(location.x, location.y, pathPaint, enemyHeardPaint));
        }

        gridMap = new GridMap(width, height, events,
                rawMap
                //MapReader.readMap(context.getResources().openRawResource(R.raw.test_map))
        );

        //place player
        player = new Player(rawMap.getStartingPosition(), pathPaint);

        //create initial movement overlay
        gridMap.setPlayerDestination(player.getPosition());

        player.setMovablePositions(gridMap.getPathCaster().castAllPaths(player.getPosition(), MAX_MOVEMENT_LENGTH));
        player.setFieldOfView(gridMap.castFOVShadow(player.getPosition(), MAX_MOVEMENT_LENGTH * 4, Direction.ALL));

        gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);
        gridMap.getMapDrawable().drawSquareBackgrounds(player.getMovablePositions(), squareHlPaint);

        //set initial player screen position
        Square s = gridMap.getSquare(player.getX(), player.getY());
        player.setScreenLocation(new Vertex(s.getMiddleX(), s.getMiddleY()));
        //add action buttons
        moveButton = new TextButton(0, (height / 5) * 4, width / 2, height, "move", FOVpaint);
        waitButton = new TextButton((width / 3), (height / 5) * 4, (width / 3) * 2, height, "wait", FOVpaint);
        shootButton = new TextButton((width / 3) * 2, (height / 5) * 4, width, height, "shoot", FOVpaint);

        //set enemy screen positions
        for (Enemy enemy : enemies) {
            enemy.setVisible(false, false);
            Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
            enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));
            enemy.setFieldOfView(
                    gridMap.castFOVShadow(
                            enemy.getPosition(), STANDARD_ENEMY_FOV_LENGTH, Direction.SOUTH
                    )
            );
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

            //initialize map
            try {
                createMap(width, height);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}