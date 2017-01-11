package me.kooruyu.games.battlefield1648.renderers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import me.kooruyu.games.battlefield1648.GameOverScreen;
import me.kooruyu.games.battlefield1648.R;
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
import me.kooruyu.games.battlefield1648.entities.AlertStatus;
import me.kooruyu.games.battlefield1648.entities.Enemy;
import me.kooruyu.games.battlefield1648.entities.Player;
import me.kooruyu.games.battlefield1648.events.EventCallable;
import me.kooruyu.games.battlefield1648.events.EventMap;
import me.kooruyu.games.battlefield1648.events.EventObserver;
import me.kooruyu.games.battlefield1648.gameData.GameData;
import me.kooruyu.games.battlefield1648.util.ListUtils;

/**
 * The main canvas thread that takes care of rendering the game
 * parallel to interaction with the MainGameView.
 */

public class CanvasThread extends AbstractCanvasThread implements EventCallable {

    private static final int NO_MODE = 0;
    private static final int MOVE_MODE = 1;
    private static final int SHOOT_MODE = 2;

    private static final int NUM_ENEMIES = 6;
    private static final int NUM_ITEMS = 4;

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
    private ItemDescription[] itemPopups;
    //Buttons
    private TextButton moveButton;
    private TextButton shootButton;
    private TextButton waitButton;
    private TextButton gameStatus;

    //items
    private String[] itemNames;
    private String[] itemDescriptions;

    //Map Size
    private static final int MAP_SIZE_X = 60;
    private static final int MAP_SIZE_Y = 40;

    //Entities
    private Player player;
    private List<Enemy> enemies;
    private List<Enemy> deadEnemies;

    //Animators
    private SequentialAnimator playerAnimator;
    private List<SequentialAnimator> enemyAnimators;
    private SequentialListAnimator squareCascadeAnimator;

    //Events
    private EventMap events;
    private GameData gameData;

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
        enemyAnimators = new ArrayList<>(NUM_ENEMIES);

        nextPathCoords = null;
        nextPath = null;

        playerPathChanged = false;
        enemyPathsChanged = false;
        enemyPaths = null;

        moveButton = null;
        shootButton = null;
        waitButton = null;
        gameStatus = null;

        deadEnemies = new ArrayList<>();

        zoomFactor = 1;
        xOffset = yOffset = 0;

        mode = MOVE_MODE;

