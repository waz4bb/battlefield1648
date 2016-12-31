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
import me.kooruyu.games.battlefield1648.animations.Animatable;
import me.kooruyu.games.battlefield1648.animations.AnimationScheduler;
import me.kooruyu.games.battlefield1648.animations.SequentialListAnimator;
import me.kooruyu.games.battlefield1648.animations.SimpleBooleanAnimator;
import me.kooruyu.games.battlefield1648.drawables.GridSquare;
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
        return shadowCaster.castShadow(middle.x, middle.y, range, direction);
    }

    public ShadowCaster getShadowCaster() {
        return shadowCaster;
    }

    public void moveTo(int xOffset, int yOffset) {
        mapDrawable.moveTo(xOffset, yOffset);
        setBounds(mapDrawable.getBounds());
    }

    public void zoomTo(float zoomfactor) {
        mapDrawable.setZoomFactor(zoomfactor);
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
                Square s = mapDrawable.getSquare(v.x, v.y);
                if (!s.isOpaque()) {
                    GridSquare gs = (GridSquare) s;
                    gs.setBackground(paint);
                    gs.setBackgroundVisible(false);
                    animatableSquares.add(gs);
                }
            }

            animator.addAnimationLevel(levelAnimator, animatableSquares);
        }

        return animator;
    }

    public double castSoundRay(Vertex start, Vertex end, double soundStrength) {
        int x1 = start.x, y1 = start.y;
        int x2 = end.x, y2 = end.y;

        int w = x2 - x1;
        int h = y2 - y1;
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;

        if (w < 0) dx1 = -1;
        else if (w > 0) dx1 = 1;

        if (h < 0) dy1 = -1;
        else if (h > 0) dy1 = 1;

        if (w < 0) dx2 = -1;
        else if (w > 0) dx2 = 1;

        int longest = Math.abs(w);
        int shortest = Math.abs(h);

        if (longest <= shortest) {
            longest = Math.abs(h);
            shortest = Math.abs(w);

            if (h < 0) {
                dy2 = -1;
            } else if (h > 0) {
                dy2 = 1;
            }
            dx2 = 0;
        }

        int numerator = longest >> 1;
        for (int i = 0; i <= longest; i++) {

            //TODO: modify sound reductions
            if (mapDrawable.getSquare(x1, y1).isMovable()) {
                soundStrength -= 2;
            } else {
                soundStrength -= .5;
            }

            numerator += shortest;

            if (numerator >= longest) {
                numerator -= longest;
                x1 += dx1;
                y1 += dy1;
            } else {
                x1 += dx2;
                y1 += dy2;
            }
        }

        return soundStrength;
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
