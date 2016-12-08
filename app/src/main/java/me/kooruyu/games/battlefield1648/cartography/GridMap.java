package me.kooruyu.games.battlefield1648.cartography;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.kooruyu.games.battlefield1648.algorithms.DijkstraPathfinder;
import me.kooruyu.games.battlefield1648.algorithms.Graph;
import me.kooruyu.games.battlefield1648.algorithms.PathCaster;
import me.kooruyu.games.battlefield1648.algorithms.ShadowCaster;
import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.AnimationScheduler;
import me.kooruyu.games.battlefield1648.animations.SequentialListAnimator;
import me.kooruyu.games.battlefield1648.animations.SimpleBooleanAnimator;
import me.kooruyu.games.battlefield1648.drawables.Square;
import me.kooruyu.games.battlefield1648.drawables.layers.GridMapDrawable;
import me.kooruyu.games.battlefield1648.events.EventMap;

public class GridMap extends Drawable {

    public static final int STANDARD_ZOOM = 1;

    private final GridMapDrawable mapDrawable;
    private final Graph mapGraph;

    private DijkstraPathfinder pathfinder;
    private ShadowCaster shadowCaster;
    private PathCaster pathCaster;

    private EventMap events;


    public GridMap(int width, int height, EventMap events, char[][] mapData) {
        this.events = events;
        int ySquares = mapData.length;
        int xSquares = mapData[0].length;

        mapDrawable = new GridMapDrawable(xSquares, ySquares, width, height, mapData);
        setBounds(mapDrawable.getBounds());
        mapGraph = mapDrawable.getMapGraph();
        pathfinder = new DijkstraPathfinder(mapGraph);
        pathCaster = new PathCaster(mapGraph);
        shadowCaster = new ShadowCaster(mapDrawable, xSquares, ySquares);
    }

    public Square getSquare(int x, int y) {
        return mapDrawable.getSquare(x, y);
    }

    public Vertex getVertex(int x, int y) {
        return mapDrawable.getVertex(x, y);
    }

    /**
     * Clears previous player position and disables all events
     *
     * @param start The players previous position
     */
    public void clearStartingPosition(Vertex start) {
        if (events.containsPosition(start)) {
            events.getEventAt(start).setAll(false);
        }
    }

    public ArrayList<Vertex> getPathTo(int playerX, int playerY, int x, int y) {
        return pathfinder.getPathTo(new Vertex(playerX, playerY), new Vertex(x, y));
    }

    /**
     * Draws movement indicator and updates events for the given player position
     *
     * @param target the position of the player
     */
    public void setPlayerDestination(Vertex target) {
        if (events.containsPosition(target)) {
            events.getEventAt(target).setAll(true);
        }
    }

    public PathCaster getPathCaster() {
        return pathCaster;
    }

    public Set<Vertex> castFOVShadow(Vertex middle, int range, Direction direction) {
        return shadowCaster.castShadow(middle.getX(), middle.getY(), range, direction);
    }

    public ShadowCaster getShadowCaster() {
        return shadowCaster;
    }

    public void zoomTo(float zoomfactor) {
        mapDrawable.setZoomFactor(zoomfactor);
        setBounds(mapDrawable.getBounds());
    }

    public float getZoomFactor() {
        return mapDrawable.getZoomFactor();
    }

    public void moveTo(int xOffset, int yOffset) {
        mapDrawable.moveZoomed(xOffset, yOffset);
        setBounds(mapDrawable.getBounds());
    }

    public void setBlocked(Vertex vertex, boolean blocked) {
        mapGraph.getNode(vertex).setBlocked(blocked);
    }

    public SequentialListAnimator getSquareCascadingAnimation(List<List<Vertex>> vertexLevels, int levelDuration, Paint paint) {
        SequentialListAnimator animator = new SequentialListAnimator();
        for (List<Vertex> level : vertexLevels) {
            AnimationScheduler levelAnimator = new AnimationScheduler(new SimpleBooleanAnimator(), false, true, levelDuration);
            List<Animatable> animatableSquares = new ArrayList<>();

            for (Vertex v : level) {
                Square s = mapDrawable.getSquare(v.getX(), v.getY());
                s.setBackground(paint);
                s.setBackgroundVisible(false);
                animatableSquares.add(s);
            }

            animator.addAnimationLevel(levelAnimator, animatableSquares);
        }

        return animator;
    }

    public GridMapDrawable getMapDrawable() {
        return mapDrawable;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mapDrawable.draw(canvas);
    }

    @Override
    public void setAlpha(int i) {
        mapDrawable.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mapDrawable.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return mapDrawable.getOpacity();
    }
}
