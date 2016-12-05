package me.kooruyu.games.battlefield1648.cartography;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Set;

import me.kooruyu.games.battlefield1648.algorithms.DijkstraPathfinder;
import me.kooruyu.games.battlefield1648.algorithms.Graph;
import me.kooruyu.games.battlefield1648.algorithms.PathCaster;
import me.kooruyu.games.battlefield1648.algorithms.ShadowCaster;
import me.kooruyu.games.battlefield1648.algorithms.Vertex;
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
    private int maximumMovementLength;

    private Set<Vertex> moveableSquares;


    public GridMap(int width, int height, int maximumMovementLength, EventMap events, char[][] mapData) {
        this.maximumMovementLength = maximumMovementLength;
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
        mapDrawable.clearSquareBackgrounds(moveableSquares);

        if (events.containsPosition(start)) {
            events.getEventAt(start).setAll(false);
        }
    }

    public ArrayList<Vertex> getPathTo(int playerX, int playerY, int x, int y) {
        return pathfinder.getPathTo(new Vertex(playerX, playerY), new Vertex(x, y));
    }

    public boolean isMovable(int playerX, int playerY, int x, int y) {
        return !(playerX == x && playerY == y) //return false if the target is the same as the current position
                && isReachable(new Vertex(x, y)); //check if the target is in reach of the precomputed movable area
    }

    private boolean isReachable(Vertex target) {
        return moveableSquares.contains(target);
    }

    /**
     * Draws movement indicator and updates events for the given player position
     *
     * @param target the position of the player
     */
    public void setPlayerDestination(Vertex target) {
        moveableSquares = pathCaster.castPathDistance(target, maximumMovementLength);
        mapDrawable.drawSquareBackgrounds(moveableSquares, mapDrawable.getSquareHlPaint());

        if (events.containsPosition(target)) {
            events.getEventAt(target).setAll(true);
        }
    }


    public Set<Vertex> castFOVShadow(Vertex middle, int range, Direction direction) {
        return shadowCaster.castShadow(middle.getX(), middle.getY(), range, direction);
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