        itemNames = context.getResources().getStringArray(R.array.item_names);
        itemDescriptions = context.getResources().getStringArray(R.array.item_descriptions);
        itemPopups = new ItemDescription[NUM_ITEMS];
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
                Bundle[] eventMetadata = gridMap.setPlayerDestination(player.getPosition());
                if (eventMetadata != null) {
                    for (Bundle eventData : eventMetadata) {
                        if (eventData != null && eventData.getString("ID").equals(ItemDescription.EVENT_ID)) {
                            gameData.collectItem(eventData.getString("ITEM"));
                            gameStatus.setText(String.format(Locale.ENGLISH, "%d/%d collected", gameData.getCollectedItems().size(), NUM_ITEMS));
                            gridMap.getSquare(player.getX(), player.getY()).setPaint(gridMap.getMapDrawable().getSquarePaint());
                        }
                    }
                }
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
                if (enemy.hasPath() && !enemy.isStopped() && player.inFieldOfView(enemy.getPosition())) {
                    SequentialAnimator currentAnimator = new SequentialAnimator();
                    currentAnimator.addListener(enemy);
                    enemyAnimators.add(currentAnimator);
                    enemyPaths.add(getPathAnimation(enemy.getPathSnippet(), currentAnimator));
                }
            }
        }
    }


    private void fixedOnTouchEvent() {
        for (ItemDescription desc : itemPopups) {
            desc.setVisible(false, false);
        }

        //Handles region specific events
        int x = (int) touchEvent.getX();
        int y = (int) touchEvent.getY();

        //handles gridMap events
        Vertex touchedPosition = null;
        if (gridMap.getBounds().contains(x, y)) {

            //FIXME: out of bounds exceptions when zoomed in
            touchedPosition = gridMap.getVertex(x, y);
        }

        if ((playerAnimator == null || !playerAnimator.isRunning())) {
            if (mode != MOVE_MODE && moveButton.contains(x, y)) {
                if (mode == SHOOT_MODE) {
                    gridMap.getMapDrawable().clearSquareBackgrounds(player.getShootArch());
                }
                player.setMovablePositions(gridMap.getPathCaster().castAllPaths(player.getPosition(), MAX_MOVEMENT_LENGTH));

                gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);
                redrawEnemyFOVs();
                squareCascadeAnimator = gridMap.getSquareCascadingAnimation(
                        gridMap.getPathCaster().getPathTraversal(player.getPosition(), MAX_MOVEMENT_LENGTH),
                        CASCADING_ANIMATION_LENGTH / 2, squareHlPaint
                );

                mode = MOVE_MODE;


            } else if (mode != SHOOT_MODE && shootButton.contains(x, y)) {
                if (mode == MOVE_MODE) {
                    gridMap.clearStartingPosition(player.getPosition());
                    gridMap.getMapDrawable().clearSquareBackgrounds(player.getMovablePositions());
                    redrawEnemyFOVs();
                }
                player.setShootArch(gridMap.castFOVShadow(player.getPosition(), Player.PISTOL_RANGE, Direction.ALL));

                gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);
                redrawEnemyFOVs();
                squareCascadeAnimator = gridMap.getSquareCascadingAnimation(
                        gridMap.getShadowCaster().castShadowLevels(player.getX(), player.getY(), Player.PISTOL_RANGE, Direction.ALL),
                        CASCADING_ANIMATION_LENGTH, shootArchPaint
                );

                mode = SHOOT_MODE;

            } else if (waitButton.contains(x, y)) {
                if (mode == SHOOT_MODE) {
                    gridMap.getMapDrawable().clearSquareBackgrounds(player.getShootArch());
                } else if (mode == MOVE_MODE) {
                    gridMap.clearStartingPosition(player.getPosition());
                    gridMap.getMapDrawable().clearSquareBackgrounds(player.getMovablePositions());
                    redrawEnemyFOVs();
                }

                //uses waiting time for reloading the pistol
                player.reload(false);
                updateEnemies();
                gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);
                redrawEnemyFOVs();

                enemyPathsChanged = true;

                mode = NO_MODE;

            } else if (mode == MOVE_MODE && touchedPosition != null && (squareCascadeAnimator == null || !squareCascadeAnimator.isRunning()) && player.canMoveTo(touchedPosition.x, touchedPosition.y)) {
                mode = NO_MODE;

                nextPath = gridMap.getPathTo(player.getX(), player.getY(), touchedPosition.x, touchedPosition.y);
                gridMap.clearStartingPosition(player.getPosition());
                gridMap.getMapDrawable().clearSquareBackgrounds(player.getMovablePositions());
                gridMap.getMapDrawable().clearSquareBackgrounds(player.getFieldOfView());


                player.moveTo(touchedPosition.x, touchedPosition.y);
                player.setFieldOfView(gridMap.castFOVShadow(player.getPosition(), Player.FOV_SIZE, Direction.ALL));

                playerPathChanged = true;
                enemyPathsChanged = true;

                //process turn end
                updateEnemies();

            } else if (mode == SHOOT_MODE && touchedPosition != null) {
                if (!squareCascadeAnimator.isRunning() && player.getShootArch().contains(touchedPosition)) {
                    for (Enemy enemy : enemies) {
                        if (!enemy.isDead()
                                && enemy.getX() == touchedPosition.x
                                && enemy.getY() == touchedPosition.y
                                && player.shoot()) {

                            enemy.kill();
                            gameData.addShot();
                            deadEnemies.add(enemy);
                            //TODO: improve this eventually
                            enemy.setPaint(deathPaint);
                            enemy.setPath(null);

                            gridMap.getMapDrawable().clearSquareBackgrounds(player.getShootArch());
                            if (enemy.hasFieldOfView()) {
                                gridMap.getMapDrawable().clearSquareBackgrounds(enemy.getFieldOfView());
                                enemy.setFieldOfView(null);
                            }
                            gridMap.getMapDrawable().drawSquareBackgrounds(player.getFieldOfView(), playerFOVpaint);

                            //process weapon sound
                            for (Enemy otherEnemy : enemies) {
                                if (!enemy.isDead()) {
                                    //alert all enemies which can hear the sound loudly enough
                                    if (gridMap.castSoundRay(player.getPosition(), enemy.getPosition(), Player.SHOOTING_NOISE) >= Enemy.LOUD_NOISE_THRESHOLD) {
                                        otherEnemy.setStatus(AlertStatus.SEARCHING);
                                    }
                                }
                            }

                            //unblock dead enemies
                            gridMap.setBlocked(enemy.getPosition(), false);
                            //shooting takes a turn
                            updateEnemies();
                            //only one enemy can be killed per turn
                            break;
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
        //every time the enemies are updated a turn is taken
        gameData.addTurn();

        Set<Vertex> FOVs = new HashSet<>();
        Random rand = new Random(SystemClock.elapsedRealtime());

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                continue;
            }

            //TODO: figure out ideal sound values
            enemy.markHeard(gridMap.castSoundRay(enemy.getPosition(), player.getPosition(), Enemy.MOVEMENT_SOUND) > 0);

            if (enemy.getStatus() != AlertStatus.FOLLOWING && !enemy.inFieldOfView(player.getPosition())) {

                if (enemy.getStatus() == AlertStatus.SEARCHING && enemy.isCooledDown()) {
                    enemy.setStatus(AlertStatus.IDLE);
                }

                for (Enemy deadEnemy : deadEnemies) {
                    if (enemy.inFieldOfView(deadEnemy.getPosition())) {
                        enemy.discoverBody(deadEnemy.ID);
                    }
                }

                if (enemy.getStatus().stops(enemy.isStopped())) {

                    stoppedEnemy(enemy, rand);

                    if (player.inFieldOfView(enemy.getPosition())) {
                        FOVs.addAll(enemy.getFieldOfView());
                        enemy.setVisible(true, true);
                    } else {
                        enemy.setVisible(false, false);
                    }

                    if (enemy.inFieldOfView(player.getPosition())) {
                        enemy.setStatus(AlertStatus.FOLLOWING);
                        gameData.addSeen();
                    }
                } else if (enemy.hasTraversed()) {
                    enemy.setStopped(false);
                    Set<Vertex> possiblePaths = gridMap.getPathCaster().castMaximumPaths(enemy.getPosition(), (int) (MAX_MOVEMENT_LENGTH * 4.5), gridMap.getMapData().bounds);

                    Vertex target = ListUtils.getRandomElement(possiblePaths, rand);

                    enemy.setPath(gridMap.getPathTo(enemy.getX(), enemy.getY(), target.x, target.y));

                    //stop once before continuing to path
                    enemy.setStopped(true);

                    stoppedEnemy(enemy, rand);

                    if (player.inFieldOfView(enemy.getPosition())) {
                        FOVs.addAll(enemy.getFieldOfView());
                        enemy.setVisible(true, true);
                    } else {
                        enemy.setVisible(false, false);
                    }

                    if (enemy.inFieldOfView(player.getPosition())) {
                        enemy.setStatus(AlertStatus.FOLLOWING);
                        gameData.addSeen();
                    }
                    //relocateEnemy(enemy, enemy.getPath(), 0);
                    //enemy.increasePathIndexBy(enemy.getStatus().movementSpeed);

                } else {
                    enemy.setStopped(false);
                    relocateEnemy(enemy, enemy.getPath(), enemy.getLastPathIndex());
                    enemy.increasePathIndexBy(enemy.getStatus().movementSpeed);
                }

                if (player.inFieldOfView(enemy.getPosition())) {
                    FOVs.addAll(enemy.getFieldOfView());
                    enemy.setVisible(true, false);
                } else {
                    enemy.setVisible(false, false);
                }

                continue;
            }
            if (!enemy.inFieldOfView(player.getPosition())) {

                if (enemy.getSearchState() == null) {
                    enemy.startSearch(player.getDirection());
                }

                if (enemy.getSearchState().getState() == Enemy.SearchState.BACK_TO_ALERT) {
                    enemy.stopSearch();
                    enemy.setStatus(AlertStatus.SEARCHING);

                    stoppedEnemy(enemy, rand);

                } else {
                    int state = enemy.getSearchState().getState();
                    if (state == Enemy.SearchState.LOOKING) {
                        stoppedEnemy(enemy, rand);
                    } else { //if (state == Enemy.SearchState.FOLLOWING_DIRECTION) {
                        enemy.setStopped(false);

                        Set<Vertex> possiblePaths = gridMap.getPathCaster().castMaximumDirectionPaths(
                                enemy.getPosition(), (int) (MAX_MOVEMENT_LENGTH * 4.5), enemy.getSearchState().previousPlayerDirection
                        );

                        //in case following the direction of the player is impossible
                        if (possiblePaths.isEmpty()) {
                            possiblePaths = gridMap.getPathCaster().castMaximumPaths(enemy.getPosition(), enemy.getStatus().movementSpeed * 2, gridMap.getMapData().bounds);
                        }

                        Vertex target = ListUtils.getRandomElement(possiblePaths, rand);
                        enemy.setPath(gridMap.getPathTo(enemy.getX(), enemy.getY(), target.x, target.y));

                        relocateEnemy(enemy, enemy.getPath(), 0);
                        enemy.increasePathIndexBy(enemy.getStatus().movementSpeed);
                        //} else {
                        //    enemy.setStopped(false);
                        //    ArrayList<Vertex> path = gridMap.getPathTo(enemy.getX(), enemy.getY(), player.getX(), player.getY());
                        //    relocateEnemy(enemy, path, 0);
                    }

                    enemy.getSearchState().advance();
                }

                if (player.inFieldOfView(enemy.getPosition())) {
                    FOVs.addAll(enemy.getFieldOfView());
                    enemy.setVisible(true, true);
                } else {
                    enemy.setVisible(false, false);
                }

                continue;
            }

            enemy.setStatus(AlertStatus.FOLLOWING);
            gameData.addSeen();
            ArrayList<Vertex> path = gridMap.getPathTo(enemy.getX(), enemy.getY(), player.getX(), player.getY());

            //should only happen when an enemy is on the same square as the player
            if (path == null) {
                enemy.setStopped(true);
                gridMap.getMapDrawable().clearSquareBackgrounds(enemy.getFieldOfView());

                if (player.inFieldOfView(enemy.getPosition())) {
                    FOVs.addAll(enemy.getFieldOfView());
                    enemy.setVisible(true, true);
                } else if (enemy.getPosition().equals(player.getPosition())) {
                    enemy.setVisible(true, false);
                } else {
                    enemy.setVisible(false, false);
                }

                continue;
            }

            enemy.setStopped(false);

            relocateEnemy(enemy, path, 0);
            if (player.inFieldOfView(enemy.getPosition())) {
                FOVs.addAll(enemy.getFieldOfView());
                enemy.setVisible(true, false);
            } else if (enemy.getPosition().equals(player.getPosition())) {
                enemy.setVisible(true, false);
            } else {
                enemy.setVisible(false, false);
            }
        }

        gridMap.getMapDrawable().drawSquareBackgrounds(FOVs, FOVpaint);
    }

    private void stoppedEnemy(Enemy enemy, Random rand) {
        enemy.setStopped(true);

        gridMap.getMapDrawable().clearSquareBackgrounds(enemy.getFieldOfView());
        if (enemy.getDirection() != Direction.ALL) {
            //turns either left or right with a 50:50 chance
            enemy.setDirection((rand.nextInt(2) == 1) ? enemy.getDirection().turnLeft() : enemy.getDirection().turnRight());
        }

        enemy.setFieldOfView(
                gridMap.castFOVShadow(enemy.getPosition(), STANDARD_ENEMY_FOV_LENGTH, enemy.getDirection())
        );
    }


    private void relocateEnemy(Enemy enemy, List<Vertex> path, int startingIndex) {
        List<Vertex> snippet = new ArrayList<>();
        for (int i = startingIndex; i < path.size() && i < (enemy.getStatus().movementSpeed + startingIndex); i++) {
            snippet.add(path.get(i));
        }
        enemy.setPathSnippet(snippet);

        gridMap.setBlocked(enemy.getPosition(), false);

        enemy.moveTo(snippet.get(snippet.size() - 1));
        Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
        enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));

        if (enemy.getPosition().equals(player.getPosition())) {
            gameData.fail();
            if (enemy.getStatus() == AlertStatus.FOLLOWING) {
                gameData.setFate("Caught and killed by an enemy soldier");
            } else {
                gameData.setFate("Killed by a surprised enemy soldier");
            }
            if (gameData.getCollectedItems().size() == gameData.itemsTotal) {
                gameData.setFate(gameData.getFate() + " while trying to flee after completing the mission");
            }

            endGame();
        }

        gridMap.setBlocked(enemy.getPosition(), true);


        gridMap.getMapDrawable().clearSquareBackgrounds(enemy.getFieldOfView());
        enemy.setDirection(Direction.getDirection(enemy.getPreviousX(), enemy.getPreviousY(), enemy.getX(), enemy.getY()));

        enemy.setFieldOfView(
                gridMap.castFOVShadow(enemy.getPosition(), STANDARD_ENEMY_FOV_LENGTH, enemy.getDirection())
        );

        if (enemy.inFieldOfView(player.getPosition())) {
            enemy.setStatus(AlertStatus.FOLLOWING);
            gameData.addSeen();
        }
    }


    private void placeActionButtons(int width, int height) {
        int x = -xOffset;
        int y = -yOffset;
        int buttonHeight = (height / 10) * 8;
        int buttonsWidth = (width / 2);
        moveButton.resize(x, y + buttonHeight, x + (buttonsWidth / 3), y + height);
        waitButton.resize(x + (buttonsWidth / 3), y + buttonHeight, x + ((buttonsWidth / 3) * 2), y + height);
        shootButton.resize(x + ((buttonsWidth / 3) * 2), y + buttonHeight, x + buttonsWidth, y + height);
        gameStatus.resize(x + buttonsWidth, y + buttonHeight, x + width, y + height);
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

    private void endGame() {
        Intent gameOver = new Intent(context, GameOverScreen.class);

        gameOver.putExtra("GAME_DATA", gameData);
        context.startActivity(gameOver);
        //stop the game activity
        ((Activity) context).finish();
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

                    if (currentZoomFactor <= 2 && currentZoomFactor >= 1) {
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
        gameStatus.draw(canvas);
        for (ItemDescription desc : itemPopups) {
            desc.draw(canvas);
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
    private void createMap(int width, int height) throws IOException {

        CampData rawMap;

        do {
            rawMap = new MapGenerator(new Random().nextLong()).generateCamp(MAP_SIZE_X, MAP_SIZE_Y);
        }
        while (rawMap.rooms.size() <= NUM_ENEMIES); //reject maps with too few rooms - this is highly unlikely to run multiple times

        gameData = new GameData(NUM_ITEMS, rawMap.seed);

        enemies = new ArrayList<>(NUM_ENEMIES);

        List<Vertex> enemyPositions = rawMap.getRandomRoomPositions(NUM_ENEMIES);
        int id = 0;
        for (Vertex location : enemyPositions) {
            enemies.add(new Enemy(location.x, location.y, pathPaint, enemyHeardPaint, id));
            id++;
        }

        events = new EventMap();

        List<Vertex> eventPositions = rawMap.getRandomRoomPositions(NUM_ITEMS);
        Random rand = new Random(rawMap.seed);
        for (int i = 0; i < NUM_ITEMS; i++) {
            int itemIndex = rand.nextInt(itemNames.length);
            //create layers to fit the new screen size
            ItemDescription desc = new ItemDescription(
                    itemNames[itemIndex], itemDescriptions[itemIndex],
                    width * .1f, height * .1f, width * .9f, height * .9f
            );
            itemPopups[i] = desc;
            events.put(eventPositions.get(i), new EventObserver(desc, true));
        }

        //add exit map events that end the game
        for (int x = (rawMap.width - 1), y = 0; y < rawMap.height; y++) {
            events.put(new Vertex(x, y), new EventObserver(this, true));
        }

        //set all to disabled as a start state
        events.disableAll();

        gridMap = new GridMap(width, height, events,
                rawMap, context.getResources()
                //MapReader.readMap(context.getResources().openRawResource(R.raw.test_map))
        );

        //add temporary marker on map to highlight debug event location
        Paint itemMarker = new Paint();
        itemMarker.setColor(Color.RED);

        Paint exitMarker = new Paint();
        exitMarker.setColor(Color.GREEN);
        
        for (Vertex location : eventPositions) {
            gridMap.getSquare(location.x, location.y).setPaint(itemMarker);
        }

        for (int x = (rawMap.width - 1), y = 0; y < rawMap.height; y++) {
            gridMap.getSquare(x, y).setPaint(exitMarker);
        }

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
        int buttonHeight = (height / 10) * 8;
        int buttonsWidth = (width / 2);
        moveButton = new TextButton(0, buttonHeight, buttonsWidth / 3, height, "move", FOVpaint);
        waitButton = new TextButton((buttonsWidth / 3), buttonHeight, (buttonsWidth / 3) * 2, height, "wait", FOVpaint);
        shootButton = new TextButton((buttonsWidth / 3) * 2, buttonHeight, buttonsWidth, height, "shoot", FOVpaint);
        gameStatus = new TextButton(buttonsWidth, buttonHeight, width, height, String.format(Locale.ENGLISH, "0/%d collected", NUM_ITEMS), FOVpaint);

        //set enemy screen positions
        for (Enemy enemy : enemies) {
            //block initial entity locations
            gridMap.setBlocked(enemy.getPosition(), true);

            enemy.setVisible(false, false);
            Square temp = gridMap.getSquare(enemy.getX(), enemy.getY());
            enemy.setScreenLocation(new Vertex(temp.getMiddleX(), temp.getMiddleY()));
            enemy.setFieldOfView(
                    gridMap.castFOVShadow(
                            enemy.getPosition(), STANDARD_ENEMY_FOV_LENGTH, Direction.SOUTH
                    )
            );
        }
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

            //initialize map
            try {
                createMap(width, height);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setActive(boolean active) {
        if (active) {
            trigger();
        }
    }

    /**
     * This method is called when the player reaches the right map border
     */
    @Override
    public void trigger() {
        if (gameData.getCollectedItems().size() == 0) {
            gameData.setFate("You decided to live a deserter in order stay alive.");
        } else if (gameData.getCollectedItems().size() == gameData.itemsTotal) {
            gameData.setFate("Mission completed succesfully");
        } else {
            gameData.setFate("At least it can be called a partial success");
        }
        //TODO: do more processing here

        endGame();
    }

    @Override
    public Bundle getMetadata() {
        return null;
    }
}