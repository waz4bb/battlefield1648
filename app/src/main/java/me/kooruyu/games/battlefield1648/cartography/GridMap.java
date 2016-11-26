package me.kooruyu.games.battlefield1648.cartography;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import me.kooruyu.games.battlefield1648.algorithms.DijkstraPathfinder;
import me.kooruyu.games.battlefield1648.algorithms.Edge;
import me.kooruyu.games.battlefield1648.algorithms.Graph;
import me.kooruyu.games.battlefield1648.algorithms.Vertex;
import me.kooruyu.games.battlefield1648.drawables.Square;
import me.kooruyu.games.battlefield1648.drawables.layers.GridMapDrawable;
import me.kooruyu.games.battlefield1648.events.EventMap;

public class GridMap extends Drawable {

    public static final int STANDARD_ZOOM = 1;

    private final GridMapDrawable mapDrawable;
    private final Graph mapGraph;

    private DijkstraPathfinder pathfinder;
    //private ShadowCaster shadowCaster;

    private EventMap events;
    private int maximumMovementLength;

    private Set<Vertex> moveableSquares;


    public GridMap(int xSquares, int ySquares, int width, int height, int maximumMovementLength, EventMap events) {
        this.maximumMovementLength = maximumMovementLength;
        this.events = events;

        mapDrawable = new GridMapDrawable(xSquares, ySquares, width, height);
        setBounds(mapDrawable.getBounds());
        mapGraph = mapDrawable.getMapGraph();
        pathfinder = new DijkstraPathfinder(mapGraph);
        //shadowCaster = new ShadowCaster(mapGraph);
    }

    public Square getSquare(int x, int y) {
        return mapDrawable.getSquare(x, y);
    }

    public Vertex getVertex(int x, int y) {
        return mapDrawable.getVertex(x, y);
    }


    public void redrawStartingPosition() {
        mapDrawable.drawSquareBackgrounds(moveableSquares, mapDrawable.getSquareHlPaint());
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
        return pathfinder.settle(new Vertex(playerX, playerY), new Vertex(x, y));
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
        highlightSquares(target, maximumMovementLength);
        if (events.containsPosition(target)) {
            events.getEventAt(target).setAll(true);
        }
    }

    //TODO: possibly change this to private later
    public void highlightSquares(Vertex middle, int radius) {
        moveableSquares = new HashSet<>();
        Set<Vertex> visited = new HashSet<>();
        Queue<Stack<Vertex>> nodes = new LinkedList<>();

        Stack<Vertex> tempStack = new Stack<>();
        tempStack.push(middle);
        nodes.offer(tempStack);
        visited.add(middle);

        Graph.Node currentNode;
        Vertex currentNeighbor;

        while (!nodes.isEmpty()) {
            tempStack = nodes.poll();
            currentNode = mapGraph.getNode(tempStack.peek());

            moveableSquares.add(currentNode.getVertex());

            //add 1 to disregard the middle
            if (tempStack.size() == (maximumMovementLength + 1)) continue;

            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (mapGraph.getNode(currentNeighbor).isBlocked()) continue;
                if (!visited.contains(currentNeighbor)) {
                    visited.add(currentNeighbor);
                    Stack<Vertex> clone = (Stack<Vertex>) tempStack.clone();
                    clone.push(currentNeighbor);
                    nodes.offer(clone);
                }
            }
        }

        mapDrawable.drawSquareBackgrounds(moveableSquares, mapDrawable.getSquareHlPaint());
    }

    public Set<Vertex> highlightFOVRadius(Vertex middle, Direction direction, Paint paint) {
        Set<Vertex> traversed = new HashSet<>();
        Set<Vertex> visited = new HashSet<>();
        Stack<Stack<Vertex>> nodes = new Stack<>();

        int middleY = middle.getY();
        int middleX = middle.getX();

        Graph.Node currentNode;
        Vertex currentNeighbor;

        Stack<Vertex> tempStack = new Stack<>();
        tempStack.push(middle);
        nodes.push(tempStack);

        boolean correctDirection = false;

        while (!nodes.isEmpty()) {
            tempStack = nodes.pop();
            currentNode = mapGraph.getNode(tempStack.peek());

            getSquare(currentNode.getVertex().getX(), currentNode.getVertex().getY()).setBackground(paint);
            traversed.add(currentNode.getVertex());

            //add 1 to disregard the middle
            if (tempStack.size() == (maximumMovementLength + 1)) continue;

            for (Edge e : currentNode.getNeighbors()) {
                currentNeighbor = e.getDestination();
                if (visited.contains(currentNeighbor)) continue;
                visited.add(currentNeighbor);

                switch (direction) {
                    case SOUTH:
                        if (currentNeighbor.getY() >= middleY) {
                            correctDirection = true;
                        }
                        break;
                    case NORTH:
                        if (currentNeighbor.getY() <= middleY) {
                            correctDirection = true;
                        }
                        break;
                    case WEST:
                        if (currentNeighbor.getX() <= middleX) {
                            correctDirection = true;
                        }
                        break;
                    case EAST:
                        if (currentNeighbor.getX() >= middleX) {
                            correctDirection = true;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid Direction " + direction);
                }

                if (correctDirection) {
                    Stack<Vertex> clone = (Stack<Vertex>) tempStack.clone();
                    clone.push(currentNeighbor);
                    nodes.push(clone);
                    correctDirection = false;
                }
            }
        }

        return traversed;
    }

    /*
     * Unimplemented
     */
    public Set<Vertex> castFOVShadow(Vertex middle, Direction direction, Paint paint) {
        //Set<Vertex> shadow = shadowCaster.castShadows(middle,maximumMovementLength);
        //return shadow;
        return null;
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
